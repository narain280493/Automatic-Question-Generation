// Question Generation via Overgenerating Transformations and Ranking
// Copyright (c) 2010 Carnegie Mellon University.  All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Michael Heilman
//	  Carnegie Mellon University
//	  mheilman@cmu.edu
//	  http://www.cs.cmu.edu/~mheilman

package edu.cmu.ark;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;


import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.parser.lexparser.*;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.tregex.*;
import edu.stanford.nlp.trees.tregex.tsurgeon.*;
import edu.stanford.nlp.util.Pair;



public class AnalysisUtilities {
	private AnalysisUtilities(){
		parser = null;
		
		conjugator = new VerbConjugator();
		conjugator.load(GlobalProperties.getProperties().getProperty("verbConjugationsFile", "config"+File.separator+"verbConjugations.txt"));
		headfinder = new CollinsHeadFinder();
		tree_factory = new LabeledScoredTreeFactory();
		tlp = new PennTreebankLanguagePack();
	}
	
	
	public static void addPeriodIfNeeded(Tree input){	
		String tregexOpStr = "ROOT < (S=mainclause !< /\\./)";
		TregexPattern matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(input);

		if(matcher.find()){
			TsurgeonPattern p;
			List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
			List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();

			ps.add(Tsurgeon.parseOperation("insert (. .) >-1 mainclause"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, input);
		}
	}

	public static int getNumberOfMatchesInTree(String tregexExpression, Tree t){
		int res = 0;
		TregexMatcher m = TregexPatternFactory.getPattern(tregexExpression).matcher(t);
		while(m.find()){
			res++;
		}
		return res;
	}
	
	
	public static List<String> getSentences(String document) {
		DocumentPreprocessor dp = new DocumentPreprocessor(false);
		List<String> res = new ArrayList<String>();
		String sentence;
		
		document = preprocess(document);
		
		
		String [] paragraphs = document.split("\\n");
		
		for(int i=0; i<paragraphs.length; i++){
			StringReader reader = new StringReader(paragraphs[i]);
			List<List<? extends HasWord>> sents = new ArrayList<List<? extends HasWord>>();
		
			try{
				sents = dp.getSentencesFromText(reader);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			for(List<? extends HasWord> tmp1: sents){
				sentence = "";
				for(HasWord tmp2: tmp1){
					String tmp = tmp2.word().toString();
					sentence += tmp + " ";
				}
				sentence = sentence.trim();
				res.add(sentence);
			}
		}
		
		return res;
	}
	
	public static String abbrevTree(Tree tree) {
		ArrayList<String> toks = new ArrayList<String>();
		for (Tree L : tree.getLeaves()) {
			toks.add(L.label().toString());
		}
		return tree.label().toString() + "[" + StringUtils.join(toks, " ") + "]";
	}
	
	public static void downcaseFirstToken(Tree inputTree){
		Tree firstWordTree = inputTree.getLeaves().get(0);
		if(firstWordTree == null)	return;
		Tree preterm = firstWordTree.parent(inputTree);
		String firstWord = firstWordTree.yield().toString();
		if(!preterm.label().toString().matches("^NNP.*") && !firstWord.equals("I")){	
		//if(firstWord.indexOf('-') == -1 && !firstWord.equals("I")){
			firstWord = firstWord.substring(0,1).toLowerCase() + firstWord.substring(1);
			firstWordTree.label().setValue(firstWord);
		}

		//if(QuestionTransducer.DEBUG) System.err.println("downcaseFirstToken: "+inputTree.toString());
	}
	
	
	public static void upcaseFirstToken(Tree inputTree){
		Tree firstWordTree = inputTree.getLeaves().get(0);
		if(firstWordTree == null)	return;
		
		String firstWord = firstWordTree.yield().toString();
		firstWord = firstWord.substring(0,1).toUpperCase() + firstWord.substring(1);
		firstWordTree.label().setValue(firstWord);

		//if(QuestionTransducer.DEBUG) System.err.println("upcaseFirstToken: "+inputTree.toString());
	}
	
	
	
	public static String preprocess(String sentence) {
		//remove trailing whitespace	
		sentence = sentence.trim();
		
		
		//remove single words in parentheses.  
		//the stanford parser api messed up on these
		//by removing the parentheses but not the word in them
		sentence = sentence.replaceAll("\\(\\S*\\)","");
		sentence = sentence.replaceAll("\\(\\s*\\)","");
		
		//some common unicode characters that the tokenizer throws out otherwise
		sentence = sentence.replaceAll("—", "--");
		sentence = sentence.replaceAll("’", "'");
		sentence = sentence.replaceAll("”", "\"");
		sentence = sentence.replaceAll("“", "\"");
		sentence = sentence.replaceAll("é|è|ë|ê", "e");
		sentence = sentence.replaceAll("É|È|Ê|Ë", "E");
		sentence = sentence.replaceAll("ì|í|î|ï", "i");
		sentence = sentence.replaceAll("Ì|Í|Î|Ï", "I");
		sentence = sentence.replaceAll("à|á|â|ã|ä|æ|å", "a");
		sentence = sentence.replaceAll("À|Á|Â|Ã|Ä|Å|Æ", "A");
		sentence = sentence.replaceAll("ò|ó|ô|õ|ö", "o");
		sentence = sentence.replaceAll("Ò|Ó|Ô|Õ|Ö", "O");
		sentence = sentence.replaceAll("ù|ú|û|ü", "u");
		sentence = sentence.replaceAll("Ù|Ú|Û|Ü", "U");
		sentence = sentence.replaceAll("ñ", "n");

		//contractions
		sentence = sentence.replaceAll("can't", "can not");
		sentence = sentence.replaceAll("won't", "will not");
		sentence = sentence.replaceAll("n't", " not"); //aren't shouldn't don't isn't
		sentence = sentence.replaceAll("are n't", "are not");
		
		//simply remove other unicode characters
		//if not, the tokenizer replaces them with spaces, 
		//which wreaks havoc on the final parse sometimes
		for(int i=0;i<sentence.length();i++){
			if(sentence.charAt(i) > 'z'){
				sentence = sentence.substring(0,i)+sentence.substring(i+1);
			}
		}
		
		
		//add punctuation to the end if necessary
		/*Matcher matcher = Pattern.compile(".*\\.['\"\n ]*$", Pattern.DOTALL).matcher(sentence);
		if(!matcher.matches()){
			sentence += ".";
		}*/
		

		
		return sentence;
	}
	
	
	public static String preprocessTreeString(String sentence) {
		sentence = sentence.replaceAll(" n't", " not");
		sentence = sentence.replaceAll("\\(MD ca\\)", "(MD can)");
		sentence = sentence.replaceAll("\\(MD wo\\)", "(MD will)");
		sentence = sentence.replaceAll("\\(MD 'd\\)", "(MD would)");
		sentence = sentence.replaceAll("\\(VBD 'd\\)", "(VBD had)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBP 're\\)", "(VBP are)");
		
		return sentence;
	}
	
	
	public VerbConjugator getConjugator(){
		return conjugator;
	}
	
	
	public CollinsHeadFinder getHeadFinder(){
		return headfinder;
	}
	
	
	public static AnalysisUtilities getInstance(){
		if(instance == null){
			instance = new AnalysisUtilities();
		}
		return instance;
	}
	

	
	public ParseResult parseSentence(String sentence) {
		String result = "";
		//System.err.println(sentence);
		//see if a parser socket server is available
        int port = new Integer(GlobalProperties.getProperties().getProperty("parserServerPort","5556"));
        String host = "127.0.0.1";
        Socket client;
        PrintWriter pw;
        BufferedReader br;
        String line;
        Tree parse = null;
        double parseScore = Double.MIN_VALUE;
        
		try{
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pw.println(sentence);
			pw.flush(); //flush to complete the transmission

            while((line = br.readLine())!= null){
                //if(!line.matches(".*\\S.*")){
                //        System.out.println();
                //}
                if(br.ready()){
                	line = line.replaceAll("\n", "");
                    line = line.replaceAll("\\s+", " ");
                	result += line + " ";
                }else{
                	parseScore = new Double(line);
                }
            }

			br.close();
			pw.close();
			client.close();
			
			if(parse == null){
				parse = readTreeFromString("(ROOT (. .))");
				parseScore = -99999.0;
			}
		
			if(GlobalProperties.getDebug()) System.err.println("result (parse):"+result);
			parse = readTreeFromString(result);
			return new ParseResult(true, parse, parseScore);
			
		} catch (Exception ex) {
			if(GlobalProperties.getDebug()) System.err.println("Could not connect to parser server.");
			//ex.printStackTrace();
		}
        
		System.err.println("parsing:"+sentence);
		
		//if socket server not available, then use a local parser object
		if(parser == null){
			try {
				Options op = new Options();
				String serializedInputFileOrUrl = GlobalProperties.getProperties().getProperty("parserGrammarFile", "config"+File.separator+"englishFactored.ser.gz");
				parser = new LexicalizedParser(serializedInputFileOrUrl, op);
				int maxLength = new Integer(GlobalProperties.getProperties().getProperty("parserMaxLength", "40")).intValue();
				parser.setMaxLength(maxLength);
				parser.setOptionFlags("-outputFormat", "oneline");
			} catch (Exception e){
				e.printStackTrace();
			}
		}

		try{
			if(parser.parse(sentence)){
				parse = parser.getBestParse();
				
				//remove all the parent annotations (this is a hacky way to do it)
				String ps = parse.toString().replaceAll("\\[[^\\]]+/[^\\]]+\\]", "");
				parse = AnalysisUtilities.getInstance().readTreeFromString(ps);
				
				parseScore = parser.getPCFGScore();
				return new ParseResult(true, parse, parseScore);
			}
		}catch(Exception e){
		}

		parse = readTreeFromString("(ROOT (. .))");
        parseScore = -99999.0;
        return new ParseResult(false, parse, parseScore);
	}
	
	
	public String getLemma(String word, String pos){
		if(!(pos.startsWith("N") || pos.startsWith("V") || pos.startsWith("J") || pos.startsWith("R"))
				|| pos.startsWith("NNP"))
		{
			return word.toLowerCase();
		}
				
		String res = word.toLowerCase();
		
		if(res.equals("is") || res.equals("are") || res.equals("were") || res.equals("was")){
			res = "be";
		}else{
			try{
				IndexWord iw;
				if(pos.startsWith("V")) iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(POS.VERB, res);
				else if(pos.startsWith("N")) iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(POS.NOUN, res);
				else if(pos.startsWith("J")) iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(POS.ADJECTIVE, res);
				else iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(POS.ADVERB, res);
					
				if(iw == null) return res;
				res = iw.getLemma();
			}catch(Exception e){
				e.printStackTrace();
			}
		}		
		
		return res;
	}
	

	
	/**
	 * Remove traces and non-terminal decorations (e.g., "-SUBJ" in "NP-SUBJ") from a Penn Treebank-style tree.
	 * 
	 * @param inputTree
	 */
	public void normalizeTree(Tree inputTree){
		inputTree.label().setFromString("ROOT");

		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		TregexMatcher matcher;
		
		tregexOpStr = "/\\-NONE\\-/=emptynode";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		ps.add(Tsurgeon.parseOperation("prune emptynode"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, inputTree);
		
		Label nonterminalLabel;
		
		tregexOpStr = "/.+\\-.+/=nonterminal < __";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		while(matcher.find()){
			nonterminalLabel = matcher.getNode("nonterminal");
			if(nonterminalLabel == null) continue;
			nonterminalLabel.setFromString(tlp.basicCategory(nonterminalLabel.value()));
		}
		

	}
	
	/**
	 * remove extra quotation marks
	 * (a hack due to annoying PTB conventions by which quote marks aren't in the same consituent)
	 *  
	 * @param input
	 */
	public static void removeExtraQuotes(Tree input){
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		List<TsurgeonPattern> ps;

		ps = new ArrayList<TsurgeonPattern>();
		tregexOpStr = "ROOT [ << (``=quote < `` !.. ('' < '')) | << (''=quote < '' !,, (`` < ``)) ] ";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		ps.add(Tsurgeon.parseOperation("prune quote"));
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, input);
		
		
	}
	
	
	public static String getCleanedUpYield(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();

		//if(GlobalProperties.getDebug()) System.err.println("yield:"+copyTree.toString());

		return cleanUpSentenceString(copyTree.yield().toString());
	}
	
	
	
	public static String cleanUpSentenceString(String s) {
		String res = s;
		//if(res.length() > 1){
		//	res = res.substring(0,1).toUpperCase() + res.substring(1);
		//}
		
		res = res.replaceAll("\\s([\\.,!\\?\\-;:])", "$1");
		res = res.replaceAll("(\\$)\\s", "$1");
		res = res.replaceAll("can not", "cannot");
		res = res.replaceAll("\\s*-LRB-\\s*", " (");
		res = res.replaceAll("\\s*-RRB-\\s*", ") ");
		res = res.replaceAll("\\s*([\\.,?!])\\s*", "$1 ");
		res = res.replaceAll("\\s+''", "''");
		//res = res.replaceAll("\"", "");
		res = res.replaceAll("``\\s+", "``");
		res = res.replaceAll("\\-[LR]CB\\-", ""); //brackets, e.g., [sic]
		res = res.replaceAll("\\. \\?", ".?");
		res = res.replaceAll(" 's(\\W)", "'s$1");
		res = res.replaceAll("(\\d,) (\\d)", "$1$2"); //e.g., "5, 000, 000" -> "5,000,000"
		res = res.replaceAll("``''", "");
		
		//remove extra spaces
		res = res.replaceAll("\\s\\s+", " ");
		res = res.trim();

		return res;
	}
	
	public static boolean cCommands(Tree root, Tree n1, Tree n2){
		if(n1.dominates(n2)) return false;
		
		Tree n1Parent = n1.parent(root);
		while(n1Parent != null && n1Parent.numChildren() == 1){
			n1Parent = n1Parent.parent(root);
		}
		
		if(n1Parent != null && n1Parent.dominates(n2)) return true;
		
		return false;
	}
	
	
	public Tree readTreeFromString(String parseStr){
		//read in the input into a Tree data structure
		TreeReader treeReader = new PennTreeReader(new StringReader(parseStr), tree_factory);
		Tree inputTree = null;
		try{
			inputTree = treeReader.readTree();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return inputTree;
	}
	
	public static boolean filterOutSentenceByPunctuation(String sentence) {
		//return (sentence.indexOf("\"") != -1 
				//|| sentence.indexOf("''") != -1 
				//|| sentence.indexOf("``") != -1
				//|| sentence.indexOf("*") != -1);
			if(sentence.indexOf("*") != -1){
				return true;
			}
			
			//if(sentence.matches("[^\\w\\-\\/\\?\\.,;:\\$\\#\\&\\(\\) ]")){
			//	return true;
			//}
			
			return false;
	}
	
	

	public String getSurfaceForm(String lemma, String pos) {
		return conjugator.getSurfaceForm(lemma, pos);
	}

	private LexicalizedParser parser;
	private static AnalysisUtilities instance;
	private VerbConjugator conjugator;
	private CollinsHeadFinder headfinder;
	private LabeledScoredTreeFactory tree_factory;
	private PennTreebankLanguagePack tlp;


}