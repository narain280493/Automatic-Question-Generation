package edu.cmu.ark;

import edu.stanford.nlp.trees.tregex.ParseException;

public class Tester {
	 public static void main(String[] args) throws ParseException{


		 QuestionAsker qa = new QuestionAsker();
		// System.out.println(qa.HeadWordResolver("to Abraham Lincoln")); //correct
		//System.out.println(qa.HeadWordResolver("an excellent farmer,carpenter or engineer")); //correct 
		// System.out.println(qa.HeadWordResolver("a teacher, counselor, coach, or after-school program director")); // incorrect
		// System.out.println(qa.HeadWordResolver("Narain Sharma and Thomas Lincoln")); //correct
		 //System.out.println(qa.HeadWordResolver(" America and Germany")); //correct
		// System.out.println(qa.HeadWordResolver("to Lincolns and Tonys"));
	//	 System.out.println(qa.HeadWordResolver("to excellent men"));
		 //System.out.println(qa.HeadWordResolver(""));
		// String[] output=new String[50];
		String[] output=qa.HeadWordResolver("The point I'm trying to make is that when you're asleep, this thing doesn't shut down.");
		System.out.println("\nOutput:");
		 for (String word: output) {
			 	if(word!=null)
	            System.out.println(word);
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
