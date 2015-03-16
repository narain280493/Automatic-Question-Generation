package edu.cmu.ark.tests;

import junit.framework.TestCase;


import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.SpecificityAnalyzer;
import edu.stanford.nlp.trees.Tree;

public class TestSpecificityAnalyzer extends TestCase{

	protected void setUp(){
		sa = SpecificityAnalyzer.getInstance();
	}
	
	protected void tearDown() { 
    	sa = null;
    } 
	
	public void testPronouns() {
		Tree parse = AnalysisUtilities.getInstance().parseSentence("I had met her when that happened.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumPronouns(), sa.getNumPronouns()==3);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==3);
		
		parse = AnalysisUtilities.getInstance().parseSentence("These are new.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumPronouns(), sa.getNumPronouns()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
	}
	
	
	public void testVagueNPs() {
		Tree parse = AnalysisUtilities.getInstance().parseSentence("Those men went to the store.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumPronouns(), sa.getNumPronouns()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==2);
		
		parse = AnalysisUtilities.getInstance().parseSentence("The man went to the store in that town.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==2);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
		
		parse = AnalysisUtilities.getInstance().parseSentence("John Smith went to the bookstore that was built in the city.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
		assertTrue(parse.toString()+"\t"+sa.getNumNPsWithProperNouns(), sa.getNumNPsWithProperNouns()==1);
		
		parse = AnalysisUtilities.getInstance().parseSentence("John Smith went to the bookstore that was built in New York.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
		assertTrue(parse.toString()+"\t"+sa.getNumNPsWithProperNouns(), sa.getNumNPsWithProperNouns()==2);
		
		parse = AnalysisUtilities.getInstance().parseSentence("A big red dog barked.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPsWithProperNouns(), sa.getNumNPsWithProperNouns()==0);
		
		parse = AnalysisUtilities.getInstance().parseSentence("They strove for independence.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==2);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
		
		parse = AnalysisUtilities.getInstance().parseSentence("John went to the other.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
		
		parse = AnalysisUtilities.getInstance().parseSentence("John bought those.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
	}
	
	
	public void testComplexNPs(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("The best pictures available at the moment are from John Smith.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
		
		parse = AnalysisUtilities.getInstance().parseSentence("It was the ghost of the Soviet Brigade discovered in Cuba.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==4);

		parse = AnalysisUtilities.getInstance().parseSentence("I read the paper which he promised us in New York.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==3);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==5);
		assertTrue(parse.toString()+"\t"+sa.getNumPronouns(), sa.getNumPronouns()==3);
	}
	
	
	public void testCrossSentenceReferences(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("Jones was stationed at another command in Colorado.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumNPsWithProperNouns()==2);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumReferenceWords()==1);
		
		parse = AnalysisUtilities.getInstance().parseSentence("The other boy ran.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumNPsWithProperNouns()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumReferenceWords()==1);
	}
	
	
	public void testDates(){
		Tree parse = AnalysisUtilities.getInstance().parseSentence("The incident happened in 1974.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
		
		parse = AnalysisUtilities.getInstance().parseSentence("The incident happened in December, 1974.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
	
		parse = AnalysisUtilities.getInstance().parseSentence("The incident happened in the 1970s.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
	}
	
	
	public void testPossession() {
		Tree parse = AnalysisUtilities.getInstance().parseSentence("John's friend ran.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumPronouns(), sa.getNumPronouns()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumNPsWithProperNouns(), sa.getNumNPsWithProperNouns()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
		
		parse = AnalysisUtilities.getInstance().parseSentence("The man's friend ran.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumPronouns(), sa.getNumPronouns()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPsWithProperNouns(), sa.getNumNPsWithProperNouns()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);
	}
	
	public void testConjunctions() {
		Tree parse = AnalysisUtilities.getInstance().parseSentence("Smith commands the Navy's Indian Ocean and Persian Gulf forces.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
	}
	

	
    /**
	 * expected to fail.  system doesn't address these issues
	 */
	public void testToughCases(){
		//"the world" is specific enough by itself
		Tree parse = AnalysisUtilities.getInstance().parseSentence("The largest animal in the world is the Blue Whale.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==0);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==3);
		
		parse = AnalysisUtilities.getInstance().parseSentence("The 60s was a tumultuous decade.").parse;
		sa.analyze(parse);
		assertTrue(parse.toString()+"\t"+sa.getNumVagueNPs(), sa.getNumVagueNPs()==1);
		assertTrue(parse.toString()+"\t"+sa.getNumNPs(), sa.getNumNPs()==2);	
		

		
	}
	
	
	
	private SpecificityAnalyzer sa;

}


