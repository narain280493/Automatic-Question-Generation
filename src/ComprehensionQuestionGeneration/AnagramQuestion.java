package ComprehensionQuestionGeneration;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.util.*;
import java.*;

import Configuration.Configuration;
import TopicExtraction.Topic;
import TopicExtraction.WikipediaMinerAPI;
import Utility.MiscellaneousHelper;
/**
 *
 * @author aditya
 */
public class AnagramQuestion 
{
	public static void main (String args[]) 
        {		
		 	anagramMaker();
		
	}
        static String getAnagram(String a){
        	//making all uppercase
        	a.toUpperCase();
        	int len=a.length();
            Random rand=new Random();
            int j=0;
            int[] num=new int[len];
            boolean[] b=new boolean[len]; 
            char[] anagram = new char[len];
            
            //Random Sequence
            while(j!=len)
            {
                int randomInt = rand.nextInt(len);
                if(b[randomInt]==false)
                {
                    b[randomInt]=true;
                    num[j]=randomInt;
                    j++;
                }
            }
            //Creating anagram according to random sequence
            for (int i=0; i<len; i++) 
                anagram[i]=a.charAt(num[i]);
            
            return String.valueOf(anagram).toUpperCase();   

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
		static void anagramMaker()
        {	
				System.out.println("Enter input filename");
				Scanner in=new Scanner(System.in);
				//Read a string from the user
				Configuration.INPUT_FILE_NAME = in.nextLine();
				String inputString="";
				
				try {
					inputString=MiscellaneousHelper.readFile(Configuration.INPUT_FILE_PATH+Configuration.INPUT_FILE_NAME);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Text :"+inputString);
			
			
			List<Topic> topicList=WikipediaMinerAPI.getTopics(inputString);
			int i=0;
			System.out.println();
			System.err.println("Rearrange the letters to find important keywords from the passage");
			System.out.println();
			for(int j=0;j<topicList.size()&&i<5;j++,i++){
				Topic topic=topicList.get(j);
				System.out.println(getAnagram(topic.topicName));
				System.out.println("Answer :"+topic.topicName.toUpperCase());
			}
			
        }
        
	 
}