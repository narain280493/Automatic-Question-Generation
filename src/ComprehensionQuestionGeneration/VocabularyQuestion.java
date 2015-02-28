package ComprehensionQuestionGeneration;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Configuration.Configuration;
import Utility.ThesaurusAPI;
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
	public static void populateTagMap(){
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
			List<String> list=getWordsFromPOSTag(Configuration.INPUT_FILE_PATH, tag);
			
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
	public static void generateMatchTheSynonymQuestion(){
		String[] posTags = {"JJ","JJR","JJS","VB","VBG","VBZ","RB","RBR","RBS"};
		Set<String> selectedWords=new HashSet<String>();
		int count=0;
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
	}
	public static void main(String[] args) {
		populateTagMap();
		generateMatchTheSynonymQuestion();
	}

}
