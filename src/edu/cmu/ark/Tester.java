package edu.cmu.ark;

import edu.stanford.nlp.trees.tregex.ParseException;

public class Tester {
	 public static void main(String[] args) throws ParseException{



		// QuestionAsker qa = new QuestionAsker();
		 //System.out.println(qa.resolveHead("The Qayen earthquake"));
//		String t=PorterStemmer.getInstance().stem("kids");
//		System.out.println(t);
		String questionSentence="Is Vishnu a coder ?";
		Character firstChar=questionSentence.charAt(0);
		firstChar=Character.toLowerCase(firstChar);
		StringBuilder qS=new StringBuilder(questionSentence);
		qS.setCharAt(0,firstChar);
		System.out.println("Why "+qS);

	 }
}
