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
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.ark.QuestionAsker;

public class VocabularyQuestion {
	//Using POS tagging remove articles,nouns,pronouns
	public static final Map<String,List<String> > TAG_MAPS = new HashMap<String,List<String>>();
	
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
			List<String> list=getWordsFromPOSTag("input.txt", tag);
			
			System.out.println("Tag :"+tag);
			if(list.size()==1&&list.get(0).equals("-1")){
				System.out.println("No words in this tag ");
			}
			else{		
				for(String word:list){
					System.out.println(word);
				}
				TAG_MAPS.put(tag, list);
			}
		}
		
		
	}
	public static void main(String[] args) {
		populateTagMap();
	}

}
