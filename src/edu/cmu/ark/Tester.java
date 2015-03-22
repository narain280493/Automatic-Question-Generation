package edu.cmu.ark;

import edu.stanford.nlp.trees.tregex.ParseException;

public class Tester {
	 public static void main(String[] args) throws ParseException{


		 QuestionAsker qa = new QuestionAsker();
		qa.getQuestionsForSentence("My name is Narain.");
		 
		// String[] output=new String[50];
		// String output =qa.HeadWordResolver("to Abraham Lincoln"); //correct
		//String[] output=qa.HeadWordResolver("John, Vishnu and Adi");//fails for these type of cases. //need NER to resolve these.
	//	 String[] output=qa.HeadWordResolver("an excellent farmer,carpenter or engineer"); //correct 
	//	 String[] output=qa.HeadWordResolver("a teacher, counselor, coach, or after-school program director");//correct
	//	 String [] output=qa.HeadWordResolver("Americans, Germans and French"); //correct
//		 String[] output=qa.HeadWordResolver("America and Germany"); //correct
		// String[] output=qa.HeadWordResolver("to Lincolns and Tonys"); //correct
		// String[] output=qa.HeadWordResolver("to excellent men"); //correct
	
		 
		 /*String [] output=qa.HeadWordResolver("from Narain Sharma and Vishnu Jayvel");
		 System.out.println("\nOutput:");
		 for (String word: output) {
			 	if(word!=null)
	            System.out.println(word);
	        }*/
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
