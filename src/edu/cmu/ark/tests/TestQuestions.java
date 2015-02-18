package edu.cmu.ark.tests;

import java.util.*;

import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.Question;
import edu.cmu.ark.QuestionTransducer;
import edu.stanford.nlp.trees.Tree;
import junit.framework.TestCase;

public class TestQuestions extends TestCase{
	private QuestionTransducer qt;


	/**
	 * Sets up the test fixture. 
	 * (Called before every test case method.) 
	 */ 
	protected void setUp() { 
		qt = new QuestionTransducer();
		GlobalProperties.setDebug(true);
	} 

	/**
	 * Tears down the test fixture. 
	 * (Called after every test case method.) 
	 */ 
	protected void tearDown() { 
		qt = null; 
	} 
	
	
	public void testUnicodeCleanup(){
		String tmp = "Martín likes enchiladas.";
		tmp = AnalysisUtilities.preprocess(tmp);
		assertTrue(tmp.length() == 24);
		assertTrue(tmp.equals("Martin likes enchiladas."));
	}
	
	
	public void testComplementsInPPs(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("Putin was president of Russia").parse;
		List<String> res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who was president of Russia?"));
		assertTrue(res.toString(),res.contains("What was Putin president of?") || res.contains("Where was Putin president of?"));
		assertTrue(res.toString(),res.contains("Who was Putin?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("The president of Russia was Putin").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who was Putin?"));
		assertFalse(res.toString(),res.contains("What was the president of Putin?") || res.contains("Where was the president of Putin?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("The capital of Alaska was visited by John.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What was visited by John?"));
		assertFalse(res.toString(),res.contains("What was the capital of visited by John?") || res.contains("Where was the capital of visited by John?"));
		
		//parser doesn't produce the right parse with "in Alaska" attaching to "city"
		parse = AnalysisUtilities.getInstance().readTreeFromString("(ROOT (S (NP (NNP John)) (VP (VBD visited) (NP (NP (DT a) (NN city)) (PP (IN in) (NP (NNP Alaska))))) (. .)))");
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What did John visit a city in?") || res.contains("Where did John visit a city in?"));
		assertTrue(res.toString(),res.contains("What did John visit?") || res.contains("Where did John visit?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("James saw John in the hall of mirrors.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did James see John in?"));
		assertFalse(res.toString(),res.contains("What did James see John in the hall of?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John read a book about linguistics.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did John read?"));
		assertTrue(res.toString(),res.contains("What did John read a book about?"));
	}
	
	
	public void testAuxiliariesAndVerbDecomposition() { 
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I walked.").parse;
		List<String> res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==2));
		assertTrue(res.toString(),res.contains("Who walked?"));
		assertTrue(res.toString(),res.contains("Did I walk?"));
		

		
		parse = AnalysisUtilities.getInstance().parseSentence("I may have walked.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==2));
		assertTrue(res.toString(),res.contains("Who may have walked?"));
		assertTrue(res.toString(),res.contains("May I have walked?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("I did walk.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==2));
		assertTrue(res.toString(),res.contains("Who did walk?"));
		assertTrue(res.toString(),res.contains("Did I walk?"));

		parse = AnalysisUtilities.getInstance().parseSentence("I have walked.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==2));
		assertTrue(res.toString(),res.contains("Who has walked?"));
		assertTrue(res.toString(),res.contains("Have I walked?"));

		parse = AnalysisUtilities.getInstance().parseSentence("I have been walking.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==2));
		assertTrue(res.toString(),res.contains("Who has been walking?"));
		assertTrue(res.toString(),res.contains("Have I been walking?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("I had a book.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did I have?"));
		assertTrue(res.toString(),res.contains("Did I have a book?"));
		assertFalse(res.toString(),res.contains("Had I a book?"));
		assertFalse(res.toString(),res.contains("I had a book?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("I did a bad thing.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did I do?"));
		assertTrue(res.toString(),res.contains("Did I do a bad thing?"));
		assertFalse(res.toString(),res.contains("Did I a bad thing?"));
		assertFalse(res.toString(),res.contains("I did a bad thing?"));
	} 
	
	
	public void testVerbalComplements(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("John knows that James plays baseball.").parse;
		List<String> res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What does John know?"));
		assertTrue(res.toString(),res.contains("Who knows that James plays baseball?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John will play when Mary comes.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What will John play?"));
		assertTrue(res.toString(),res.contains("Who will play when Mary comes?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John will play what Mary chooses.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What will John play?"));
		assertTrue(res.toString(),res.contains("Who will play what Mary chooses?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John will play whatever Mary chooses.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What will John play?"));
		assertTrue(res.toString(),res.contains("Who will play whatever Mary chooses?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John will play even if Mary does not come.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What will John play?"));
		assertTrue(res.toString(),res.contains("Who will play even if Mary does not come?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John is hopeful that James will play.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What is John hopeful?"));
		assertTrue(res.toString(),res.contains("Who is hopeful that James will play?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("James knows how to win.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What does James know?"));
		assertTrue(res.toString(),res.contains("Who knows how to win?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("James knows how much money the car is worth.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What does James know?"));
		assertTrue(res.toString(),res.contains("Who knows how much money the car is worth?"));
	}
	
	
	public void testMainVerbCopula(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("He was president.").parse;
		List<String> res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who was president?"));
		assertTrue(res.toString(),res.contains("What was he?") || res.contains("Who was he?"));
		assertTrue(res.toString(),res.contains("Was he president?"));
		
		
		parse = AnalysisUtilities.getInstance().parseSentence("My favorite activity is to run in the park.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What is my favorite activity to run in?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("My favorite activity seems to be to run in the park.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What does my favorite activity seem to be to run in?"));
		
	}
	

	
	
	public void testSeem(){
		String sentence;
		Tree parse;
		List<String> res;

		sentence = "The player seemed finished with his turn.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("Seemed the player finished with his turn?"));
		assertTrue(res.toString(),res.contains("Did the player seem finished with his turn?"));
		
	}
	
	
	
	
	
	
	
	public void testPunctuationWithQuotations(){
		String sentence;
		Tree parse;
		List<String> res;
		
		sentence = "John said, ``Mary likes Bob.''";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		
		//not going to deal with placing the q mark outside or inside the quotation marks as appropriate...
		assertTrue(res.toString(),res.contains("Who said, ``Mary likes Bob''?") || res.contains("Who said, ``Mary likes Bob?''"));
		assertTrue(res.toString(),res.contains("Did John say, ``Mary likes Bob''?") || res.contains("Did John say, ``Mary likes Bob?''"));
		
		assertFalse(res.toString(),res.contains("Who ``likes Bob?''"));
		assertFalse(res.toString(),res.contains("Who ``likes Bob''?"));
		assertFalse(res.toString(),res.contains("Does ``Mary like Bob?''"));
		assertFalse(res.toString(),res.contains("Does ``Mary like Bob''?"));
		assertFalse(res.toString(),res.contains("Who does ``Mary like?''"));
		assertFalse(res.toString(),res.contains("Who does ``Mary like''?"));
		assertFalse(res.toString(),res.contains("Who does, ``Mary like?''"));
		
	}
	
	

	
	
	public void testVerbAgreement() {
		String sentence;
		Tree parse;
		List<String> res;
		
		sentence = "Dogs chase cars.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What chase cars?"));
		assertFalse(res.toString(),res.contains("What chases cars?"));
		
		sentence = "Dogs are animals.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What are dogs?"));
		assertTrue(res.toString(),res.contains("What are animals?"));
		assertFalse(res.toString(),res.contains("What is dogs?"));
		assertFalse(res.toString(),res.contains("What is animals?"));
	}
	
	





	public void testWonderWho(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I wonder who he likes.").parse;
		List<String> res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==3));
		assertTrue(res.toString(),res.contains("Who wonders who he likes?"));
		assertTrue(res.toString(),res.contains("What do I wonder?"));
		assertTrue(res.toString(),res.contains("Do I wonder who he likes?"));
	}


	
	

	
	
	public void testAdjunctClausesFollowingCommasAreIslands() {
		String sentence;
		Tree parse;
		List<String> res;
		
		sentence = "I went to work, barely catching the bus.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What did I go to work, barely catching?"));
		
		sentence = "I went to the building carrying a backpack.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did I go to the building carrying?"));
		assertTrue(res.toString(),res.contains("What did I go to carrying a backpack?"));
	}
	
	
	public void testPassiveDitransitiveRecipientIsNotIsland(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I was given a book by her.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==4));
		//assertTrue(res.toString(),res.contains("What was interesting?"));
		assertTrue(res.toString(),res.contains("Was I given a book by her?"));
		assertTrue(res.toString(),res.contains("What was I given by her?"));
		assertTrue(res.toString(),res.contains("Whom was I given a book by?") || res.contains("Who was I given a book by?"));
		assertTrue(res.toString(),res.contains("Who was given a book by her?"));
	}
	
	
	public void testWithTelescope() { 
		Tree parse = AnalysisUtilities.getInstance().parseSentence("He saw her with a telescope.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==4));
		assertTrue(res.toString(),res.contains("Who saw her with a telescope?"));
		assertTrue(res.toString(),res.contains("Who did he see with a telescope?"));
		assertTrue(res.toString(),res.contains("What did he see her with?"));
		assertTrue(res.toString(),res.contains("Did he see her with a telescope?"));
	}
	

	public void testPreprocessing(){
		String res;

		res = AnalysisUtilities.preprocessTreeString("(ROOT (S (NP (EX There)) (VP (VBZ 's) (NP (DT a) (NN dog)))))");
		assertTrue(res.toString(),res.equals("(ROOT (S (NP (EX There)) (VP (VBZ is) (NP (DT a) (NN dog)))))"));

		res = AnalysisUtilities.preprocessTreeString("(ROOT (S (NP (PRP we)) (VP (VBD 'd) (VP (VBN had) (NP (DT all) (NN morning))))))");
		assertTrue(res.toString(),res.equals("(ROOT (S (NP (PRP we)) (VP (VBD had) (VP (VBN had) (NP (DT all) (NN morning))))))"));

		res = AnalysisUtilities.preprocessTreeString("(ROOT (S (NP (PRP we)) (VP (VBD 'd) (VP (VBN had) (NP (DT all) (NN morning))))))");
		assertTrue(res.toString(),res.equals("(ROOT (S (NP (PRP we)) (VP (VBD had) (VP (VBN had) (NP (DT all) (NN morning))))))"));

		res = AnalysisUtilities.preprocessTreeString("(ROOT (S (NP (PRP he)) (VP (MD 'd) (VP (VB play)))))");
		assertTrue(res.toString(),res.equals("(ROOT (S (NP (PRP he)) (VP (MD would) (VP (VB play)))))"));

		res = AnalysisUtilities.preprocessTreeString("(NP (NP (NNP John) (POS 's)) (NN team))");
		assertTrue(res.toString(),res.equals("(NP (NP (NNP John) (POS 's)) (NN team))"));

		res = AnalysisUtilities.preprocessTreeString("(ROOT (S (NP (PRP It)) (VP (VBZ does) (RB n't) (VP (VB replace) (S (VP (VBG pitching)))))))");
		assertTrue(res.toString(),res.equals("(ROOT (S (NP (PRP It)) (VP (VBZ does) (RB not) (VP (VB replace) (S (VP (VBG pitching)))))))"));

		res = AnalysisUtilities.preprocessTreeString("(VP (MD ca) (RB n't) (VP (VB cut) (NP (PRP it))");
		assertTrue(res.toString(),res.equals("(VP (MD can) (RB not) (VP (VB cut) (NP (PRP it))"));
	}


	/*public void testAvoidPronounsInComplementPhrases(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("John says he will study.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who says he will study?"));
		assertFalse(res.toString(),res.contains("Who does John say will study?"));
	}*/
	
	public void testAvoidDemonstratives(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("John liked that book.").parse;
		List<String> res;
		qt.setAvoidPronounsAndDemonstratives(true);
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did John like?"));
		assertFalse(res.toString(),res.contains("Who liked that book?"));
	}


	public void testExistentialThere() {
		String sentence = "There were thousands of reasons.";
		Tree parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse; 
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("Were there thousands of reasons?"));
		assertFalse(res.toString(),res.contains("What were there?"));
		assertFalse(res.toString(), res.contains("What were thousands of reasons?"));
	} 


	public void testPuntOnTwoNPs(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("He last week issued a report.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==2));
		assertTrue(res.toString(),res.contains("What did he last week issue?"));
		assertTrue(res.toString(),res.contains("Did he last week issue a report?"));
	}


	


	public void testComplementsVersusAdjuncts() {
		String sentence;
		Tree parse;
		List<String> res;

		sentence = "He knew that he would win the race.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What did he know that he would win?"));

		sentence = "He ran so he would win the race.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What did he run so he would win?"));

		sentence = "He discovered the theory since she wrote the book.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("What did he discover the theory since she wrote?"));

		parse = AnalysisUtilities.getInstance().parseSentence("He believed John saw a miracle.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who believed John saw a miracle?"));
		assertTrue(res.toString(),res.contains("Who did he believe saw a miracle?"));
		assertTrue(res.toString(),res.contains("What did he believe John saw?"));
		assertTrue(res.toString(),res.contains("Did he believe John saw a miracle?"));

		parse = AnalysisUtilities.getInstance().parseSentence("He believed that John saw a miracle.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who believed that John saw a miracle?"));
		assertTrue(res.toString(),res.contains("What did he believe that John saw?"));
		assertTrue(res.toString(),res.contains("Did he believe that John saw a miracle?"));
	} 


	public void testActiveDitransitiveRecipientIsIsland(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("He gave her a book.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		//assertTrue(res.toString(),res.contains("What was interesting?"));
		assertTrue(res.toString(),res.contains("Did he give her a book?"));
		assertFalse(res.toString(),res.contains("Who did he give a book?"));
	}



	public void testParsing(){
		Tree res = AnalysisUtilities.getInstance().parseSentence("I walked.").parse;
		String parseStr = res.toString();
		assertTrue(parseStr, parseStr.equals("(ROOT (S (NP (PRP I)) (VP (VBD walked)) (. .)))"));
	}




	public void testDontTreatSententialComplementsAsIslands(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I told you that he left the book.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==5));
		//assertTrue(res.toString(),res.contains("What was interesting?"));
		assertTrue(res.toString(),res.contains("Who told you that he left the book?"));
		assertTrue(res.toString(),res.contains("Who did I tell that he left the book?"));
		assertTrue(res.toString(),res.contains("What did I tell you that he left?"));
		assertTrue(res.toString(),res.contains("Did I tell you that he left the book?"));
		assertTrue(res.toString(),res.contains("What did I tell you?"));
	}

	public void testSubjectOfSententialComplementsCanMoveWhenNoComplementizerPresent(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I knew John read the book.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==5));
		//assertTrue(res.toString(),res.contains("What was interesting?"));
		assertTrue(res.toString(),res.contains("Who knew John read the book?"));
		assertTrue(res.toString(),res.contains("Who did I know read the book?"));
		assertTrue(res.toString(),res.contains("What did I know John read?"));
		assertTrue(res.toString(),res.contains("Did I know John read the book?"));
		assertTrue(res.toString(),res.contains("What did I know?"));
	}

	public void testSubjectOfSententialComplementsCantMoveWhenComplementizerPresent(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I knew that John read the book.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),(res.size()==4));
		//assertTrue(res.toString(),res.contains("What was interesting?"));
		assertTrue(res.toString(),res.contains("Who knew that John read the book?"));
		assertTrue(res.toString(),res.contains("What did I know that John read?"));
		assertTrue(res.toString(),res.contains("Did I know that John read the book?"));
		assertTrue(res.toString(),res.contains("What did I know?"));
	}



	public void testTreatRelativeClausesAsIslands(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I bought the book that he saw.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who bought the book that he saw?"));
		assertTrue(res.toString(),res.contains("What did I buy?") || res.contains("What did I buy that he saw?"));
		assertTrue(res.toString(),res.contains("Did I buy the book that he saw?"));
	}


	public void testDeepNesting() {
		String sentence = "He felt that John thought that Mary said that Harry argued that Susan liked the new car.";
		Tree parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who felt that John thought that Mary said that Harry argued that Susan liked the new car?"));
		assertTrue(res.toString(),res.contains("What did he feel that John thought that Mary said that Harry argued that Susan liked?"));
		assertTrue(res.toString(),res.contains("Did he feel that John thought that Mary said that Harry argued that Susan liked the new car?"));
	} 


	public void testObjectOfPreposition(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("He ran in the park.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who ran in the park?"));
		assertTrue(res.toString(),res.contains("In what did he run?") || res.contains("Where did he run?"));
		assertTrue(res.toString(),res.contains("Did he run in the park?"));
	}

	protected static List<String> getQuestionOutputStringsFromParse(QuestionTransducer qt, Tree parse) {
		List<String> res;
		qt.generateQuestionsFromParse(parse);
		res = new ArrayList<String>();
		for(Question q: qt.getQuestions()){
			res.add(q.yield());
		}
		return res;
	}



	public void testSubjectIsIsland(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("That he ran was surprising.").parse;
		List<String> res;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What was surprising?"));
		assertTrue(res.toString(),res.contains("Was that he ran surprising?"));
		assertFalse(res.toString(),res.contains("Who was that ran surprising?") || res.contains("Who was That ran surprising?"));
	}

	public void testFormatting(){
		Tree parse;
		List<String> res;

		parse = AnalysisUtilities.getInstance().parseSentence("That man bought a car.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who bought a car?"));
		assertTrue(res.toString(),res.contains("What did that man buy?"));

		parse = AnalysisUtilities.getInstance().parseSentence("I like cars.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who likes cars?"));
		assertTrue(res.toString(),res.contains("What do I like?"));

		//capitalization test for proper nouns at the start of the sentence 
		parse = AnalysisUtilities.getInstance().parseSentence("Rivers likes cars.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What does Rivers like?"));
		
		//capitalization test for proper nouns at the start of the sentence 
		parse = AnalysisUtilities.getInstance().parseSentence("Supporters followed Castro.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("Who did Supporters follow?"));
		assertTrue(res.toString(),res.contains("Who did supporters follow?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("I won $500.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who won $500?"));

		
		parse = AnalysisUtilities.getInstance().parseSentence("John lives in Pittsburgh, PA.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Who lives in Pittsburgh, PA.?") || res.contains("Who lives in Pittsburgh, PA?"));
	}

	
	/**
	 * This test is not important since this operation is never used in the system
	 * Leading modifiers are instead addressed by stage 1.
	 * 
	 */
	public void testMoveLeadingModifiers() {
		String sentence;
		Tree parse;
		List<String> res;

		sentence = "While I was a student, I studied math.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("While I was a student, what did I study?"));
		assertTrue(res.toString(),res.contains("While I was a student, who studied math?"));
		assertTrue(res.toString(),res.contains("While I was a student, did I study math?"));
		

		parse = AnalysisUtilities.getInstance().parseSentence("However, quickly, John ran.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("However, quickly, who ran?"));
		assertTrue(res.toString(),res.contains("However, quickly, did John run?"));
		assertFalse(res.toString(),res.contains("However, who quickly ran?"));
		assertFalse(res.toString(),res.contains("Quickly, however who ran?"));
		assertFalse(res.toString(),res.contains("However, who quickly, ran?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("Fewer in numbers , the Red Kangaroo occupies deserts.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Fewer in numbers, does the Red Kangaroo occupy deserts?"));
		assertTrue(res.toString(),res.contains("Fewer in numbers, what does the Red Kangaroo occupy?"));
		

		sentence = "In January, John will meet Mary.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("In January, who will John meet?"));
		assertTrue(res.toString(),res.contains("In January, who will meet Mary?"));
		assertTrue(res.toString(),res.contains("In January, will John meet Mary?"));

		parse = AnalysisUtilities.getInstance().parseSentence("During school, John rarely studied.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("During school, rarely who studied?"));
		assertFalse(res.toString(),res.contains("Who during school, rarely studied?"));
		assertTrue(res.toString(),res.contains("During school, who rarely studied?"));

		parse = AnalysisUtilities.getInstance().parseSentence("While in school, John rarely studied.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("While in school, rarely who studied?"));
		assertTrue(res.toString(),res.contains("While in school, who rarely studied?"));
		assertTrue(res.toString(),res.contains("While in school, did John rarely study?"));

		parse = AnalysisUtilities.getInstance().parseSentence("John rarely studied.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertFalse(res.toString(),res.contains("Rarely who studied?"));
		assertTrue(res.toString(),res.contains("Who rarely studied?"));

		parse = AnalysisUtilities.getInstance().parseSentence("Separately, John studies.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Separately, who studies?"));
		assertTrue(res.toString(),res.contains("Separately, does John study?"));
		assertTrue(res.toString(),res.contains("Separately, does John study?"));
		assertFalse(res.toString(),res.contains("Who separately, studies?"));
		assertFalse(res.toString(),res.contains("Who, separately studies?"));
		


		parse = AnalysisUtilities.getInstance().parseSentence("If I had money, I would buy a car.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("If I had money, what would I buy?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("John, while running, saw James.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("While running, who saw James?"));
		assertTrue(res.toString(),res.contains("While running, who did John see?"));
		assertTrue(res.toString(),res.contains("While running, did John see James?"));
		
		
		//make sure things work without commas
		sentence = "While I was a student I studied math.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("While I was a student what did I study?"));
		assertTrue(res.toString(),res.contains("While I was a student who studied math?"));
		assertTrue(res.toString(),res.contains("While I was a student did I study math?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("If I had money I would buy a car.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("If I had money what would I buy?"));
		
		parse = AnalysisUtilities.getInstance().parseSentence("Separately John studies.").parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("Separately who studies?"));
		assertFalse(res.toString(),res.contains("Who separately studies?"));
		assertFalse(res.toString(),res.contains("Who separately studies?"));
		

		
	}
	
	
	
    /**
	 * expected to fail.  system doesn't address these issues
	 */
	public void testToughCases(){
		String sentence;
		Tree parse;
		List<String> res;

		sentence = "While I was a student, that I studied surprised me.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		//assertTrue(res.toString(),res.contains("While I was a student, what surprised me?"));
		assertTrue(res.toString(),res.contains("While I was a student, who did that I studied surprise?"));
		assertTrue(res.toString(),res.contains("While I was a student, did that I studied surprise me?"));
		
		sentence = "My favorite activity is to run in the park.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = getQuestionOutputStringsFromParse(qt, parse);
		assertTrue(res.toString(),res.contains("What is my favorite activity?"));
		
		
	}
	

}


/*
    public void testDuration() {
    	qt.clearEntityMap();
    	String sentence = "I studied for four years.";
    	String sentence2 = qt.extractNamedEntities(sentence);
    	Tree parse = AnalysisInfrastructure.getInstance().parseSentence(sentence2);
    	List<String> res = qt.getQuestionsFromParse(parse);
    	assertTrue(res.toString(),(res.size()==3));
    	assertTrue(res.toString(),res.contains("Who studied for four years?"));
    	assertTrue(res.toString(),res.contains("For how long did I study?"));
    	assertTrue(res.toString(),res.contains("Did I study for for years?"));
    } 
 */


//this parse is wrong!
/*public void testAfterReadingThrough(){
    	//John was able to understand the question after reading through the book one more time.
    	qt.clearEntityMap();
    	qt.putEntityString("PERSON-0", "John");
    	List<String> res = qt.getQuestionsFromParse("(ROOT (S (NP (NNP PERSON-0)) (VP (VBD was) (ADJP (JJ able) (S (VP (TO to) (VP (VB understand) (NP (DT the) (NN question)) (PP (IN after) (S (VP (VBG reading) (PP (IN through) (NP (DT the) (NN book)))))))))) (NP (ADVP (NP (CD one)) (JJR more)) (NN time))) (. .)))");
    	assertTrue(res.toString(),(res.size()==2));
    	assertTrue(res.toString(),res.contains("Who was able to understand the question after reading through the book one more time?"));
    	//assertTrue(res.toString(),res.contains("What was John able to understand after reading through the book one more time?"));
    	assertTrue(res.toString(),res.contains("Was John able to understand the question after reading through the book one more time?"));
    }*/


/*
    public void testAdverbPhrase(){
    	//John ran as quickly as a deer.
    	qt.clearEntityMap();
    	qt.putEntityString("PERSON-0", "John");
    	List<String> res = qt.getQuestionsFromParse("(ROOT (S (NP (NNP PERSON-0)) (VP (VBD ran) (ADVP (RB as) (RB quickly)) (PP (IN as) (NP (DT a) (NN deer)))) (. .)))");
    	assertTrue(res.toString(),(res.size()==3));
    	assertTrue(res.toString(),res.contains("Who ran as quickly as a deer?"));
    	assertTrue(res.toString(),res.contains("What did John run as quickly as?"));
    	assertTrue(res.toString(),res.contains("Did John run as quickly as a deer?"));
    }
 */

//  public void testMoveLeadingModifiers() {
//	Tree parse = AnalysisInfrastructure.getInstance().parseSentence("He likes her, I said.").parse;
//	List<String> res = qt.getQuestionsFromParse(parse);
//	assertTrue(res.toString(),(res.size()==2));
//	assertTrue(res.toString(),res.contains("Who said, he likes her?"));
//	assertTrue(res.toString(),res.contains("Did I say, he likes her?"));
//} 

//  public void testTreatPhrasalModifiersAsIslands() {
//	Tree parse = AnalysisInfrastructure.getInstance().parseSentence("Across the country, he was opening new stores.").parse;
//	List<String> res = qt.getQuestionsFromParse(parse);
//	assertTrue(res.toString(),(res.size()==3));
//	assertTrue(res.toString(),res.contains("Who was opening new stores, across the country?"));
//	assertTrue(res.toString(),res.contains("What was he opening, across the country?"));
//	assertTrue(res.toString(),res.contains("Was he opening new stores, across the country?"));
//} 


/*
public void testDontTransformSentencesWithLeadingModifiers() {
	Tree parse = AnalysisUtilities.getInstance().parseSentence("He likes her, I said.").parse;
	List<String> res;
	qt.generateQuestionsFromParse(parse);
	res = qt.getQuestions();
	assertTrue(res.toString(),(res.size()==0));
	//assertTrue(res.toString(),res.contains("Who said, he likes her?"));
	//assertTrue(res.toString(),res.contains("Did I say, he likes her?"));
}*/ 


