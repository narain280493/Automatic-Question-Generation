package edu.cmu.ark;

import java.io.IOException;

import edu.stanford.nlp.trees.tregex.ParseException;

public class QuestionGenerationDriver {
	public static void main(String[] args) throws IOException {
		  String[] arguments = new String[] {"--debug",
				  "--model",
				  "models/linear-regression-ranker-reg500.ser.gz","--flag"};
		  try {
			QuestionAsker.main(arguments);
			System.out.println("Main exited");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
