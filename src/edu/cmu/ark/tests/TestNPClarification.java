

package edu.cmu.ark.tests;

import edu.cmu.ark.*;

import java.io.File;
import java.util.*;

import arkref.analysis.ARKref;
import junit.framework.TestCase;
import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.Question;
import edu.cmu.ark.QuestionTransducer;
import edu.stanford.nlp.trees.*;

public class TestNPClarification extends TestCase{
	private NPClarification npc;


	/**
	 * Sets up the test fixture. 
	 * (Called before every test case method.) 
	 */ 
	protected void setUp() { 
		ARKref.Opts.propertiesFile = "config"+File.separator+"QuestionTransducer.properties";
		npc = new NPClarification();
		GlobalProperties.setDebug(true);
	} 

	/**
	 * Tears down the test fixture. 
	 * (Called after every test case method.) 
	 */ 
	protected void tearDown() { 
		npc = null; 
	} 



	private boolean setContainsIntermediateTreeWithYield(Set<Question> questions, String yield){
		for(Question q: questions){
			String tmp = AnalysisUtilities.getCleanedUpYield(q.getIntermediateTree());
			if(tmp.equalsIgnoreCase(yield)){
				return true;
			}
		}
		return false;
	}


	private boolean setContainsTreeWithYield(Set<Question> questions, String yield){
		for(Question q: questions){
			String tmp = AnalysisUtilities.getCleanedUpYield(q.getTree());
			if(tmp.equalsIgnoreCase(yield)){
				return true;
			}
		}
		return false;
	}

	private boolean listContainsTreeWithYield(List<Question> trees, String yield){
		Set<Question> set = new HashSet<Question>();
		set.addAll(trees);
		return this.setContainsTreeWithYield(set, yield);
	}
	
	public List<Tree> parseDoc(String doc){
		List<String> sents = AnalysisUtilities.getSentences(doc);
		List<Tree> res = new ArrayList<Tree>();
		
		for(int i=0; i<sents.size(); i++){
			String s = sents.get(i);
			Tree t = AnalysisUtilities.getInstance().parseSentence(s).parse;
			res.add(t);
		}
		
		return res;
	}
	
	public List<Question> makeQuestions(List<Tree> doc){
		List<Question> res = new ArrayList<Question>();
		
		for(int i=0; i<doc.size(); i++){
			Question q = new Question();
			q.setSourceSentenceNumber(i);
			q.setSourceTree(doc.get(i));
			q.setIntermediateTree(doc.get(i));
			res.add(q);
		}
		
		return res;
	}
	
	
	
	
	
	
	
	
	public void testMentionSimplification(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;
		
		sentence = "John, the walker, walked.  He liked walking.  The swimmer, Susan, swam.  She liked swimming.  Bill, who was a runner, ran.  He liked running.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Susan liked swimming."));
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "John liked walking."));
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Bill liked running."));
		
		sentence = "John (1901-1982), a leader, was old.  He liked movies. Bob, a friend, was old.  He liked cars.  Mark (1900-1979) was old.  He liked books.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.clear();
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "John liked movies."));
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Bob liked cars."));
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Mark liked books."));
		
		sentence = "Connie Talbot (born 20 November 2000) is an English child singer from Streetly.  Talbot likes singing.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.clear();
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Connie Talbot likes singing."));
		
	}
	
	
	public void testNesting() {
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;
		
		sentence = "The settlers began constructing earthen burial sites and fortifications around the time of 600 BC.  Some mounds from that time are in the shape of birds or serpents. They probably served religious purposes.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Some mounds from the time of 600 BC are in the shape of birds or serpents."));
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "Some mounds from the time of 600 BC probably served religious purposes."));
		assertFalse(res.toString(), setContainsIntermediateTreeWithYield(res, "Some mounds from that time probably served religious purposes."));
		
		
		//make sure the source is not modified
		for(Question q: res){
			if(q.getIntermediateTree().yield().toString().equals("Some mounds from the time of 600 BC probably served religious purposes.")){
				assertTrue(q.toString(), q.getSourceTree().yield().equals("They probably served religious purposes."));
			}
		}
		
	}
	
	public void testDontReplaceWithIdenticalMention(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		List<Tree> doc;
		
		sentence = "John liked international service.  International service was fun.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		npc.resolveCoreference(doc);
		List<Question> clarified = npc.clarifyNPs(questions, true, true);
		assertTrue(clarified.toString(), clarified.size()==0);
		
	
	}
	
	
	public void testAvoidDuplicates(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;
		
		sentence = "John ran.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), res.size()==1);
	}
	
	
	public void testPossessives1() {
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;
		
		sentence = "John ran.  His feet hurt.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "John's feet hurt."));
		assertFalse(res.toString(), setContainsIntermediateTreeWithYield(res, "John feet hurt."));
		

	}
	
	
	public void testPossessives2() {
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;
		
		sentence = "John's feet hurt.  He bought new shoes.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));
		
		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "John bought new shoes."));
		assertFalse(res.toString(), setContainsIntermediateTreeWithYield(res, "John 's bought new shoes."));

	}
	
	
	public void testOnlyReplaceFirstMention() {
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;
		
		sentence = "John thought he could win the race.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));

		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "John thought he could win the race."));
		assertFalse(res.toString(), setContainsIntermediateTreeWithYield(res, "John thought John could win the race."));
		
		sentence = "John likes movies.  He watches them often. He believed he could win.";
		doc = parseDoc(sentence);
		questions = makeQuestions(doc);
		
		res.addAll(questions);
		npc.resolveCoreference(doc);
		res.addAll(npc.clarifyNPs(questions, true, true));

		assertTrue(res.toString(), setContainsIntermediateTreeWithYield(res, "John believed he could win."));
		assertFalse(res.toString(), setContainsIntermediateTreeWithYield(res, "John believed John could win."));
	}

	
	public void testQGWithTransformations(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		
		sentence = "The pet turtle, which is large, is a sea turtle.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Is the pet turtle a sea turtle?"));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "What is a sea turtle?"));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "What is the pet turtle?"));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Is the pet turtle the pet turtle?"));
	}
	
	
	public void testQGPronouns(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		
		qt.setAvoidPronounsAndDemonstratives(true);
		
		sentence = "He was a prince, but had no children of his own.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Did he have no children of his own?"));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Was he a prince?"));
		
		
		sentence = "The brown book was old.  He liked the book.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Did he like the brown book?"));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Did he like the book?"));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Was the brown book old?"));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "What was old?"));
	}
	
	
	public void testQGPronounsWithNesting(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		
		qt.setAvoidPronounsAndDemonstratives(true);
		
		sentence = "Her car was old.  The car still ran.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Did her car still run?"));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "What ran?") || setContainsTreeWithYield(res, "What still ran?"));
		

	}
	
	
	
	public void testQuestionGenerationJustPronouns(){
		String sentence;
		//ARKref.Opts.debug = true;
		List<Question> questions = new ArrayList<Question>();
		List<Question> res = new ArrayList<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		
		//FIRST: test that all the mentions are replaced when NPC is fully enabled 
		s1.setDoPronounNPC(true);
		s1.setDoNonPronounNPC(true);
		qt.setAvoidPronounsAndDemonstratives(true);
		
		res.clear();
		sentence = "Talking birds are rare.  Mary gave John a book that was about birds.  He liked the book.  John kept it.  ";
		doc = parseDoc(sentence);
		questions = s1.transform(doc);
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What did John like?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who liked the book?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who kept a book that was about talking birds?")); 
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who liked a book that was about talking birds?"));
		
		//SECOND: test that non-pronoun mentions are not replaced when that feature is disabled
		s1.setDoPronounNPC(true);
		s1.setDoNonPronounNPC(false);
		qt.setAvoidPronounsAndDemonstratives(true);
		
		res.clear();
		questions = s1.transform(doc);
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What did John like?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who liked the book?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who kept a book that was about birds?")); 
		assertFalse(listContainsTreeWithYield(res, "Who liked a book that was about birds?"));
		assertFalse(res.toString(), listContainsTreeWithYield(res, "Who kept a book that was about talking birds?"));
		

	}
	
	
	public void testQuestionGenerationNoNPC(){
		String sentence;
		//ARKref.Opts.debug = true;
		List<Question> questions = new ArrayList<Question>();
		List<Question> res = new ArrayList<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		s1.setDoPronounNPC(false);
		s1.setDoNonPronounNPC(false);
		qt.setAvoidPronounsAndDemonstratives(true);
		
		res.clear();
		sentence = "Mary gave John a book that was about birds.  He liked the book.  John kept it.";
		doc = parseDoc(sentence);
		questions = s1.transform(doc);
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertFalse(res.toString(), listContainsTreeWithYield(res, "What did he like?"));
		assertFalse(res.toString(), listContainsTreeWithYield(res, "What did he keep?"));
		assertFalse(res.toString(), listContainsTreeWithYield(res, "What did John like?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What did John keep?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who gave John a book that was about birds?"));
	}
	

	public void testQuestionGeneration(){
		String sentence;
		//ARKref.Opts.debug = true;
		List<Question> questions = new ArrayList<Question>();
		List<Question> res = new ArrayList<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		s1.setDoPronounNPC(true);
		s1.setDoNonPronounNPC(true);
		qt.setAvoidPronounsAndDemonstratives(true);
		
		res.clear();
		sentence = "John ran.  Because of the rain, his clothes were wet.";
		doc = parseDoc(sentence);
		questions = s1.transform(doc);
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Were John's clothes wet?") || listContainsTreeWithYield(res, "Were John's clothes wet because of the rain?"));
		
		
		res.clear();
		sentence = "Julius Caesar invaded Britain. He set out from Gaul and was hampered by the weather while crossing the English Channel.";
		doc = parseDoc(sentence);
		questions = s1.transform(doc);
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Did Julius Caesar set out from Gaul?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What was Julius Caesar hampered by while crossing the English Channel?"));
		
		sentence = "John likes movies.  He watches them often. He believed he could win.  James likes cars, but he also likes trucks. Sam thinks he likes books.  She likes cats.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		/*for(Question q: res){
			System.err.println("Q:"+q.getTree().yield().toString());
		}*/
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who believed he could win?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Did John believe he could win?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Does John watch movies often?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who also likes trucks?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who likes cars?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who likes movies?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who watches movies often?"));
		assertFalse(res.toString(), listContainsTreeWithYield(res, "What does she like?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What does Sam think he likes?"));
		//assertTrue(res.toString(), listContainsTreeWithYield(res, "Could John win?"));
		

		sentence = "John Smith likes dogs.  Smith also likes cats.  He owns birds.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		

		
		QuestionTransducer.removeDuplicateQuestions(res);
		
		//for(Question q: res){
		//	System.out.println(q.getFeatureValue("performedNPClarification")+"\t"+q.yield() );
		//}
		
		assertFalse(res.toString(), listContainsTreeWithYield(res, "Does he like birds?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Does John Smith own birds?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who owns birds?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What does John Smith own?"));
		
		for(Question q: res){
			
			if(q.yield().equals("Who owns birds?")){
				assertTrue(q.yield(), q.getFeatureValue("performedNPClarification") == 0.0);
			}
			if(q.yield().equals("Does John Smith own birds?")){
				assertTrue(q.yield(), q.getFeatureValue("performedNPClarification") == 1.0);
			}
			if(Question.getFeatureNames().contains("numPronounsReplacedMentions") && q.yield().equals("What does John Smith own?")){
				assertTrue(q.toString(), q.getFeatureValue("numPronounsReplacedMentions") == 1.0);
			}
			
			
		}
	}
	
	/*
	public void testNPCInAnswerOnly(){
		String sentence;

		List<Question> simplified = new ArrayList<Question>();
		List<Question> res = new ArrayList<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		
		sentence = "The cat ran away.  John ran after it.";
		res.clear();
		doc = parseDoc(sentence);
		simplified = s1.transform(doc);		
		for(Question q: simplified){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		
		QuestionTransducer.removeDuplicateQuestions(res);
		
		assertTrue(res.toString(), listContainsTreeWithYield(res, "Who ran after the cat?"));
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What did John run after?"));


		boolean found = false;
		for(Question q: res){
			if(AnalysisUtilities.getCleanedUpYield(q.getTree()).equalsIgnoreCase("What did John run after?")){
				if(q.getAnswerPhraseTree().yield().toString().contains("the cat")){
					assertTrue(res.toString(), q.getFeatureValue("performedNPClarification") == 0.0);
					assertTrue(res.toString(), q.getFeatureValue("performedNPClarificationAnswerOnly") == 1.0);
					found = true;
				}
			}
		}
		assertTrue(res.toString(), found);
		
		
		sentence = "John ran after it.";
		res.clear();
		doc = parseDoc(sentence);
		simplified = s1.transform(doc);		
		for(Question q: simplified){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		
		QuestionTransducer.removeDuplicateQuestions(res);
		QuestionRanker.adjustScores(res, doc, true, true, true, true);
		
		assertTrue(res.toString(), listContainsTreeWithYield(res, "What did John run after?"));
		
		found = false;
		for(Question q: res){
			if(AnalysisUtilities.getCleanedUpYield(q.getTree()).equalsIgnoreCase("What did John run after?")){
				if(q.getAnswerPhraseTree().yield().toString().contains("it")){
					assertTrue(res.toString(), q.getFeatureValue("answerIsHeadedByPronoun") == 1.0);
					found = true;
				}
			}
		}
		assertTrue(res.toString(), found);
	}
	*/

	
    /**
	 * expected to fail.  system doesn't address these issues
	 */
	public void testToughCases(){
		String sentence;

		List<Question> questions = new ArrayList<Question>();
		Set<Question> res = new HashSet<Question>();
		List<Tree> doc;

		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep s1 = new InitialTransformationStep();
		
		sentence = "John practiced hard. He believed he could win.";
		res.clear();
		doc = parseDoc(sentence);
		questions = s1.transform(doc);		
		for(Question q: questions){
			qt.generateQuestionsFromParse(q);
			res.addAll(qt.getQuestions());
		}
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Who believed he could win?"));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Did John believe he could win?"));
		
		//this last one currently fails because it doesn't
		//think that the second he is the first mention in the sentence.
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Could John win?"));
	}
	
	
	
	
	
}



