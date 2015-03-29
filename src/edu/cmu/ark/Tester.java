package edu.cmu.ark;

import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.trees.tregex.ParseException;

public class Tester {
	 public static void main(String[] args) throws ParseException, IOException{


		 QuestionAsker qa = new QuestionAsker();
		//qa.getQuestionsForSentence("My name is Narain.");
	
		// String[] output=new String[50];
	//	 String output[] =qa.HeadWordResolver("to Narain Sharma and Aditya Suresh Kumar"); //correct
		//String[] output=qa.HeadWordResolver("John, Vishnu, Adi and Sai");//fails for these type of cases. //need NER to resolve these.
	//	 String[] output=qa.HeadWordResolver("an excellent farmer,carpenter or engineer"); //correct 
	//	 String[] output=qa.HeadWordResolver("a teacher, counselor, coach, or after-school program director");//correct
	//	 String [] output=qa.HeadWordResolver("Americans, Germans and French"); //correct
//		 String[] output=qa.HeadWordResolver("America and Germany"); //correct
		// String[] output=qa.HeadWordResolver("to Lincolns and Tonys"); //correct

	//	 String[] output=qa.HeadWordResolver("on the village of Ardekul"); //correct
		 List<String> output=qa.HeadWordResolver("into the adulthood ");
		 
		 System.out.println("\nPossible HeadWords");

		// String[] output=qa.HeadWordResolver("to excellent men"); //correct

		
		
		 System.out.println("\nOutput:");
		 if(output==null)
			 System.out.println("No response");
		 else{
			 for (String word: output) {
				 	if(word!=null)
		            System.out.println(word);
		        }
		}
		
		 //	System.out.println(output[4]);
		

		
	// System.out.println(qa.resolveHead("a teacher, counselor, coach, or after-school program director"));
		 
		// QuestionAsker qa = new QuestionAsker();
		 //System.out.println(qa.resolveHead("The Qayen earthquake"));
//		String t=PorterStemmer.getInstance().stem("kids");
//		System.out.println(t);
	/*	String questionSentence="Is Vishnu a coder ?";
		Character firstChar=questionSentence.charAt(0);
		firstChar=Character.toLowerCase(firstChar);
		StringBuilder qS=new StringBuilder(questionSentence);
		qS.setCharAt(0,firstChar);
		System.out.println("Why "+qS);
*/
	 }
}
