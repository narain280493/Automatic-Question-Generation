package Utility;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import Configuration.Configuration;
import edu.cmu.ark.PorterStemmer;


public class WordNetPythonAPI {
	public static List<String> getResponse(String type,String word,String superSenseTag){

	      URL u;
	      InputStream is = null;
	      DataInputStream dis;
	      String s;
	      
	      String str="";
	      String urlString="";
		try {
			urlString = "http://localhost:8030/"+URLEncoder.encode(type,"UTF-8")+"/"+URLEncoder.encode(word,"UTF-8")+"/"+URLEncoder.encode(superSenseTag,"UTF-8");
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
	      List<String> responseList=new ArrayList<String>();
	      
	      String[] list=str.split(";");
	      for(String responseWord:list){
	    	  responseList.add(responseWord);
	      }
	      responseList=removeDuplicates(responseList, word);
	      return responseList;
	      
	   }
	public static void main(String[] args) {
		String word="good";
		String tag ="adj.all";
		List<String>result=getResponse("antonym", word, tag);
		for(String str:result){
			System.out.println(str);
		}
	}
	
	public static List<String> getDefinition(int choice, String word, String tag)
	{
		List <String> list= new ArrayList<String>();
	    try {
			Process p = Runtime.getRuntime().exec(Configuration.WORDNET_PYTHON_SCRIPT+" "+choice+" "+word+" "+tag);
			int counter =0;
			String line;
			
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = in.readLine()) != null)
			{
				list.add(line);
			}
			
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		
		return list;
		
	}
	public static List<String> getSynonym(int choice, String word, String tag)
	{
		List<String> synonyms = new ArrayList();
		String line =new String();
		try {
			Process p = Runtime.getRuntime().exec(Configuration.WORDNET_PYTHON_SCRIPT+" "+choice+" "+word+" "+tag);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = in.readLine()) != null)
			{
				String[] splitter = line.split("\\.");
				synonyms.add(splitter[0]);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		synonyms=removeDuplicates(synonyms,word);
		
		return synonyms;
		
	}
	
	
	public static List<String> getAntonyms(int choice, String word, String tag)
	{
		List<String> antonyms = new ArrayList();
		String line =new String();
		try {
			Process p = Runtime.getRuntime().exec(Configuration.WORDNET_PYTHON_SCRIPT+" "+choice+" "+word+" "+tag);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = in.readLine()) != null)
			{
			
				antonyms.add(line);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		antonyms=removeDuplicates(antonyms,word);
		return antonyms;
		
	}
	//removes duplicates
	//removes stem words
	public static List<String> removeDuplicates(List<String> words,String word)
	{
		String rootWord=PorterStemmer.getInstance().stem(word);
		words = new ArrayList<String>(new HashSet<String>(words));
		words.remove(word);
		if(words.contains(rootWord))
			words.remove(rootWord);
		return words;
	
	}
	
}
