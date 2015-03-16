package edu.cmu.ark;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * Class that read in SRILM toolkit-built language models (hopefully of any order) and computes 
 * sentence probabilities from them.
 * 
 * Not a particularly efficient implementation!
 * 
 * Example SRILM command:
 * ngram-count -order 5 -interpolate -kndiscount -text nyt_eng_200601.tok -lm nyt_eng_200601.lm -tolower -unk -prune 1e-6 -vocab my.vocab
 * 
 * Note that SRILM stores everything in BASE 10, not BASE e!
 * 
 * @author mheilman
 *
 */
public class LanguageModel {
	public LanguageModel(String path){		
		load(path);
	}
	
	
	public double logBase10ProbabilityOfSentence(List<String> tokens){
		List<String> tokList = new ArrayList<String>();
		tokList.add("<s>");
		tokList.addAll(tokens);
		tokList.add("</s>");
				
		return logBase10ProbabilityOfSequence(tokList);
	}
	

	public double logBase10ConditionalProbability(List<String> tokList, List<String> givenTokList){
		double res = 0.0;
		
		List<String> combined = new ArrayList<String>();
		combined.addAll(givenTokList);
		combined.addAll(tokList);
		
		double p1 = logBase10ProbabilityOfSequence(combined);
		double p2 = logBase10ProbabilityOfSequence(givenTokList);
		
		res = p1 - p2;
		return res;
	}
	
	
	public double logBase10ProbabilityOfSequence(List<String> tokList){
		double res = 0.0;		
		for(int i=0; i<tokList.size(); i++){
			res += helper(tokList.subList(Math.max(0, i+1-maxOrder), i+1));
		}
		return res;
	}
	
	
	private double helper(List<String> subList) {
		if(subList.get(subList.size()-1).equals("<s>")){ 
			return 0.0; //log 1
		}
		String key = key(subList);
		Double res = frequencies.get(key); 
		
		//System.err.println("pr:"+key);
		if(res == null){
			if(subList.size() == 1){
				//can't backoff any further
				res = frequencies.get("<unk>");
			}else{
				//log a*b = a+b
				res = backOff(subList.subList(0, subList.size()-1)) + helper(subList.subList(1, subList.size()));
			}
		}
			
		return res;
	}

	private double backOff(List<String> subList){
		String key = key(subList);
		Double res = backOffWeights.get(key);
		
		//System.err.println("backoff:"+key);
		if(res == null){
			res = 0.0; //log(1)
		}
		
		return res;
	}


	private String key(List<String> subList) {
		String key = "";
		for(String w: subList){
			key += w.toLowerCase() + " ";
		}
		key = key.trim();
		return key;
	}

	
	private void load(String filename){
		frequencies = new HashMap<String, Double>();
		backOffWeights = new HashMap<String, Double>();
		frequencies.put("<unk>", 0.0);  //just in case nothing loads

		if(GlobalProperties.getDebug()) System.err.print("Loading language model from "+filename+"...");
		
		try{
			String buf;
			String [] parts;
			BufferedReader br;
			if(filename.matches(".*\\.gz")){
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
			}else{
				br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			}
			while((buf=br.readLine()) != null){
				parts = buf.split("\\t");
				if(parts.length < 2){
					continue;
				}
				try{
					int order = parts[1].split("\\s").length;
					if(order > maxOrder){
						maxOrder = order;
					}
					frequencies.put(parts[1], new Double(parts[0]));
					if(parts.length > 2) backOffWeights.put(parts[1], new Double(parts[2]));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(GlobalProperties.getDebug()) System.err.println("done.");
	}
	
	public double unigramLogBase10Probability(String word){
		Double res = frequencies.get(word.toLowerCase());
		if(res == null){
			res = frequencies.get("<unk>");
		}
		return res.doubleValue();
	}
	
	public double meanUnigramLogBase10Probability(List<String> tokens){
		double res = 0.0;
		int numtokens = tokens.size();
		for(int i=0; i<numtokens; i++){
			res += unigramLogBase10Probability(tokens.get(i));
		}
		if(numtokens>0) res /= numtokens;
		return res;
	}
	
	
	public static void main(String [] args) throws IOException{
		
		System.err.println("Enter a sentence with spaces between tokens:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String buf;
		NumberFormat nf = NumberFormat.getInstance();
		while((buf = br.readLine()) != null){
			String [] tokens = buf.split("\\s+");
			if(tokens.length == 0){
				continue;
			}
			LanguageModel lm = new LanguageModel(GlobalProperties.getProperties().getProperty("languageModelFile", "config"+File.separator+"anc-v2-written.lm.gz"));
			double r = lm.logBase10ProbabilityOfSequence(Arrays.asList(tokens));
			System.out.println("Prob.:"+nf.format(r)+"\tNorm. by length:"+nf.format(r/tokens.length));
			System.err.println("Enter a sequence of words with spaces between tokens:");
		}
		
	}
	
	private int maxOrder;
	private Map<String, Double> frequencies;
	private Map<String, Double> backOffWeights;
}
