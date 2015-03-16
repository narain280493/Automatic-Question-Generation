package edu.cmu.ark.tests;

import java.util.List;

import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.QuestionTransducer;
import edu.stanford.nlp.trees.Tree;

import junit.framework.TestCase;


public class TestWhPhraseGenerator extends TestCase{
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

    /*
    public void testAddIfAllowedHowLong(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	
    }
    
    
    public void testAddIfAllowedHowFar(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	
    }
    */
    
    
    public void testPartitiveConstruction(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	sentence = "I met some of my friends.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Who did I meet?"));
    	assertFalse(res.toString(),res.contains("What did I meet?"));
    	assertFalse(res.toString(),res.contains("What kind of some did I meet?"));
    	
    	sentence = "I met 55 of the participants.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Who did I meet?"));
    	assertFalse(res.toString(),res.contains("What did I meet?"));
    	
    	sentence = "I met all of the participants.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Who did I meet?"));
    	assertFalse(res.toString(),res.contains("What did I meet?"));
    	assertFalse(res.toString(),res.contains("What kind of all did I meet?"));
    	   	
    	sentence = "I met five percent of the participants.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Who did I meet?"));
    	assertFalse(res.toString(),res.contains("What kind of percent did I meet?"));
    	
    	sentence = "I want to see all of the mountains.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("What do I want to see?"));
    	assertFalse(res.toString(),res.contains("Where do I want to see?"));
    	
    	sentence = "Susan ate some of John's cookies.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Whose cookies did Susan eat some of?"));
    	assertTrue(res.toString(),res.contains("What did Susan eat?"));
    	
    }
    
    
    
    public void testAddIfAllowedWhose(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	

	    
    	sentence = "I like John's car.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Whose car do I like?"));    	
	    
    	sentence = "I like John's new car.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    //assertTrue(res.toString(),res.contains("Whose car do I like?"));
	    assertTrue(res.toString(),res.contains("Whose new car do I like?"));
	    
	    /*sentence = "I like his new car.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    //assertTrue(res.toString(),res.contains("Whose car do I like?"));
	    assertTrue(res.toString(),res.contains("Whose new car do I like?"));
	    
	    sentence = "I like his friend from America.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    //assertTrue(res.toString(),res.contains("Whose friend do I like?"));
	    assertTrue(res.toString(),res.contains("Whose friend from America do I like?"));
	    */
	    
	    sentence = "Cartier's expeditions along the river were fun.";
	    parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Whose expeditions along the river were fun?"));
	    assertFalse(res.toString(),res.contains("Whose along the river were fun?"));
	 
	    sentence = "Cartier's and Tim's expeditions along the river were fun.";
	    parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Whose expeditions along the river were fun?"));
	    assertFalse(res.toString(),res.contains("Whose along the river were fun?"));
	    
    	sentence = "Troma 's and Fox 's rights expired.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Whose rights expired?")); 
    	
    	sentence = "Troma and Fox 's rights expired.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Whose rights expired?"));
	    
	    sentence = "The book's cover was worn.";
	    parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("Whose cover was worn?"));
	    assertTrue(res.toString(),res.contains("What was worn?"));
    }
    
    
    public void testAddIfAllowedHowMany(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	sentence = "I bought one hundred big red cars.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("What did I buy?"));
	    assertTrue(res.toString(),res.contains("How many big red cars did I buy?"));
	    
    	sentence = "I bought one hundred cars that were old.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("What did I buy?"));
	    assertTrue(res.toString(),res.contains("How many cars that were old did I buy?"));
	    
    	sentence = "I bought a car which was worth one hundred dollars.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("How many cars did I buy?"));
	    assertTrue(res.toString(),res.contains("What did I buy?"));
	    
	    sentence = "I bought the 5 cars that were on sale.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("How many the cars that were on sale did I buy?"));
	    assertTrue(res.toString(),res.contains("How many cars that were on sale did I buy?"));
	    
    }
    
    
    
    public void testAddIfAllowedWhat(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	sentence = "I ran.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("What ran?"));
	    
    	sentence = "John likes Mary.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("What likes Mary?"));

    	sentence = "John ran Wednesday.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("What did John run?"));
	    
	    sentence = "John ran because the weather was nice.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("What did John run?"));
	    
	    sentence = "John works for the government.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(), res.contains("What does John work for?"));

	    sentence = "The Grand Prix was held at the TI Circuit , Aida , Japan.";
	    parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Where was the Grand Prix held?"));
	    assertFalse(res.toString(),res.contains("What was the Grand Prix held?"));

    }
    
    
    /*public void testAddIfAllowedWhy(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	   
	    sentence = "John ran because the weather was nice.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Why did John run?"));
    }*/
    
    /*
    public void testAddIfAllowedWhichWhatKindOf(){
    	String sentence;
    	Tree parse;
    	List<String> res;


    	
    	sentence = "The man with the blue coat was very nice.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Which man was very nice?"));
    	assertFalse(res.toString(),res.contains("What kind of man was very nice?"));
    	
    	sentence = "I bought 5 cars.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What kind of cars did I buy?"));

    	sentence = "I bought the 5 red cars.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Which cars did I buy?"));
    	
    	sentence = "I bought 5 red cars.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("What kind of cars did I buy?"));
    	
    	sentence = "I saw a big red dog.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("Which dog did I see?"));
    	assertTrue(res.toString(),res.contains("What kind of dog did I see?"));
    	
    	sentence = "I saw a big red dog.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("Which dog did I see?"));
    	assertTrue(res.toString(),res.contains("What kind of dog did I see?"));

    	sentence = "I saw the dog.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("Which dog did I see?"));
    	   
    	sentence = "That he ran was surprising.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("Which That was surprising?") || res.contains("Which that was surprising?"));

    	sentence = "John likes apples, oranges, and bananas.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What kind of apples does John like?"));
    	
    	sentence = "The improvements are related to additional facilities that have been put online.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What facilities are the improvements related to?") || res.contains("Which facilities are the improvements related to?"));
    	assertTrue(res.toString(),res.contains("What kind of facilities are the improvements related to?"));
        
    	
    	sentence = "President John ran.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What kind of John ran?"));
    	assertFalse(res.toString(),res.contains("What John ran?"));
    	assertTrue(res.toString(),res.contains("Who ran?"));
    	
    	sentence = "The Hall of Mirrors was old.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What kind of Hall was old?"));
    	assertFalse(res.toString(),res.contains("Which Hall was old?"));
    	
    	sentence =  "The Blue Iguana is one of the longest-living species of lizard.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What NN is the Blue Iguana?"));
    	assertFalse(res.toString(),res.contains("What kind of lizard is the Blue Iguana?"));
    	
    	sentence = "Los Angeles became known as the \"Queen of the Cow Counties\" for its role in supplying beef and other foodstuffs to hungry miners in the north.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertFalse(res.toString(),res.contains("What kind of NN did Los Angeles become known as'' for its role in supplying beef and other foodstuffs to hungry miners in the north?"));
    	
    	
    }
    */
    
    public void testAddIfAllowedWhen(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	

	    sentence = "John ran in November.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("When did John run?"));
	    assertFalse(res.toString(),res.contains("When did John run in?"));
	    
	    sentence = "John ran on Wednesday.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("When did John run?"));
	    
	    sentence = "John ran Wednesday.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("When did John run?"));
	    
	    sentence = "John ran yesterday.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("When did John run?"));
	    

    }
    

    
    
    public void testAddIfAllowedWhere(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	sentence = "I drove in my car.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("What did I drive in?"));
	    assertFalse(res.toString(),res.contains("Where did I drive?"));
	    assertFalse(res.toString(),res.contains("Where did I drive in?"));
	    
    	sentence = "I ran in Japan.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Where did I run?"));
	    assertFalse(res.toString(),res.contains("Where did I run in?"));
	    
    	sentence = "I went to Japan.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Where did I go?"));
	    
	    sentence = "I drove by the moonlight.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(), res.contains("Where did I drive?"));
	    
	    sentence = "Chicago is a nice city.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(), res.contains("Where is a nice city?"));
	    
	    sentence = "John likes Japan.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("Where does John like?"));
	    
	    sentence = "John was standing near the door.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("What was John standing near?"));
	    //"Where was John standing?" is hard to get because we can't reliably identify "door" as a location
	    
	    sentence = "John studied on Wednesday.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("Where did John study?"));
    }
    

    
    
  
    
    
    public void testAddIfAllowedPrepositions(){
    	String sentence;
    	Tree parse;
    	List<String> res;
   
    	
    	sentence = "I met him immediately after lunch.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(), res.contains("Immediately what did I meet him?"));
	    assertTrue(res.toString(), res.contains("What did I meet him immediately after?"));
    	
    	sentence = "I gave the book to John.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(), res.contains("Who did I give the book to?")||res.contains("Whom did I give the book to?"));
	    
    	sentence = "He studied for two years.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(), res.contains("What did he study for?"));
	    assertTrue(res.toString(), res.contains("Who studied for two years?"));
	    
    	sentence = "He studied for food.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(), res.contains("What did he study for?"));
	    
	    sentence = "John ran in November.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("When did John run in?"));
	    
	    sentence = "The bond matures over twenty years.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("When does the bond mature over?"));
	    
    	sentence = "John liked Mary because of Bill.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(), res.contains("Who liked Mary because of Bill?"));
	    assertTrue(res.toString(), res.contains("Who did John like because of Bill?"));
	    assertFalse(res.toString(), res.contains("What did John like Mary because?"));
	    assertTrue(res.toString(), res.contains("Who did John like Mary because of?") || res.contains("Whom did John like Mary because of?") || res.contains("What did John like Mary because of?"));
    }
    
   

    
    

    
    
    public void testAddIfAllowedWho(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    	
    	sentence = "John likes Mary.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Who likes Mary?"));
	    assertTrue(res.toString(),res.contains("Who does John like?"));
	    
    	sentence = "John likes dogs.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("Who does John like?"));
    }
    
    
    /*public void testExtractNEs(){
    	String res = AnalysisUtilities.getInstance().annotateSentenceWithSupersenses();
    	assertTrue(res, res.equals("John/PERSON saw/O Bill/PERSON ./O"));
    } */   
    
    
    
    /**
	 * expected to fail.  system doesn't address these issues
	 */
    public void testToughCases(){
    	String sentence;
    	Tree parse;
    	List<String> res;
    		    
	    sentence = "I ran three months ago.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("When did I run?"));
	    sentence = "I will start in a moment.";  //moment is not identified as a date or time
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("When will I start?"));
	    assertFalse(res.toString(),res.contains("In what will I start?"));
	    
	    sentence = "Her father worked as an estate agent.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("As what did her father work?"));
	    assertFalse(res.toString(),res.contains("As whom did her father work?"));
	    
	    sentence = "There were thousands of reasons.";  //thousands is not identified as a number
	    parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertFalse(res.toString(),res.contains("What kind of reasons were there?"));
	    assertFalse(res.toString(),res.contains("Which thousands were there?"));
	    
    	sentence = "I ran to Japan.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(),res.contains("Where run I go to?"));
	    
	    //groups and organizations
	    sentence = "John works for the organization.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
	    assertTrue(res.toString(), res.contains("Who does John work for?")||res.contains("Whom does John work for?"));
	    
	    sentence = "I met part of the family.";
    	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
    	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    	assertTrue(res.toString(),res.contains("Who did I meet?"));
    	assertFalse(res.toString(),res.contains("What kind of part did I meet?"));
    }
}



/*
public void testAddIfAllowedHowMuch(){
	String sentence;
	Tree parse;
	List<String> res;
	
	sentence = "I spent one million dollars.";
	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    assertTrue(res.toString(),res.contains("How much did I spend?"));
    
	sentence = "I bought one hundred cars.";
	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    assertFalse(res.toString(),res.contains("How much did I buy?"));  
    
	sentence = "The dollar rose against the Euro.";
	parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
	res = TestQuestions.getQuestionOutputStringsFromParse(qt, parse);
    assertTrue(res.toString(),res.contains("What rose against the Euro?"));
    assertFalse(res.toString(),res.contains("How much rose against the Euro?"));
}*/

    /*
    public void testExtractFeatures(){
    	Tree answerTree = QuestionTransducer.readTreeFromString("(NP (DET the) (NNS books) (PP (IN on) (NP (DET the) (NN shelf))))");
		gen.setAnswer(answerTree, answerTree.yield().toString());

		gen.findRepresentation("which books");
    	List<Double> fvals = gen.extractFeatures();
    	assertTrue(fvals.toString(), fvals.size()>0);
    }*/

    /*
	public void testFindRepresentation() {
		int replaceStart, replaceEnd, chopStart;
		String replaceToken = null;
		Tree answerTree;
		String qPhrase;
		
		answerTree = QuestionTransducer.readTreeFromString("(NP (NNS books))");
		gen.setAnswer(answerTree, answerTree.yield().toString());
		qPhrase = "what";
		gen.findRepresentation(qPhrase);
		replaceStart = gen.getReplaceStart();
		replaceEnd = gen.getReplaceEnd();
		replaceToken = gen.getReplaceToken();
		chopStart = gen.getChopStart();
		assertTrue(replaceStart == 0);
		assertTrue(replaceEnd== 0);
		assertTrue(chopStart == 1);
		assertTrue(replaceToken != null && replaceToken.equals("what"));
		
		answerTree = QuestionTransducer.readTreeFromString("(NP (DET the) (NNS books))");
		gen.setAnswer(answerTree, answerTree.yield().toString());
		qPhrase = "what";
		gen.findRepresentation(qPhrase);
		replaceStart = gen.getReplaceStart();
		replaceEnd = gen.getReplaceEnd();
		replaceToken = gen.getReplaceToken();
		chopStart = gen.getChopStart();
		assertTrue(replaceStart == 0);
		assertTrue(replaceEnd == 1);
		assertTrue(chopStart == 2);
		assertTrue(replaceToken != null && replaceToken.equals("what"));
		
		answerTree = QuestionTransducer.readTreeFromString("(NP (DET the) (NNS books) (PP (IN on) (NP (DET the) (NN shelf))))");
		gen.setAnswer(answerTree, answerTree.yield().toString());
		qPhrase = "what books";
		gen.findRepresentation(qPhrase);
		replaceStart = gen.getReplaceStart();
		replaceEnd = gen.getReplaceEnd();
		replaceToken = gen.getReplaceToken();
		chopStart = gen.getChopStart();
		assertTrue(replaceStart == 0);
		assertTrue(replaceEnd == 0);
		assertTrue(chopStart == 2);
		assertTrue(replaceToken != null && replaceToken.equals("what"));
		
		answerTree = QuestionTransducer.readTreeFromString("(NP (DET the) (NNS books) (PP (IN on) (NP (DET the) (NN shelf))))");
		gen.setAnswer(answerTree, answerTree.yield().toString());
		qPhrase = "what kind of books";
		gen.findRepresentation(qPhrase);
		replaceStart = gen.getReplaceStart();
		replaceEnd = gen.getReplaceEnd();
		replaceToken = gen.getReplaceToken();
		chopStart = gen.getChopStart();
		assertTrue(replaceStart == 0);
		assertTrue(replaceEnd == 0);
		assertTrue(chopStart == 2);
		assertTrue(replaceToken != null && replaceToken.equals("what kind of"));
		
		answerTree = QuestionTransducer.readTreeFromString("(NP (DET the) (NNS books) (PP (IN on) (NP (DET the) (NN shelf))))");
		gen.setAnswer(answerTree, answerTree.yield().toString());
		qPhrase = "what color books";
		gen.findRepresentation(qPhrase);
		replaceStart = gen.getReplaceStart();
		replaceEnd = gen.getReplaceEnd();
		replaceToken = gen.getReplaceToken();
		chopStart = gen.getChopStart();
		assertTrue(replaceStart == -1);
		assertTrue(replaceEnd == -1);
		assertTrue(chopStart == -1);
		
	}
*/	
  

