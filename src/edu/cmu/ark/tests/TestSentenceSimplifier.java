// Question Generation via Overgenerating Transformations and Ranking
// Copyright (c) 2010 Carnegie Mellon University.  All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Michael Heilman
//	  Carnegie Mellon University
//	  mheilman@cmu.edu
//	  http://www.cs.cmu.edu/~mheilman

package edu.cmu.ark.tests;

import java.util.*;
import junit.framework.TestCase;
import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.Question;
import edu.cmu.ark.SentenceSimplifier;
import edu.stanford.nlp.trees.*;

public class TestSentenceSimplifier extends TestCase{
	private SentenceSimplifier simp;


	/**
	 * Sets up the test fixture. 
	 * (Called before every test case method.) 
	 */ 
	protected void setUp() { 
		simp = new SentenceSimplifier();
		simp.setBreakNPs(true);
		simp.setExtractFromVerbComplements(true);
		GlobalProperties.setDebug(true);
	} 

	/**
	 * Tears down the test fixture. 
	 * (Called after every test case method.) 
	 */ 
	protected void tearDown() { 
		simp = null; 
	} 



	private boolean setContainsTreeWithYield(Collection<Question> questions, String yield){
		for(Question q: questions){
			String tmp = AnalysisUtilities.getCleanedUpYield(q.getIntermediateTree());
			if(tmp.equals(yield)){
				return true;
			}
		}
		return false;
	}


	

	public void testQuotations(){
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "Officials said the indictment was `` on hold , '' but did not elaborate .";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Officials said the indictment was ``on hold.''") || setContainsTreeWithYield(res, "Officials said the indictment was ``on hold''."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Officials said the indictment was ``on hold."));
		
		sentence = "This is it, John said.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said this is it."));
		
		sentence = "``This is it,'' John said.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said, ``This is it.''") || setContainsTreeWithYield(res, "John said, ``This is it''."));
		
		sentence = "``This is it,'' said John.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said, ``This is it.''") || setContainsTreeWithYield(res, "John said, ``This is it''."));
		
		sentence = "John said, ``This is it.''";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said, ``This is it.''") || setContainsTreeWithYield(res, "John said, ``This is it''."));
		
		
		sentence = "In 1978, ``This is it,'' John said.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said in 1978, ``This is it.''") || setContainsTreeWithYield(res, "John said in 1978, ``This is it''."));
	
		sentence = "Officials said the initial results were positive but that \"more analysis is required.\"";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Officials said the initial results were positive."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Officials said that ``more analysis is required.''"));
	}
	
	
	public void testMoveLeadingPPs(){
		String sentence;
		Tree parse;
		Collection<Question> res;
		
		//modifies predicate (PP)
		sentence = "In January, snow fell.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Snow fell in January."));
		
		sentence = "In January snow fell.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Snow fell in January."));
		
		sentence = "In Kansas, snow fell.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Snow fell in Kansas."));
		
		sentence = "On Tuesday , John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran on Tuesday."));
		
		sentence = "Hardly anywhere is the bedrock exposed on the surface.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Hardly anywhere is the bedrock exposed on the surface."));
		
		sentence = "Because of the nice weather , John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran because of the nice weather."));
		

		
		//modifies predicate (S < (VP < TO))
		/*sentence = "To become an engineer, John went to school.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John went to school to become an engineer."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John went to school."));
		
		//modifies predicate (SBAR < (S !< NP))
		sentence = "While walking to the store, John saw Mary.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John saw Mary."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John saw Mary while walking to the store."));

		
		//don't want to generate from these (SBAR < (S < NP))
		sentence = "While I had many reasons, I could not think of one at the time.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I could not think of one at the time while I had many reasons."));
		

		//modifies subj (NP)
		sentence = "A baseball fan, John went to the game.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was a baseball fan."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John went to the game."));
		
		
		//modifies subj (S < VP !<NP)
		sentence = "Being a baseball fan, John went to the game.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was a baseball fan."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John went to the game."));
		*/

		
	}
	
	public void testMoveQuotes() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "This is it, John said.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said this is it."));
		
		sentence = "``This is it,'' John said.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John said, ``This is it.''") || setContainsTreeWithYield(res, "John said, ``This is it''."));
		
		sentence = "`` She stood up to the Soviets and let them know she meant business , '' he said .";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "He said, ``She stood up to the Soviets and let them know she meant business''."));
		
	}
	
	
	public void testExtractNounParticipials() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		//don't generate unless there is a comma
		sentence = "This was the blue car bought by John.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The blue car was bought by John."));
		
		sentence = "This was the book, written thousands of years ago.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The book was written thousands of years ago."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The book is written thousands of years ago."));
		
		sentence = "It was a blue car, bought by John.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The blue car was bought by John."));
		
		sentence = "A rare bird, known as a blue wren, lives in Australia.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The rare bird is known as a blue wren."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "A rare bird lives in Australia."));
		
		sentence = "The bird, known as a blue wren, lives in Australia.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bird is known as a blue wren."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bird lives in Australia."));
		
		sentence = "The bird, known as a blue wren, lived in Australia.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bird was known as a blue wren."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bird lived in Australia."));
		
		sentence = "Being an unabashed carnivore, John liked steak.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was being an unabashed carnivore.") || setContainsTreeWithYield(res, "John was an unabashed carnivore."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John liked steak."));
		
		sentence = "Walking to the store, John saw Susan.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John walked to the store."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John saw Susan."));
			
		sentence = "While walking to the store, John saw Susan.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John walked to the store."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John saw Susan."));
		
		sentence = "While walking to the store, John did not see Susan.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John walked to the store."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John walks to the store."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John is walking to the store."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John did not see Susan."));
		
		sentence = "Founded by Bill Gates, Microsoft made money.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Microsoft made money."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Microsoft was founded by Bill Gates."));
		
		sentence = "His car giving him trouble, John walked.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John walked."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John was giving him trouble."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John gave him trouble."));
		
	}
	
	
	


	public void testDropAdjuncts() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "Though officially designated as a tropical storm , the intensity of Vamei is disputed.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The intensity of Vamei is disputed."));

		sentence = "To help expedite the construction, the state of Utah forwarded funds to Arizona.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The state of Utah forwarded funds to Arizona."));

		sentence = "The cyclone hit the eastern and western coastlines with powerful waves , wrecking 25 fishing boats.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The cyclone hit the eastern and western coastlines with powerful waves."));

		sentence = "The activists marched through Washington, calling for an end to the policy.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The activists marched through Washington."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "The activists called for an end to the policy."));

		
		//don't want to drop arguments.
		sentence = "I like meeting people.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I like meeting."));
	
		sentence = "The cyclone hit the coastlines with powerful waves , despite our best efforts.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The cyclone hit the coastlines with powerful waves."));
		
	}







	

	public void testSplitConjoinedVPs() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "John ran in the yard and walked in the park.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran in the yard."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John walked in the park."));

		sentence = "John ran in the yard or walked in the park.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John ran in the yard."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John walked in the park."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran in the yard or walked in the park."));
		
		sentence = "John would not go.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John would not go."));
		assertTrue(res.toString(), res.size()==1);
		
		sentence = "John ran, jumped, and played.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John played."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John jumped."));

		sentence = "John ran and played.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John played."));
		
		sentence = "John may or may not play.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John may."));
		
		
		sentence = "John ran in the yard, jumped in the house, and played in the park.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran in the yard."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John played in the park."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John jumped in the house."));

		sentence = "John is liked by Mary but hated by Bob.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John is liked by Mary."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John is hated by Bob."));
		
		sentence = "John bought a car, thinking he might need it soon.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John thinking he might need it soon."));
	
		sentence = "Bob runs, and walks with Mary and Susan.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob runs."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob walks with Mary."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob walks with Susan."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob walks with Mary and Susan."));
	}


	
	public void testSplitConjoinedSBARs() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "John told me that he likes dogs but that he does not like cats.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John told me that he likes dogs."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John told me that he does not like cats."));

		sentence = "John told me that he likes dogs, but that he does not like cats.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John told me that he likes dogs."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John told me that he does not like cats."));


	}
	


	public void testSplitConjoinedFiniteClauses() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		
		sentence = "Bill sold a book, and John bought a picture.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a picture."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bill sold a book."));

		sentence = "John bought a book and Mary sold a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary sold a hat."));

		sentence = "John bought a book or Mary sold a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary sold a hat."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book or Mary sold a hat."));
		
		sentence = "Either John bought a book or Mary sold a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary sold a hat."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Either John bought a book or Mary sold a hat."));
		
		sentence = "John bought a book, and Mary sold a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary sold a hat."));
		
		sentence = "John bought a book; Mary sold a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary sold a hat."));
		
	}



	public void testRemoveAppositives() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "Lincoln, the 16th president, was tall.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Lincoln was tall."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Lincoln was the 16th president."));
	}
	
	
	public void testRemoveParentheticals() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "John, for example, won the match.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John won the match."));
		
		sentence = "John, in top form, won the match.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John won the match."));
		
		sentence = "John (a friend) won the match.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John won the match."));
	}



	
	public void testExtractVerbParticipials() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "John paused, wishing he had brought a raincoat, which would have kept him dry.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John paused."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "A raincoat would have kept him dry.") || setContainsTreeWithYield(res, "The raincoat would have kept him dry."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "He had brought a raincoat."));
		
		sentence = "John paused, wishing that he had an umbrella.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John paused."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John wished that he had an umbrella."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "He had an umbrella."));
		
		sentence = "John paused, wishing that he had an umbrella or a raincoat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John paused."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John wished that he had an umbrella or a raincoat."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "He had a raincoat."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "He had an umbrella."));
		
		sentence = "John did not wait, wishing to arrive on time.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John did not wait."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John wished to arrive on time."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John wishes to arrive on time."));

		//verb participial modifiers where the main verb is not past tense
		sentence = "John hurries, wishing to arrive on time.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John wishes to arrive on time."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John hurries."));
		
		sentence = "John ran, being a busy person.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was a busy person."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		
		sentence = "John runs, being a busy person.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John is a busy person."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John runs."));
		
		//verb participial modifiers where the main verb is not past tense
		sentence = "I ran, being a busy person.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I was a busy person."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I ran."));
		
		sentence = "I run, being a busy person.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I am a busy person."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I run."));
	}
	
	
	
	
	public void testExtractAppositives() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "Lincoln, the 16th president, was tall.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Lincoln was the 16th president."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "The 16th president was tall."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Lincoln was tall."));
		
		sentence = "I saw Bob, the manager.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob was the manager."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob is the manager."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I saw Bob."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "I saw the manager."));
		
		sentence = "I saw Bob, John, and Susan.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I saw Bob, John, and Susan.") || setContainsTreeWithYield(res, "I saw Bob."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob was John."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob was John and Susan."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob was Susan."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "I saw the manager."));
		
		sentence = "I saw a book, a dog, and a cat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "A book was a dog."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "A book was a dog and a cat."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "A book was a dog, and a cat."));
		
		sentence = "John supports Bob, the current president.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob is the current president."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob was the current president."));
		
		sentence = "Savoie, 38, a Tennessee native and a naturalized Japanese citizen, allegedly kidnapped his children.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Savoie was 38."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Savoie was a Tennessee native."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Savoie was a naturalized Japanese citizen."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Savoie allegedly kidnapped his children.") || setContainsTreeWithYield(res, "Savoie kidnapped his children."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "38 was a naturalized Japanese citizen."));
		
		sentence = "While Bob, the president, was meeting with me, Susan called.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob was the president."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob is the president."));
		
		sentence = "The meeting, in 1984, was important.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The meeting was in 1984."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The meeting was important."));
		
		sentence = "\"The book is on the table,\" said John, a friend of mine.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was a friend of mine."));
		
		sentence = "John lives in town, not far from where he believes Susan, his friend, lives.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John lives in town."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Susan is his friend."));
		
		//appositives in NPs with other modifiers
		sentence = "The man from Texas, John, walks.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The man from Texas walks."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The man from Texas is John."));
	}




	public void testExtractFiniteSubordinateClauses() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "While I was a student, I studied math.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I studied math."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I was a student."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "I studied math while I was a student.") || setContainsTreeWithYield(res, "While I was a student, I studied math."));
		
		sentence = "If I liked math, I would study.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I would study."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I liked math."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "If I liked math, I would study."));
		
		sentence = "When the letter came, John was happy.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was happy."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The letter came."));
		
		sentence = "Being an avid marathoner , John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Being an avid marathoner."));
		
		sentence = "Bill wanted Susan to like Peter.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bill wanted Susan to like Peter."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Susan to like Peter."));
		
		sentence = "Bill was suprised about Susan liking Peter.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bill was suprised about Susan liking Peter."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Susan liking Peter."));
	}




	public void testRemoveMainClauseLevelModifiers() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "However , quickly , John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "However John ran."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Quickly John ran."));

		sentence = "John, however, ran quickly.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran quickly."));

		sentence = "John allegedly walked to the store.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John allegedly walked to the store.") || setContainsTreeWithYield(res, "John walked to the store."));
		
		sentence = "Then John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));

		sentence = "Being an avid marathoner , John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));

		sentence = "While I walked , John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		
		sentence = "In 1609 Smith returned to England, and in his absence, the colony descended into anarchy.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "."));
		
		sentence = "Today, John ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran."));
		
	}




	public void testExtractFromNonRestrictiveRelativeClauses() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		//dont extract from restrictive relative clauses
		sentence = "I bought the blue car which I liked.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I liked the blue car."));

		sentence = "I saw a man whom I knew.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I knew a man."));
		
		sentence = "The car that James bought was new.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "James bought."));
		
		sentence = "The blue car, which I liked, was in the shop.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I liked the blue car."));

		sentence = "I saw a man, whom I think that I know.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I think that I know the man.") || setContainsTreeWithYield(res, "I think that I know a man."));
		
		sentence = "I saw the man, whom I think I know.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I think I know the man."));
		
		sentence = "I saw the man from Texas, whom I think walks.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I think the man from Texas walks."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I saw the man from Texas."));
		assertTrue(res.toString(), res.size()==2);
		
		sentence = "I saw the man, whom I think likes walking.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I think the man likes walking."));
		
		sentence = "I saw the man, whom I think knows me.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I think the man knows me."));
		
		sentence = "While the blue car, which I bought, was in the shop, I studied";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I bought the blue car."));

		sentence = "I gave him the book, which I read in an afternoon.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I read the book in an afternoon."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I gave him the book."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I read in an afternoon the book."));

		sentence = "I gave him the book, which I read quickly in an afternoon.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I read the book quickly in an afternoon."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I gave him the book."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I read quickly in an afternoon the book."));

		
		
		sentence = "The cliff, where I saw the bird, was tall.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The cliff was tall."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I saw the bird at the cliff."));
		
		sentence = "The pool, in which the fish swims, is deep.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The pool is deep."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The fish swims in the pool."));
		
		sentence = "The pool, where the fish swims, is deep.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The pool is deep."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The fish swims at the pool."));
		

		sentence = "Walter was sent to Oxford, where he was taught a great many things.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Walter was sent to Oxford."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "He was taught a great many things at Oxford."));
		
		sentence = "John, whose car was in the shop, walked.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John walked."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John's car was in the shop."));

	}





	public void testIncludeOriginalSentence() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "I am.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I am."));
	}
	
	
	
	
	public void testOptionForBreakingNPs() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		simp.setBreakNPs(false);
		sentence = "John and Mary played baseball and soccer, but they did not play hockey.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John and Mary played baseball and soccer."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John played baseball."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "They did not play hockey."));
		
		sentence = "John and I are friends.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John is friends."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I am friends."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John and I are friends."));
		simp.setBreakNPs(true);
	}
	
	public void testOptionForExtractingWithinComplements() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		simp.setExtractFromVerbComplements(false);
		sentence = "John knew that Mary, his friend, would win.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Mary would win."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary would win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary was his friend."));
		
		sentence = "John played, knowing that Mary, his friend, would win.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Mary would win."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary would win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary was his friend."));
		
		sentence = "John lost, while Mary won.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John lost."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary won."));
		
		sentence = "John knew that Mary could win and that Bob could lose.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Bob could lose."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob could lose."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary could win."));
		
		sentence = "John knew that Mary could win and Bob could lose.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Bob could lose."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win and Bob could lose."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob could lose."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary could win."));
		
		//ENABLE!
		simp.setExtractFromVerbComplements(true);
		
		sentence = "John knew that Mary could win and Bob could lose.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Bob could lose."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win and Bob could lose."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob could lose."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary could win."));
		
		sentence = "John knew that Mary could win and that Bob could lose.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John knew that Bob could lose."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob could lose."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary could win."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John knew that Mary could win and that Bob could lose."));
	}
	

	
	public void testNoDuplicates(){
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "In 1979, John, a friend of mine, ran.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		String expectedOutput = "John was a friend of mine .";
		int count = 0;
		for(Question q: res){
			if(q.getIntermediateTree().yield().toString().equals(expectedOutput)){
				count++;
			}
		}
		assertTrue(""+count, count == 1);
	}
	
	
	public void testSplitConjoinedSubjectNPs() {
		String sentence;
		Tree parse;
		Collection<Question> res;
		
		sentence = "John and Mary like Bill.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John likes Bill."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary likes Bill."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Mary like Bill."));
		
		sentence = "John and I like Bill.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John likes Bill."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "I like Bill."));
			

		
		sentence = "Books and magazines are made of paper.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Books are made of paper."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Magazines are made of paper."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Magazines is made of paper."));

		sentence = "A book and magazines were on the desk.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "A book was on the desk."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Magazines were on the desk."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Magazines was on the desk."));
		
		sentence = "A book and a magazine sit on the desk.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "A book sits on the desk."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "A magazine sits on the desk."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "A magazine sit on the desk."));
		
		sentence = "John practiced before he won the game and the tournament.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John practiced before he won the game and the tournament."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "He won the tournament."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "He won the game."));
	}
	
	
	public void testSplitConjoinedNPs() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "John bought a book, a hat, and a picture.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a picture."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a hat."));
		
		sentence =  "John understood life and what it means.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John understood life."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John understood what it means."));
		
		sentence = "John bought a book and a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a hat."));
		
		sentence = "John bought a book or a hat.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John bought a book."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John bought a hat."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a book or a hat."));

		sentence = "John bought books and hats.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought books."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought hats."));
		
		sentence = "John likes pancakes for breakfast and dinner.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John likes pancakes for breakfast."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John likes pancakes for dinner."));
	
		sentence = "I met John, a friend of Susan and Jill, at the party.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was a friend of Susan."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John was a friend of Jill."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John was a friend of Susan and Jill."));
		
		sentence = "We honor the courage and resilience of the Iraqi people.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "We honor the courage of the Iraqi people."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "We honor the resilience of the Iraqi people."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "We honor the courage and resilience of the Iraqi people."));
		
		//sentence = "John bought a book as well as a hat."; //doesn't parse this right ("as well as..." is labeled an ADVP)
		//parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		//res = simp.simplify(parse);
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a red book."));
		//assertTrue(res.toString(), setContainsTreeWithYield(res, "John bought a green hat."));

		sentence = "John, Mary, and Bob played soccer, baseball, football, and tennis.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John played soccer."));
		assertTrue(res.toString(), res.size() == 12);
		assertTrue(simp.getNumSimplifyHelperCalls()+"", simp.getNumSimplifyHelperCalls() <= 17);
		
		sentence = "John, Mary, and Bob played in the park and ran in the yard.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John played in the park."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Mary ran in the yard."));
		assertTrue(res.toString(), res.size() == 6);
		assertTrue(simp.getNumSimplifyHelperCalls()+"", simp.getNumSimplifyHelperCalls() <= 9);
	
		sentence = "The yellow and blue book is on the table.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The yellow book is on the table."));
		
	}
	

	public void testMobyDick() {
		Tree parse;
		Collection<Question> res;

		String parseStr = "(ROOT (S (SBAR (IN As) (S (NP (PRP they)) (VP (VBD narrated) (PP (TO to) (NP (DT each) (NN other))) (NP (NP (PRP$ their) (JJ unholy) (NNS adventures)) (, ,) (NP (NP (PRP$ their) (NNS tales)) (PP (IN of) (NP (NN terror))) (VP (VBN told) (PP (IN in) (NP (NNS words) (PP (IN of) (NP (NN mirth))))))))))) (: ;) (SBAR (IN as) (S (NP (PRP$ their) (JJ uncivilized) (NN laughter)) (VP (VBD forked) (ADVP (RB upwards)) (PP (IN out) (IN of) (NP (PRP them))) (, ,) (PP (IN like) (NP (NP (DT the) (NNS flames)) (PP (IN from) (NP (DT the) (NN furnace)))))))) (: ;) (SBAR (IN as) (S (ADVP (TO to) (CC and) (RB fro)) (, ,) (PP (IN in) (NP (PRP$ their) (NN front))) (, ,) (NP (DT the) (NNS harpooneers)) (VP (ADVP (RB wildly)) (VBD gesticulated) (PP (IN with) (NP (PRP$ their) (JJ huge) (NP (JJ pronged) (NNS forks)) (CC and) (NNS dippers)))))) (: ;) (SBAR (IN as) (S (S (NP (DT the) (NN wind)) (VP (VBD howled) (PRT (RP on)))) (, ,) (CC and) (S (NP (DT the) (NN sea)) (VP (VBD leaped))) (, ,) (CC and) (S (NP (DT the) (NN ship)) (VP (VP (VBD groaned) (CC and) (VBD dived)) (, ,) (CC and) (RB yet) (VP (VP (ADVP (RB steadfastly)) (VBD shot) (NP (PRP$ her) (JJ red) (NN hell)) (ADVP (RBR further) (CC and) (RBR further)) (PP (IN into) (NP (NP (DT the) (NN blackness)) (PP (IN of) (NP (NP (DT the) (NN sea)) (CC and) (NP (DT the) (NN night))))))) (, ,) (CC and) (VP (ADVP (RB scornfully)) (VBD champed) (NP (NP (DT the) (JJ white) (NN bone)) (PP (IN in) (PRP$ her) (NP (NN mouth))))) (, ,) (CC and) (VP (ADVP (RB viciously)) (VBD spat) (PP (IN round) (NP (PRP her))) (PP (IN on) (NP (DT all) (NNS sides))))))))) (: ;) (ADVP (RB then)) (NP (NP (DT the) (JJ rushing) (NNP Pequod)) (, ,) (VP (VP (VBN freighted) (PP (IN with) (NP (NN savages)))) (, ,) (CC and) (VP (VBN laden) (PP (IN with) (NP (NN fire)))) (, ,) (CC and) (VP (VBG burning) (NP (DT a) (NN corpse))) (, ,) (CC and) (VP (VBG plunging) (PP (IN into) (NP (NP (DT that) (NN blackness)) (PP (IN of) (NP (NN darkness))))))) (, ,)) (VP (VBD seemed) (NP (NP (DT the) (JJ material) (NN counterpart)) (PP (IN of) (NP (NP (PRP$ her) (JJ monomaniac) (NN commander) (POS 's)) (NN soul))))) (. .)))";
		parse = AnalysisUtilities.getInstance().readTreeFromString(parseStr);
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The rushing Pequod was freighted with savages."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The rushing Pequod was laden with fire."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The rushing Pequod was burning a corpse."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "They narrated to each other their unholy adventures."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Their unholy adventures were their tales of terror told in words of mirth."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The wind howled on."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The sea leaped."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The ship groaned."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The ship dived."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The rushing Pequod was plunging into that blackness of darkness."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The harpooneers wildly gesticulated with huge pronged forks in their front.")
				|| setContainsTreeWithYield(res, "The harpooneers wildly gesticulated with huge pronged forks to and fro in their front."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The harpooneers wildly gesticulated with huge dippers in their front.")
				|| setContainsTreeWithYield(res, "The harpooneers wildly gesticulated with huge dippers to and fro in their front."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The ship steadfastly shot her red hell further into the blackness of the sea.")  
				|| setContainsTreeWithYield(res, "The ship steadfastly shot her red hell further and further into the blackness of the sea."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The ship steadfastly shot her red hell further into the blackness of the night.")
				|| setContainsTreeWithYield(res, "The ship steadfastly shot her red hell further and further into the blackness of the night."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The ship scornfully champed the white bone in her mouth."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The ship viciously spat round her on all sides."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Their uncivilized laughter forked upwards out of them, like the flames from the furnace.")
				|| setContainsTreeWithYield(res, "Their uncivilized laughter forked upwards out of them."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The rushing Pequod seemed the material counterpart of her monomaniac commander's soul."));
		
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The ship groaned and dived."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The rushing Pequod was freighted with savages, and laden with fire, and burning a corpse, and plunging into that blackness of darkness."));
		
		assertTrue(""+res.size(), res.size() == 18);
		assertTrue(""+simp.getNumSimplifyHelperCalls(), simp.getNumSimplifyHelperCalls() == 25);
	}
	
	

	/**
	 * expected to fail.  system doesn't split these conjunctions
	 */
	public void testSplitADJPs() {
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "The book is yellow and old.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The book is yellow."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The book is old."));
		
		sentence = "The book is very yellow and extremely old.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The book is very yellow."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The book is extremely old."));
		
		sentence = "The architectural styles reflect American, Spanish, Chinese, and Malay influences.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The architectural styles reflect American influences."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The architectural styles reflect Spanish influences."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The architectural styles reflect Chinese influences."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The architectural styles reflect Malay influences."));
		
		
	}
	
	
	/**
	 * expected to fail.  system doesn't address these issues
	 */
	public void testToughCases(){
		String sentence;
		Tree parse;
		Collection<Question> res;

		sentence = "John ran quickly, however.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran quickly."));

		sentence = "Bob likes John and Mary, my friends.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob likes John, my friends."));
		
		sentence = "Bob likes both Susan and Kelly.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob likes both Kelly."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Bob likes both Susan."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob likes Susan."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Bob likes Kelly."));
			
		sentence = "John either likes or hates Bob.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John likes Bob."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John hates Bob."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John either likes."));
		
		//don't extract from either of these because they parse the same (but should behave differently)
		//note: we could see if the NP is a time...
		sentence = "Earlier this year, snow fell.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Snow fell earlier this year."));
		sentence = "An unabashed carnivore, John likes steak.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John is an unabashed carnivore."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John likes steak."));

		//parse has "may" and "may not win" as separate VPs. 
		//"may or may not" is not parsed as a constituent, oddly enough.
		sentence = "John may or may not win.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John may win."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John may not win."));
		
		sentence = "John talked to me while crossing the bridge and walking by the lake.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John talked to me while crossing the bridge."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John talked to me while walking by the lake."));
		
		//I assume these don't occur often enough to deal with them.  
		//My sense is that in most such constructions, the phrase modifies the subject
		//as in "Studying daily, John was able to pass the test."
		sentence = "Broadly speaking, the project was successful.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The project was successful."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The project was speaking."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "The project was broadly speaking."));
		
		//"his car broken" is parsed as a single NP constituent, rather than a clause
		sentence = "With his car broken, John could not get to work.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John could not get to work."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "His car was broken."));
		
		sentence = "John and Susan played together.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John played together."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "Susan played together."));
		
		sentence = "John and I are friends.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertFalse(res.toString(), setContainsTreeWithYield(res, "John is friends."));
		assertFalse(res.toString(), setContainsTreeWithYield(res, "I am friends."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John and I are friends."));
		
		sentence = "John ran for five miles, then walked another two miles.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John ran for five miles."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John then walked another two miles.")|setContainsTreeWithYield(res, "John walked another two miles."));
		
		sentence = "The cliff, beyond which was the sea, was tall.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The cliff was tall."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The sea was beyond the cliff.") || setContainsTreeWithYield(res, "Beyond the cliff was the sea."));
		
		sentence = "The bomb sank one boat, killing six sailors and injuring 19 others.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bomb sank one boat."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bomb killed 6 sailors."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "The bomb injured 19 others."));
			
		sentence = "Test flights of three new 747-8's, configured as cargo planes, have been conducted.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Test flights of three new 747-8's have been conducted."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "Test flights of three new 747-8's were configured as cargo planes."));
	
	}

	
	/**
	 * expected to fail.  extraction from such participial phrases is disabled 
	 * since they are fairly rare
	 */
	public void testWITHParticipials() {
		String sentence;
		Tree parse;
		Collection<Question> res;
		
		sentence = "James won, with John helping him.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "James won."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John helped him."));
		

		
		sentence = "With John helping him, James won.";
		parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		res = simp.simplify(parse);
		assertTrue(res.toString(), setContainsTreeWithYield(res, "James won."));
		assertTrue(res.toString(), setContainsTreeWithYield(res, "John helped him.") 
				|| setContainsTreeWithYield(res, "James won with John helping him."));
	}
	
}
