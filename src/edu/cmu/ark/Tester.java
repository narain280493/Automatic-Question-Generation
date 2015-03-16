package edu.cmu.ark;

import edu.stanford.nlp.trees.tregex.ParseException;

public class Tester {
	 public static void main(String[] args) throws ParseException{

		 QuestionAsker qa = new QuestionAsker();
		 System.out.println(qa.HeadWordResolver("to Abraham Lincoln")); //correct
		//System.out.println(qa.HeadWordResolver("an excellent farmer,carpenter and engineer")); //correct 
		// System.out.println(qa.HeadWordResolver("a teacher, counselor, coach, or after-school program director")); // incorrect
		 //System.out.println(qa.HeadWordResolver("Narain Sharma and Thomas Lincoln")); //correct
	// System.out.println(qa.resolveHead("a teacher, counselor, coach, or after-school program director"));
		 
	 }
}
