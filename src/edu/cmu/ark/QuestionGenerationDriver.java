package edu.cmu.ark;

import edu.stanford.nlp.trees.tregex.ParseException;

public class QuestionGenerationDriver {
	public static void main(String[] args) {
		  String[] arguments = new String[] {"--debug",
				  "--model",
				  "models/linear-regression-ranker-reg500.ser.gz"};
		  try {
			QuestionAsker.main(arguments);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
