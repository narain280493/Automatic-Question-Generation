package ComprehensionQuestionGeneration;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.aliasi.tokenizer.PorterStemmerFilterTokenizer;

import Configuration.Configuration;
import Utility.SuperSenseTagHelper;
import Utility.ThesaurusAPI;
import Utility.VocabularyRanker;
import Utility.WordNetPythonAPI;
import edu.cmu.ark.PorterStemmer;
import edu.cmu.ark.QuestionAsker;

public class VocabularyQuestion {
	//Using POS tagging remove articles,nouns,pronouns
	public static final Map<String,List<String> > TAG_MAP = new HashMap<String,List<String>>();
	
	public static List<String> getWordsFromPOSTag (String fileName, String queryString) {
		 
	    
	      URL u;
	      InputStream is = null;
	      DataInputStream dis;
	      String s;
	      
	      String str="";
	      String urlString="";
		try {
			urlString = "http://localhost:8080/hitMe?queryString="+URLEncoder.encode(queryString,"UTF-8")+"&&type=2&&fileName="+URLEncoder.encode(fileName,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	      
	      try {
	 
	             u = new URL(urlString);
	               is = u.openStream();         // throws an IOException
	 
	       
	         dis = new DataInputStream(new BufferedInputStream(is));
	               while ((s = dis.readLine()) != null) {
	                    str+=s;
	         }
	               
	 
	      } catch (MalformedURLException mue) {
	 
	         System.out.println("Ouch - a MalformedURLException happened.");
	         mue.printStackTrace();
	         System.exit(1);
	 
	      } catch (IOException ioe) {
	 
	         System.out.println("Oops- an IOException happened.");
	         ioe.printStackTrace();
	         System.exit(1);
	 
	      } finally {
	 
	       
	         try {
	            is.close();
	         } catch (IOException ioe) {
	            // just going to ignore this one
	         }
	 
	      } // end of 'finally' clause
	      List<String> list = Arrays.asList(str.split(","));
	   
	      return list;
	   }
	
	public static void populateTagMap(String inputFilePath){
		//The following tags are considered for selecting vocabularies
		/*
		JJ	Adjective
		JJR	Adjective, comparative
		JJS	Adjective, superlative
		VB	Verb, base form
		VBG	Verb, gerund or present participle
		VBZ Verb, 3rd person singular present
		RB	Adverb
		RBR	Adverb, comparative
		RBS	Adverb, superlative
		*/
		 
		String[] posTags = {"JJ","JJR","JJS","VB","VBG","VBZ","RB","RBR","RBS"};
		System.out.println("Words considered as vocabularies");
		
		for(String tag:posTags){
			//@param 1 - fileName Note: when you are testing hardcode the filename here.When this gets integrated in 
			//QuestionGeneration mention QuestionAsker.fileName
			List<String> list=getWordsFromPOSTag(inputFilePath, tag);
			
			System.out.println("Tag :"+tag);
			if(list.size()==1&&list.get(0).equals("-1")){
				System.out.println("No words in this tag ");
			}
			else{		
				for(String word:list){
					System.out.println(word);
				}
				TAG_MAP.put(tag, list);
			}
		}
		
		
	}
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	public static  List<String> getVocabularies(){
		String[] posTags = {"JJ","JJR","JJS","VB","VBG","VBZ"};
		Set<String> selectedWords=new HashSet<String>();
		List<String> vocabularyList=new ArrayList<String>();
		for(String tag:posTags){
		
			if(TAG_MAP.containsKey(tag)){
				
				selectedWords.addAll(TAG_MAP.get(tag));
			}
		}
		vocabularyList.addAll(selectedWords);
		return vocabularyList;
		/*	int count=0;
		int tagNum;
		int wordNum;
		while(count<5){
			tagNum=randInt(0,posTags.length-1);
			if(TAG_MAP.containsKey(posTags[tagNum])){
				List<String> words=TAG_MAP.get(posTags[tagNum]);
				wordNum=randInt(0, words.size()-1);
				if(!selectedWords.contains(words.get(wordNum))){
					System.out.println("Word : "+words.get(wordNum)+" Tag : "+posTags[tagNum]);
					selectedWords.add(words.get(wordNum));
				}
			}
	*/
	}
	public static List<String> rankVocabularies(List<String> vocabularyList){
		List<String> rankedVocabularyList=VocabularyRanker.getRankedlist(vocabularyList);
		System.out.println("Rankedlist");
		for(String word:rankedVocabularyList){
			System.out.println(word);
		}
		return rankedVocabularyList;
	}
	
	public static void generateMatchTheSynonymQuestion(String inputFilePath){
		List<String> vocabularyList=getVocabularies();
		List<String> rankedVocabularyList=rankVocabularies(vocabularyList);
		List<String> selectedVocabularyList=new ArrayList<String>();
		List<List<String>> selectedVocabularySynonymList=new ArrayList<List<String>>();
		int count=0;
		//PorterStemmer stemmer=new PorterStemmer();
		//System.out.println("WORD AND SST Tag");
		System.out.println("FINAL LIST");
		for(String word:rankedVocabularyList){
			String SSTTag=SuperSenseTagHelper.getSSTForGivenWord(inputFilePath, word);
			if(SSTTag.equals("0")||SSTTag.equals("WORD_NOT_FOUND_IN_WORD_MAP")){
				continue;
			}
			else{
				List<String> synonyms=WordNetPythonAPI.getSynonym(2, word, SSTTag);
				if(synonyms.size()==0)
					continue;
				System.out.print("WORD: "+word+" SYNONYM: ");
				for(String syn:synonyms){
					System.out.print(syn+";");
				}
				System.out.println();
				selectedVocabularySynonymList.add(synonyms);
				count++;
			}
			if(count==5)
				break;
		}
		
		for(int i=0;i<selectedVocabularyList.size();i++){
			System.out.println("WORD "+selectedVocabularyList.get(i));
			System.out.println("SYNONYMS "+selectedVocabularySynonymList.get(i).get(0));
		}
		
	}
	public static void generateMatchTheDefinitionQuestion(String inputFilePath){
		List<String> vocabularyList=getVocabularies();
		List<String> rankedVocabularyList=rankVocabularies(vocabularyList);
		List<String> selectedVocabularyList=new ArrayList<String>();
		List<List<String>> selectedVocabularyMeaningList=new ArrayList<List<String>>();
		int count=0;
		//PorterStemmer stemmer=new PorterStemmer();
		//System.out.println("WORD AND SST Tag");
		System.out.println("FINAL LIST");
		for(String word:rankedVocabularyList){
			String SSTTag=SuperSenseTagHelper.getSSTForGivenWord(inputFilePath, word);
			if(SSTTag.equals("0")||SSTTag.equals("WORD_NOT_FOUND_IN_WORD_MAP")){
				continue;
			}
			else{
				List<String> meanings=WordNetPythonAPI.getDefinition(1, word, SSTTag);
				if(meanings.size()==0)
					continue;
				System.out.print("WORD: "+word+" MEANING: ");
				for(String syn:meanings){
					System.out.print(syn+";");
				}
				System.out.println();
				selectedVocabularyMeaningList.add(meanings);
				count++;
			}
			if(count==5)
				break;
		}
		
		for(int i=0;i<selectedVocabularyList.size();i++){
			System.out.println("WORD "+selectedVocabularyList.get(i));
			System.out.println("SYNONYMS "+selectedVocabularyMeaningList.get(i).get(0));
		}
		
	}
	
	/*	System.out.println("Match the following using set :"+selectedWords);
		for(String str:selectedWords){
			System.out.print(str +" - ");
			List<String> synonyms=ThesaurusAPI.getSynonyms(str);
			if(synonyms!=null){
				for(int i=0;i<synonyms.size();i++){
					System.out.print(synonyms.get(i));
					if(i!=synonyms.size()-1)
						System.out.print(",");
				}
				System.out.println();
			}
			else{
				System.out.println("Unable to generate synonyms for :"+str);
			}
		}
	
	*/
	
	public static void main(String[] args) {
		String fileName="earthquake.txt";
		populateTagMap("/home/vishnu/workspace/QuestionGeneration/"+fileName);
		generateMatchTheSynonymQuestion("/home/vishnu/workspace/QuestionGeneration/"+fileName);
		generateMatchTheDefinitionQuestion("/home/vishnu/workspace/QuestionGeneration/"+fileName);
		
		
	}

}
