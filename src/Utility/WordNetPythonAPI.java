package Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WordNetPythonAPI {

	public static void main(String[] args) {
		String word="good";
		String tag ="adj.all";
		int choice = 3;
		switch(choice)
		{
		case 1: List<String> list= new ArrayList<String>();
		
				
				list= getDefinition(1, word, tag);
				if(list.isEmpty())
					System.out.println("No meanings found for the word.");
				else{
				System.out.println("Word - Meaning");
				// for(String k: list.keySet())
				 {
				//	 System.out.println(k+"-"+list.get(k));
				 }
				}
				break;
		case 2: 
				List<String> synonyms = new ArrayList();
		
				synonyms=getSynonym(2,word,tag);
				if(synonyms.isEmpty())
					System.out.println("No synonyms found for the word.");
				else {
				System.out.println("Synonyms:");
				for(int i=0;i<synonyms.size();i++)
				{
					System.out.println(synonyms.get(i));
					
				}
				}
				break;
				
		case 3:
				List<String> antonyms = new ArrayList();
				antonyms= getAntonyms(3,word,tag);
				if(antonyms.isEmpty())
					System.out.println("No antonyms found for the word.");
				else{
				System.out.println("Antonyms:");
				for(int i=0;i<antonyms.size();i++)
				{
					System.out.println(antonyms.get(i));
					
				}
				}
				break;
			
		}
		
		 
		
	}
	
	public static List<String> getDefinition(int choice, String word, String tag)
	{
		List <String> list= new ArrayList<String>();
	    try {
			Process p = Runtime.getRuntime().exec("python /home/vishnu/workspace/QuestionGeneration/pythonscripts syn.py "+choice+" "+word+" "+tag);
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
			Process p = Runtime.getRuntime().exec("python syn.py "+choice+" "+word+" "+tag);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = in.readLine()) != null)
			{
				String[] splitter = line.split("\\.");
				synonyms.add(splitter[0]);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		return synonyms;
		
	}
	public static List<String> getAntonyms(int choice, String word, String tag)
	{
		List<String> antonyms = new ArrayList();
		String line =new String();
		try {
			Process p = Runtime.getRuntime().exec("python syn.py "+choice+" "+word+" "+tag);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = in.readLine()) != null)
			{
			
				antonyms.add(line);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return antonyms;
		
	}
	
}
