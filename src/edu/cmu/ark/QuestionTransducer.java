// Question Generation via Overgenerating Transformations and Ranking
// Copyright (c) 2008, 2009 Carnegie Mellon University.  All Rights Reserved.
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


package edu.cmu.ark;

import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

import java.text.NumberFormat;
import java.util.regex.*;
import java.io.*;
import java.util.*;


import edu.stanford.nlp.trees.tregex.tsurgeon.*;
import edu.stanford.nlp.trees.tregex.*;


/**
 * Class for converting declarative statements into questions.
 * 
 * This class and the WhPhraseGenerator class constitute "stage 2" as discussed 
 * in papers on the system.
 *  
 * @author Michael Heilman (mheilman@cs.cmu.edu)
 * 
 */
public class QuestionTransducer {
	public QuestionTransducer(){
		whGen = new WhPhraseGenerator();
		numWHPhrases = 0;
	}

	
	/**
	 * This method removes question objects that have duplicate yields (i.e., output strings).
	 * It goes in order so that higher ranked questions, which are expected to appear first,
	 * will remain.
	 * 
	 * @param givenQuestions
	 */
	public static void removeDuplicateQuestions(Collection<Question> givenQuestions) {
		Map<String, Question> yieldMap = new HashMap<String, Question>();
		String yield;

		//add questions that used NP Clarification first
		for(Question q: givenQuestions){
			if(q.getFeatureValue("performedNPClarification") == 0.0){
				continue;
			}
			yield = q.getTree().yield().toString();
			if(yieldMap.containsKey(yield)){
				if(GlobalProperties.getDebug()) System.err.println("Removing duplicate: "+yield);
				continue;
			}
			
			yieldMap.put(yield, q);
		}
		
		//now add any new questions that don't involve NP Clarification
		for(Question q: givenQuestions){
			if(q.getFeatureValue("performedNPClarification") == 1.0){
				continue;
			}
			yield = q.getTree().yield().toString();
			if(yieldMap.containsKey(yield)){
				if(GlobalProperties.getDebug()) System.err.println("Removing duplicate: "+yield);
				
				//if a previous question that involved NP Clarification has the same yield (i.e., text),
				//then mark it as using NP Clarification for the answer only
				Question other = yieldMap.get(yield);
				if(other.getFeatureValue("performedNPClarification") == 1.0 && other.getSourceSentenceNumber() == q.getSourceSentenceNumber()){
					//other.setFeatureValue("performedNPClarificationAnswerOnly", 1.0);
					other.setFeatureValue("performedNPClarification", 0.0);
				}
				continue;
			}
			
			yieldMap.put(yield, q);
		}
		
		givenQuestions.clear();
		givenQuestions.addAll(yieldMap.values());
	}
	
	
	/**
	 * This method identifies whether the question contains personal pronouns
	 * or demonstrative pronouns (e.g., ``THAT was interesting''),
	 * so that the system (by default) avoids outputting them.
	 * If the noun phrase clarification has resolved a personal pronoun
	 * to something within the same sentence, then a question will be produced as output
	 * (e.g., John knew he would win -> Who knew he would win?)
	 * 
	 * @param q
	 * @return
	 */
	public static boolean containsUnresolvedPronounsOrDemonstratives(Question q) {
		boolean res = false;
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		
		//return false if noun phrase clarification 
		//has been performed (i.e., if there are pronouns left, they are OK).
		if(q.getFeatureValue("performedNPClarification") == 0.0){	
			tregexOpStr = "/^PRP/";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			matcher = matchPattern.matcher(q.getTree());
			res |= matcher.find();
		}
		
		//tregexOpStr = "NP < (DT < that|this|those|these !$ NN|NNP|NNPS|NP|NNS|SBAR)";
		tregexOpStr = "NP < (DT < that|this|those|these)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(q.getTree());
		res |= matcher.find();
		
		return res;
	}
	


	public void generateQuestionsFromParse(String inputParseStr){
		Tree inputTree = AnalysisUtilities.getInstance().readTreeFromString(inputParseStr);
		generateQuestionsFromParse(inputTree);
	}

	
	public void generateQuestionsFromParse(Tree inputTree){
		Question q = new Question();
		q.setSourceTree(inputTree);
		q.setIntermediateTree(inputTree.deeperCopy());
		generateQuestionsFromParse(q);
	}
	
	/**
	 * The top-level method for converting declarative sentences into
	 * yes-no and WH questions.
	 * 
	 * @param inputQuestion
	 */
	public void generateQuestionsFromParse(Question inputQuestion){
		//initialize the array used to store the output questions
		questions = new ArrayList<Question>();
		
		//check if this is a sentence we want to create questions from.
		//E.g., avoid blank sentences, fragments, and sentences that are already questions
		if(!isUsableInputSentence(inputQuestion.getIntermediateTree())){
			if(GlobalProperties.getDebug()) System.err.println("Not a usable sentence.");
			return;
		}
				
		Tree answerPhrase;
		Question tmp1 = inputQuestion.deeperCopy();
		Question tmp2 = null;
		List<Tree> outputTrees;

		if(GlobalProperties.getDebug()) System.err.println("getQuestionsFromParse: input: "+tmp1.toString());
		
		whGen.setCurrentQuestion(tmp1);
		tmp1.setTree(tmp1.getIntermediateTree().deeperCopy());
		putLeadingAbverbPhrasesInsideVPs(tmp1.getTree());
		AnalysisUtilities.downcaseFirstToken(tmp1.getTree());
		
		//mark phrases that should not be answer phrases,
		//either due to syntactic constraints or conservative restrictions
		tmp1.setTree(markUnmovablePhrases(tmp1.getTree()));
		tmp1.setTree(markPossibleAnswerPhrases(tmp1.getTree()));
		if(GlobalProperties.getDebug()) System.err.println("Number of Possible WH questions: "+numWHPhrases+"\n");
		
		//iterate over the possible answer phrases, generate
		//questions for each one
		for(int i=0; i<numWHPhrases; i++){
			tmp2 = tmp1.deeperCopy();
			
			answerPhrase = getAnswerPhrase(tmp2.getTree(), i);
			answerPhrase = removeMarkersFromTree(answerPhrase.deeperCopy());
		
			//check whether the current answer phrase is the subject.
			//if not, then decompose the main verb and perform subject auxiliary inversion
			boolean subjectMovement = isSubjectMovement(tmp2.getTree(), i);
			if(subjectMovement){
				ensureVerbAgreementForSubjectWH(tmp2.getTree());
				if(GlobalProperties.getComputeFeatures()) tmp2.setFeatureValue("isSubjectMovement", 1.0);
				if(GlobalProperties.getComputeFeatures()) tmp2.setFeatureValue("whQuestion", 1.0);
			}else{
				tmp2.setTree(decomposePredicate(tmp2.getTree()));
				tmp2.setTree(subjectAuxiliaryInversion(tmp2.getTree()));
				if(GlobalProperties.getComputeFeatures()) tmp2.setFeatureValue("isSubjectMovement", 0.0);
				if(GlobalProperties.getComputeFeatures()) tmp2.setFeatureValue("whQuestion", 1.0);
			}
			tmp2.setTree(relabelMainClause(tmp2.getTree()));

			//Now generate questions by analyzing the answer phrase and choosing possible
			//question words (e.g., what, who) from it.
			//Then, remove the answer phrase and put the question phrase at 
			//the front of the main clause before the subject.
			outputTrees = moveWHPhrase(tmp2.getTree(), tmp2.getIntermediateTree(), i, subjectMovement);
			
			
			//post-process and filter the output
			for(Tree t: outputTrees){
				tmp2 = tmp2.deeperCopy();
				tmp2.setTree(t);
				AnalysisUtilities.upcaseFirstToken(tmp2.getTree());
				
				relabelPunctuationAsQuestionMark(tmp2.getTree());
				tmp2.setAnswerPhraseTree(answerPhrase);
				if(GlobalProperties.getComputeFeatures()) QuestionFeatureExtractor.getInstance().extractFinalFeatures(tmp2);
				
				if(avoidPronounsAndDemonstratives && (containsUnresolvedPronounsOrDemonstratives(tmp2))){
					if(GlobalProperties.getDebug()) System.err.println("generateQuestionsFromParse: skipping due to pronouns");
				}else{
					questions.add(tmp2);	
				}
				
				if(GlobalProperties.getDebug()) System.err.println();
			}
		}

		//add a yes-no question by performing subject auxiliary inversion
		tmp2 = tmp1.deeperCopy();
		tmp2.setTree(decomposePredicate(tmp2.getTree()));
		if(canInvert(tmp2.getTree())){
			tmp2.setTree(removeMarkersFromTree(tmp2.getTree()));
			tmp2.setTree(subjectAuxiliaryInversion(tmp2.getTree()));
			tmp2.setTree(relabelMainClause(tmp2.getTree()));
			tmp2.setTree(moveLeadingAdjuncts(tmp2.getTree()));
			relabelPunctuationAsQuestionMark(tmp2.getTree());
			AnalysisUtilities.upcaseFirstToken(tmp2.getTree());
			tmp2.setAnswerPhraseTree(null);
			if(GlobalProperties.getComputeFeatures()) tmp2.setFeatureValue("isSubjectMovement", 0.0);
			if(GlobalProperties.getComputeFeatures()) tmp2.setFeatureValue("whQuestion", 0.0);
			if(GlobalProperties.getComputeFeatures()) QuestionFeatureExtractor.getInstance().extractFinalFeatures(tmp2);
			
			if(avoidPronounsAndDemonstratives && containsUnresolvedPronounsOrDemonstratives(tmp2)){
				if(GlobalProperties.getDebug()) System.err.println("generateQuestionsFromParse: skipping due to pronouns");
			}else{
				questions.add(tmp2);	
			}
			
			if(GlobalProperties.getDebug()) System.err.println();
		}
		
	}
	

	private void relabelPunctuationAsQuestionMark(Tree inputTree) {
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		TregexPattern matchPattern;
		TsurgeonPattern p;

		tregexOpStr = "/^\\./ < /^\\./=period";
		ps.add(Tsurgeon.parseOperation("relabel period |?|"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));	    		
		Tsurgeon.processPatternsOnTree(ops, inputTree);
		

		//Make sure there is a question mark at the end.
		//This catches odd cases like "I live in Pittsburg, PA.",
		//where the parser think the period at the end is part of an abbreviation.
		tregexOpStr = "/^\\./";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(!matchPattern.matcher(inputTree).find()){
			inputTree.getChild(0).addChild(AnalysisUtilities.getInstance().readTreeFromString("(. ?)"));
		}
	}
	

	/**
	 * Identifies whether a particular answer phrase i
	 * is the subject of the sentence.
	 * 
	 * e.g., returns true for:
	 * sentence: John met Sally.
	 * question: Who met Sally?
	 * 
	 * returns false for:
	 * sentence: John met Sally.
	 * question: Who did John meet?
	 * 
	 */
	private boolean isSubjectMovement(Tree inputTree, int i) {
		String tregexOpStr = "ROOT=root < (S < NP-"+i+"|SBAR-"+i+")";
		TregexPattern matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(inputTree);
		boolean res = matcher.find();
		return res;
	}

	
	/**
	 * The Stanford Parser (or maybe the Penn Treebank) oddly seems to only rarely include 
	 * adverbs that precede verbs in verb phrases (e.g., ''oddly'' in this sentence).
	 * 
	 * This method adjusts for that.
	 * 
	 */
	private void putLeadingAbverbPhrasesInsideVPs(Tree inputTree){
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		TregexPattern matchPattern;
		TsurgeonPattern p;

		tregexOpStr = "ADVP=mover $. VP=vp";
		ps.add(Tsurgeon.parseOperation("move mover >0 vp"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));	    		
		Tsurgeon.processPatternsOnTree(ops, inputTree);
	}
	
	
	private Tree markUnmovablePhrases(Tree inputTree){
		if(noAnswerPhraseMarking){
			return inputTree.deeperCopy();
		}else{
			return markUnmovablePhrasesFull(inputTree);
		}
	}
	
	
	/**
	 *
	 * This method marks phrases in the tree that should not undergo WH movement
	 * and become answers to questions, either due to syntactic 
	 * constraints or some conservative restrictions used to avoid
	 * particular constructions that the system is not designed to handle.
	 * 
	 * E.g., 
	 * Sentence: Darwin studied how SPECIES evolve.
	 * Avoided Question: * What did Darwin study how evolve?
	 *
	 */
	private Tree markUnmovablePhrasesFull(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();

		//adjunct clauses under verb phrases (following commas)
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (VP < (S=unmovable $,, /,/))");
		
		//anything under a sentence level subordinate clause
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root < (S < PP|ADJP|ADVP|S|SBAR=unmovable)");

		//anything under a phrase directly dominating a conjunction
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (/\\.*/ < CC << NP|ADJP|VP|ADVP|PP=unmovable)");

		//adjunct clauses -- assume subordinate clauses that have a complementizer other than "that" (or empty) are adjuncts 
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (SBAR < (IN|DT < /[^that]/) << NP|PP=unmovable)");

		//anything under a WH phrase
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (SBAR < /^WH.*P$/ << NP|ADJP|VP|ADVP|PP=unmovable)");

		//"Complementizer-trace effect"
		//the subject of a complement phrase when an explicit complementizer is present (e.g., I knew that JOHN ran.)
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (SBAR <, IN|DT < (S < (NP=unmovable !$,, VP)))");

		//anything under a clause that is a predicate nominative (e.g., my favorite activity is to run in THE PARK)
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (S < (VP <+(VP) (VB|VBD|VBN|VBZ < be|being|been|is|are|was|were|am) <+(VP) (S << NP|ADJP|VP|ADVP|PP=unmovable)))");		
		
		//objects of prepositional phrases with prepositions other than "of" or "about".
		//"of" and "about" signal that the modifier is a complement rather than an adjunct. 
		//allows: "John visited the capital of Alaska." -> "What did John visit the capital of?"
		//disallows: "John visited a city in Alaska." -> ? "What did John visit a city in?"
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (NP << (PP=unmovable !< (IN < of|about)))");
		
		//nested prepositional phrases of any kind 
		//disallows: "Bill saw John in the hall of mirrors." -> * "What did Bill see John in the hall of?"
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (PP << PP=unmovable)");
		
		//prepositional phrases in subjects (e.g., disallows: "The capital of Alaska is Juneau." -> * "What is the capital of Juneau?")
		//Nothing can be moved out of subjects.
		//I think the generative account is that phrases can only be moved to the level of the verb
		//that governs them, and subjects (along with adjuncts) are not governed by the verb.
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (NP $ VP << PP=unmovable)");
		
		//subordinate clauses that are not complements of verbs
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (SBAR=unmovable [ !> VP | $-- /,/ | < RB ])");
		
		//adjunct subordinate clauses
		//"how", "whether", and "that" under IN or WHADVP nodes signal complements.
		//WHNP always signals a complement.
		//otherwise, the SBAR is an adjunct.
		//Note: we mark words like "where" as unmovable because they are potentially adjuncts. 
		//  e.g., "he knew where it was" has a complement, but "he went to college where he grew up" has an adjunct
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (SBAR=unmovable !< WHNP < (/^[^S].*/ !<< that|whether|how))"); //dominates a non-S node that doesn't include one of the unambiguous complementizers 
		
		//////////////////////////////////////////////////////////////
		//MARK SOME AS UNMOVABLE TO AVOID OBVIOUSLY BAD QUESTIONS
		//
		
		//existential there NPs
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (NP=unmovable < EX)");

		//phrases in quotations
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (/^S/ < `` << NP|ADJP|VP|ADVP|PP=unmovable)");
		
		//prepositional phrases that don't have NP objects
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (PP=unmovable !< /.*NP/)");

		//pronouns which are the subject of complement verb phrases
		//These would nearly always lead to silly/tricky questions (e.g., "GM says its profits will fall." -> "Whose profits did GM say will fall?") 
		//markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (VP < (SBAR < (S <<, (NP=unmovable < PRP))))");

		//both NPs that are under an S (MJH: we are punting on this).  
		//If there are multiple NPs, one may be a temporal modifier
		markMultipleNPsAsUnmovable(copyTree);
		/////////////////////////////////////////////////////////////////
		
		
		////////////////////////////////////////////////////////////////
		//PROPAGATE ABOVE CONSTRAINTS
		//any non-PP phrases under otherwise movable phrases (we assume movable phrases serve as islands)
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (NP|PP|ADJP|ADVP|PP << (NP|ADJP|VP|ADVP=unmovable))");
		
		//anything under an unmovable node
		markNodesAsUnmovableUsingPattern(copyTree, "ROOT=root << (@UNMOVABLE << NP|ADJP|VP|ADVP|PP=unmovable)");

		if(GlobalProperties.getDebug()) System.err.println("markUnmovablePhrases: "+copyTree.toString());
		return copyTree;
	}


	/**
	 * This method is used to mark noun phrases that are sisters of each other, 
	 * such as in double object dative constructions.  
	 * I could not figure out how to get Tsurgeon to do this easily, 
	 * so phrases are just marked using the stanford parser API instead.
	 * 
	 * E.g., 
	 * sentence: John gave Mary the book.
	 * avoided question: * Who did John give the book? (the system doesn't convert "indirect" objects to oblique arguments)
	 * 
	 * @param inputTree
	 */
	private void markMultipleNPsAsUnmovable(Tree inputTree){
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		TregexPattern matchPattern;
		TsurgeonPattern p;

		String tregexOpStr = "(NP=unmovable $ @NP)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		ps.add(Tsurgeon.parseOperation("relabel unmovable NP-UNMOVABLE"));
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));		
		Tsurgeon.processPatternsOnTree(ops, inputTree);

		ops.clear();
		ps.clear();
		tregexOpStr = "NP-UNMOVABLE=unmovable";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		ps.add(Tsurgeon.parseOperation("relabel unmovable UNMOVABLE-NP"));
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, inputTree);
	}


	private Tree removeMarkersFromTree(Tree inputTree){
		if(inputTree == null) return null;
		Tree res;
		String treeStr = inputTree.toString();
		treeStr = treeStr.replaceAll("UNMOVABLE-", "");
		treeStr = treeStr.replaceAll("-\\d+ ", " ");
		res = AnalysisUtilities.getInstance().readTreeFromString(treeStr);
		return res;
	}


	/**
	 * Note: It would probably be easier to use the Tregex operation to find the nodes
	 * and then change the labels directly rather than writing a Tsurgeon operation.
	 * But, when I wrote the original code, I used Tsurgeon.  Probably not worth refactoring. 
	 * 
	 * @param inputTree
	 * @param tregexOpStr
	 */
	private void markNodesAsUnmovableUsingPattern(Tree inputTree, String tregexOpStr){
		TregexPattern matchPattern;
		TregexMatcher matcher;
		String label;
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		Tree tmp;

		while(matcher.find()){
			tmp = matcher.getNode("unmovable");
			label = tmp.label().toString();
			tmp.label().setValue("UNMOVABLE-"+label);
		}
	}
	
	
	/**
	 * Thsi method returns the node for the ith possible answer phrase in this sentence
	 * (after potential answer phrases have been identified by marking unmovable ones) 
	 * 
	 * @param inputTree
	 * @param i
	 * @return
	 */
	private Tree getAnswerPhrase(Tree inputTree, int i){
		Tree answerPhrase;
		String tregexOpStr;
		TregexPattern matchPattern;
		String marker = "/^(NP|PP|SBAR)-"+i+"$/";
		
		tregexOpStr = marker+"=answer";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(GlobalProperties.getDebug()) System.err.println("moveWHPhrase: inputTree:" + inputTree.toString());
		//if(GlobalProperties.getDebug()) System.err.println("moveWHPhrase: tregexOpStr:" + tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(inputTree);
		matcher.find();
		answerPhrase = matcher.getNode("answer");
		
		return answerPhrase;
	}

	
	/**
	 * 
	 * This method removes the answer phrase from its original position
	 * and places it at the front of the main clause.
	 * 
	 * Note: Tsurgeon operations are perhaps not optimal here.
	 * Using the Stanford API to move nodes directly might be simpler...
	 * 
	 */
	private List<Tree> moveWHPhrase(Tree inputTree, Tree intermediateTree, int i, boolean subjectMovement){
		Tree copyTree;
		Tree copyTree2;
		List<Tree> res = new ArrayList<Tree>();
		Tree mainclauseNode;
		Tree prepPlaceholderParent;
		
		String marker = "/^(NP|PP|SBAR)-"+i+"$/";

		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;

		//extract the "answer" phrase and generate a WH phrase from it
		tregexOpStr = "ROOT=root < (SQ=qclause << "+marker+"=answer < VP=predicate)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(GlobalProperties.getDebug()) System.err.println("moveWHPhrase: inputTree:" + inputTree.toString());
		if(GlobalProperties.getDebug()) System.err.println("moveWHPhrase: tregexOpStr:" + tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(inputTree);
		matcher.find();
		Tree phraseToMove = matcher.getNode("answer");
		
		String whPhraseSubtree;
		String leftOverPreposition;
		
		if(printExtractedPhrases) System.out.println("EXTRACTED\t"+phraseToMove.yield().toString());

		whGen.generateWHPhraseSubtrees(removeMarkersFromTree(phraseToMove), intermediateTree.yield().toString());
		List<String> whPhraseSubtrees = whGen.getWHPhraseSubtrees();
		List<String> leftOverPrepositions = whGen.getLeftOverPrepositions();

		copyTree = inputTree.deeperCopy();
		//The placeholder is necessary because tsurgeon will complain
		//if an added node has no children. This placeholder is removed below.
		ps.add(Tsurgeon.parseOperation("insert (PREPPLACEHOLDER dummy) $+ answer"));
		ps.add(Tsurgeon.parseOperation("prune answer"));
		ps.add(Tsurgeon.parseOperation("insert (SBARQ=mainclause PLACEHOLDER=placeholder) >0 root")); 
		ps.add(Tsurgeon.parseOperation("move qclause >-1 mainclause"));
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, copyTree);

		copyTree = removeMarkersFromTree(copyTree);
		
		//Now put each WH phrase into the tree and remove the original answer.
		//Operate on the tree directly rather than using tsurgeon 
		//because tsurgeon can't parse operations that insert trees with special characters (e.g., ":")
		for(int j=0; j<whPhraseSubtrees.size(); j++){
			copyTree2 = copyTree.deeperCopy();
			whPhraseSubtree = whPhraseSubtrees.get(j);
			leftOverPreposition = leftOverPrepositions.get(j);

			if(GlobalProperties.getDebug()) System.err.println("moveWHPhrase: whPhraseSubtree:"+whPhraseSubtree);
			tregexOpStr = "ROOT < (SBARQ=mainclause < PLACEHOLDER=ph1) << (__=ph2Parent < PREPPLACEHOLDER=ph2)";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			matcher = matchPattern.matcher(copyTree2);
			if(!matcher.find()){
				continue;
			}
			mainclauseNode = matcher.getNode("mainclause");
			//replace the wh placeholder with a wh phrase
			mainclauseNode.removeChild(0);
			mainclauseNode.addChild(0, AnalysisUtilities.getInstance().readTreeFromString(whPhraseSubtree));

			//Replace the pp placeholder with the left over preposition. 
			//This may happen when the answer phrase was a PP.
			//e.g., John went to the game. -> What did John go to?
			prepPlaceholderParent = matcher.getNode("ph2Parent");
			int index = prepPlaceholderParent.indexOf(matcher.getNode("ph2"));
			if(leftOverPreposition != null && leftOverPreposition.length()>0){
				prepPlaceholderParent.addChild(index, AnalysisUtilities.getInstance().readTreeFromString(leftOverPreposition));
			}
			//now remove the left-over-preposition placeholder
			ps.clear();
			ps.add(Tsurgeon.parseOperation("prune ph2"));
			p = Tsurgeon.collectOperations(ps);
			ops.clear();
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(TregexPatternFactory.getPattern("PREPPLACEHOLDER=ph2"),p));
			Tsurgeon.processPatternsOnTree(ops, copyTree2);
			
			copyTree2 = moveLeadingAdjuncts(copyTree2);

			if(GlobalProperties.getDebug()) System.err.println("moveWHPhrase: "+copyTree2.toString());
			res.add(copyTree2);
		}

		return res;
	}


	/**
	 * This method moves adjunct phrases that appear prior to the first possible subject.
	 * e.g., in order to produce "WHILE I WAS AT THE STORE, who did I meet?"
	 * from "WHILE I WAS AT THE STORE, I met him."
	 * 
	 * This operation is not actually used in the full system because 
	 * leading modifiers are either moved or removed by the simplified 
	 * factual statement extraction step in stage 1.
	 * 
	 */
	private Tree moveLeadingAdjuncts(Tree inputTree){
		if(GlobalProperties.getDebug()) System.err.println("moveLeadingAdjuncts:"+inputTree.toString());
		
		Tree copyTree = inputTree.deeperCopy();
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		boolean matchFound = true;
		List<Pair<TregexPattern, TsurgeonPattern>> ops;
		List<TsurgeonPattern> ps;
		TsurgeonPattern p;
		
		while(true){
			ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
			ps = new ArrayList<TsurgeonPattern>();
			tregexOpStr = "TMPROOT=root";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			matcher = matchPattern.matcher(copyTree);
			matchFound = matcher.find();
			ps.add(Tsurgeon.parseOperation("relabel root ROOT"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, copyTree);
			
			ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
			ps = new ArrayList<TsurgeonPattern>();
			
			//for yes/no questions, find any phrases that precede the first possible subject (NP|SBAR)
			// and move them to the front of the question clause.
			tregexOpStr = "ROOT=root < (SQ=mainclause < (/,|ADVP|ADJP|SBAR|S|PP/=mover $,, /MD|VB.*/=pivot $ NP=subject))";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			matcher = matchPattern.matcher(copyTree);
			matchFound = matcher.find();
	
			if(!matchFound){
				//for WH questions, move any phrases that precede the first potential subject
				//--or verb phrase for when the original subject is the answer phrase
				tregexOpStr = "ROOT=root < (SBARQ=mainclause < WHNP|WHPP|WHADJP|WHADVP=pivot < (SQ=invertedclause < (/,|S|ADVP|ADJP|SBAR|PP/=mover !$,, /\\*/ $.. /^VP|VB.*/)))";
				matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
				matcher = matchPattern.matcher(copyTree);
				matchFound = matcher.find();			
			}
	
			if(!matchFound){
				break;
			}
			
			//need to relabel as TMPROOT so things are moved one at a time, to preserve their order 
			ps.add(Tsurgeon.parseOperation("move mover $+ pivot"));
			ps.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, copyTree);
	
			//System.err.println("moving..."+copyTree.toString());
		}
		
		//remove extra commas for sentences like "Bill, while walking, saw John."
		Tree firstChild = copyTree.getChild(0);
		
		if(firstChild.getChild(0).label().toString().equals(",")){
			firstChild.removeChild(0);
		}
		
		if(GlobalProperties.getDebug()) System.err.println("moveLeadingAdjuncts(out):"+copyTree.toString());
		return copyTree;
	}

	
	/**
	 * This method decomposes the main verb of the sentence 
	 * for yes-no questions and WH questions where the answer
	 * phrase is not the subject.
	 * 
	 * e.g., I met John -> I did meet John.
	 * (which would later become "Who did I meet?")
	 * 
	 */
	private Tree decomposePredicate(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();
		
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		TregexPattern matchPattern;
		TsurgeonPattern p;
		TregexMatcher matcher;
		Tree tmpNode;
		//tregexOpStr = "ROOT < (S=mainclause < (VP=predphrase < /VB.?/=tensedverb !< (VP < /VB.?/)))";
		//tregexOpStr = "ROOT < (S=mainclause < (VP=predphrase < (/VB.?/=tensedverb !< is|was|were|am|are|has|have|had|do|does|did)))";
		
		//This rather complex rule identifies predicates to decompose.  
		//There are two cases, separated by a disjunction.  
		//One could break it apart into separate rules to make it simpler...
		//
		//The first part of the disjunction
		//(i.e., < (/VB.?/=tensedverb !< is|was|were|am|are|has|have|had|do|does|did) )
		//is for handling basic sentences
		//(e.g., John bought an apple -> What did John buy?), 
		//sentences with auxiliaries
		//(e.g., John had bought an apple -> Had John bought an apple?),
		//and sentences with participial phrases
		//(e.g., John seemed finished with the apple -> What did John seem finished with?).
		//
		//The second part of the disjunction
		//(i.e., < /VB.?/=tensedverb !< VP )
		//is for handling sentences that have predicates
		//that can also be auxiliaries (e.g., I have a book).
		//In these cases, we do want to decompose have, has, had, etc.
		//(e.g., What did I have?)
		tregexOpStr = "ROOT < (S=mainclause < (VP=predphrase [ < (/VB.?/=tensedverb !< is|was|were|am|are|has|have|had|do|does|did) | < /VB.?/=tensedverb !< VP ]))";

		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(copyTree);
		if(matcher.find()){
			Tree subtree = matcher.getNode("tensedverb");
			String lemma = AnalysisUtilities.getInstance().getLemma(subtree.getChild(0).label().toString(), subtree.label().toString());		
			String aux = getAuxiliarySubtree(subtree);

			if(!lemma.equals("be")){
				ps.add(Tsurgeon.parseOperation("replace predphrase (MAINVP=newpred PLACEHOLDER)"));
				ps.add(Tsurgeon.parseOperation("insert predphrase >-1 newpred"));
				ps.add(Tsurgeon.parseOperation("insert (VBLEMMA PLACEHOLDER) $+ tensedverb"));
				ps.add(Tsurgeon.parseOperation("delete tensedverb"));
				p = Tsurgeon.collectOperations(ps);
				ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
				Tsurgeon.processPatternsOnTree(ops, copyTree);
				matchPattern = TregexPatternFactory.getPattern("MAINVP=mainvp");
				matcher = matchPattern.matcher(copyTree);
				matcher.find();
				tmpNode = matcher.getNode("mainvp");
				tmpNode.removeChild(0);
				tmpNode.label().setValue("VP");
				tmpNode.addChild(0, AnalysisUtilities.getInstance().readTreeFromString(aux));

				matchPattern = TregexPatternFactory.getPattern("VBLEMMA=vblemma");
				matcher = matchPattern.matcher(copyTree);
				matcher.find();
				tmpNode = matcher.getNode("vblemma");
				tmpNode.removeChild(0);
				tmpNode.label().setValue("VB");
				tmpNode.addChild(0, AnalysisUtilities.getInstance().readTreeFromString(lemma));
			}
		}

		if(GlobalProperties.getDebug()) System.err.println("decomposePredicate: "+copyTree.toString());
		return copyTree;
	}


	/**
	 * Returns the singular present tense form of a tensed verb.
	 * This only affects the output when generating from sentences where
	 * first and second person pronouns are the subject.
	 * 
	 * E.g.,
	 * Affects:
	 * I walk -> Who walks? (rather than, Who walk?)
	 * 
	 * Does not affect:
	 * He walks -> Who walks?
	 * 
	 */
	private String getSingularFormSubtree(Tree tensedVerbSubtree) {
		String res = "";
		String lemma = AnalysisUtilities.getInstance().getLemma(tensedVerbSubtree.getChild(0).label().toString(), tensedVerbSubtree.label().toString());
		String pos = tensedVerbSubtree.value();
		if(pos.equals("VBD")){
			res = tensedVerbSubtree.toString();
		}else{
			res = "(VBZ "+ AnalysisUtilities.getInstance().getSurfaceForm(lemma, "VBZ") +")";
		}

		return res;
	}


	/**
	 * This method is used to decompose the main verb.
	 * e.g., 
	 * input: (VBD walked) 
	 * output: (VBD did)
	 * 
	 * Note: another method would extract the base form of the verb "(VB walk)"
	 * 
	 * @param tensedverb
	 * @return
	 */
	private String getAuxiliarySubtree(Tree tensedverb){
		if(tensedverb == null){
			return "";
		}

		String res = "";
		String label;
		Pattern p = Pattern.compile("\\((\\S+) [^\\)]*\\)");
		Matcher m = p.matcher(tensedverb.toString());
		m.find();
		label = m.group(1);

		if(label.equals("VBD")){
			res = "(VBD did)";
		}else if(label.equals("VBZ")){
			res = "(VBZ does)";
		}else if(label.equals("VBP")){
			res = "(VBP do)";	
		}else{
			res = "(VB do)";
		}

		return res;
	}


	/**
	 * relabels the main clause from S (declarative sentence clause)
	 * to SQ (inverted question clause)
	 * 
	 * @param inputTree
	 * @return
	 */
	private Tree relabelMainClause(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();
		String tregexOpStr = "ROOT < S=mainclause";
		TregexPattern matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		TregexMatcher m = matchPattern.matcher(copyTree);
		if(m.matches()){
			m.getNode("mainclause").label().setValue("SQ");
		}
		
		return copyTree;

	}


	/**
	 * Moves an auxiliary verb to the front of the main clause (i.e., before the subject).
	 * This is used in yes-no questions and WH questions where the answer phrase
	 * is not the subject
	 * 
	 * E.g.,
	 * John did meet Paul -> Did John meet Paul (which will then become "Who did John meet?") 
	 * 
	 */
	private Tree subjectAuxiliaryInversion(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();

		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;

		//auxilaries		
		tregexOpStr = "ROOT=root < (S=mainclause <+(/VP.*/) (VP < /(MD|VB.?)/=aux < (VP < /VB.?/=baseform)))";
		ps.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
		ps.add(Tsurgeon.parseOperation("prune aux"));
		ps.add(Tsurgeon.parseOperation("insert aux >0 mainclause"));
		
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, copyTree);


		//copula
		ops.clear();
		ps.clear();
		
		tregexOpStr = "ROOT=root < (S=mainclause <+(/VP.*/) (VP < (/VB.?/=copula < is|are|was|were|am) !< VP))";
		ps.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
		ps.add(Tsurgeon.parseOperation("prune copula\n"));
		ps.add(Tsurgeon.parseOperation("insert copula >0 mainclause"));
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, copyTree);

		ops.clear();
		ps.clear();
		tregexOpStr = "TMPROOT=root";
		ps.add(Tsurgeon.parseOperation("relabel root ROOT"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, copyTree);

		if(GlobalProperties.getDebug()) System.err.println("subjectAuxiliaryInversion: "+copyTree.toString());
		return copyTree;
	}
	

	/**
	 * Changes the inflection of the main verb for questions with
	 * first and second person pronouns are the subject.
	 * Note: this probably isn't necessary for most applications.
	 * 
	 * E.g.,
	 * Affects:
	 * I walk -> Who walks? (rather than, Who walk?)
	 * 
	 * Does not affect:
	 * He walks -> Who walks?
	 * 
	 */
	private void ensureVerbAgreementForSubjectWH(Tree inputTree){
		String tregexOpStr;
		TregexMatcher matcher;
		TregexPattern matchPattern;
		Tree subjectTree;
		String subjectString;
		
		tregexOpStr = "/^(NP|PP|SBAR)-"+0+"$/";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		if(matcher.find()){
			subjectTree = matcher.getMatch();
			subjectString = subjectTree.yield().toString();
			if(subjectString.equalsIgnoreCase("I") || subjectString.equalsIgnoreCase("you")){
				tregexOpStr = "ROOT=root < (S=mainclause < (VP=verbphrase < (/VB.?/=tensedverb)))";
				matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
				matcher = matchPattern.matcher(inputTree);
				if(matcher.find()){
					Tree verbSubtree = matcher.getNode("tensedverb");
					Tree vpSubtree = matcher.getNode("verbphrase");
					Tree singularFormSubtree = AnalysisUtilities.getInstance().readTreeFromString(getSingularFormSubtree(verbSubtree));
					int index = vpSubtree.indexOf(verbSubtree);
					vpSubtree.removeChild(index);
					vpSubtree.addChild(index, singularFormSubtree);
					if(GlobalProperties.getDebug()) System.err.println("ensureVerbAgreementForSubjectWH: "+inputTree.toString());
				}
			}
		}
	}


	/**
	 * Marks possible answer phrase nodes with indexes for later processing.
	 * This step might be easier with the Stanford Parser API's Tree class methods
	 * than with Tsurgeon...   
	 * 
	 * @param inputTree
	 * @return
	 */
	private Tree markPossibleAnswerPhrases(Tree inputTree) {
		Tree copyTree = inputTree.deeperCopy();
		numWHPhrases = 0;

		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		Tree tmp;
		
		//find and mark the main clause subject
		tregexOpStr = "ROOT < (S < (NP|SBAR=subj $+ /,/ !$++ NP|SBAR))";
		ps.add(Tsurgeon.parseOperation("relabel subj NP-0"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(copyTree);
		if(matcher.find()){
			tmp = matcher.getNode("subj");
			tmp.label().setValue(tmp.label().toString()+"-0");
			numWHPhrases++;
		}

		//noun phrases
		tregexOpStr = "ROOT=root << NP|PP|SBAR=np";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(copyTree);
		while(matcher.find()){
			tmp = matcher.getNode("np");
			tmp.label().setValue(tmp.label().toString()+"-"+numWHPhrases);
			numWHPhrases++;
		}

		if(GlobalProperties.getDebug()) System.err.println("markPossibleAnswerPhrases: "+copyTree.toString());
		return copyTree;
	}


	/**
	 * returns whether to perform subject-aux inversion
	 * (true if there is an auxiliary or modal verb in addition to the predicate)
	 * 
	 * E.g., true for "John did meet Paul" (which could lead to "Who did John meet?") 
	 * false for "John met Paul" (which could lead to "Who met Paul?")
	 * 
	 * Note that this occurs after the main verb decomposition step
	 * (which depends on whether the answer phrase is the subject or not)
	 * 
	 * @param inputTree
	 * @return
	 */
	private boolean canInvert(Tree inputTree) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		tregexOpStr = "ROOT < (S < (VP < /(MD|VB.?)/))";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		return matcher.matches();
	}


	/**
	 * Filters out some stuff we don't want to process.
	 * Note: this method is somewhat redundant with a similar method in stage 1. 
	 * 
	 * @param inputTree
	 * @return
	 */
	private boolean isUsableInputSentence(Tree inputTree) {
		boolean res = false;

		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;

		//skip if there are leading conjunctions (need to drop these during stage 1)
		tregexOpStr = "ROOT < (S=mainclause < CC=frontedconj)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(matchPattern.matcher(inputTree).matches()){
			return false;
		}

		//make sure this is not just a single node
		tregexOpStr = "/\\./ !< /\\./";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(matchPattern.matcher(inputTree).matches()){
			return false;
		}

		//MAKE SURE THERE IS A RECOGNIZABLE SUBJECT	
		//PUNT IF THERE IS A NON-NP SUBJECT
		//also, avoid "there are ..." sentences
		tregexOpStr = "ROOT < (S < (NP !< EX))";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		res = matcher.matches();
		
		return res;
	}

	
	protected void setPrintExtractedPhrases(boolean b) {
		printExtractedPhrases = b;
	}


	public void setAvoidPronounsAndDemonstratives(boolean b) {
		avoidPronounsAndDemonstratives = b;
	}
	
	
	public boolean getAvoidPronounsAndDemonstratives() {
		return avoidPronounsAndDemonstratives;
	}


	public List<Question> getQuestions() {
		return questions;
	}


	public void setNoAnswerPhraseMarking(boolean b) {
		this.noAnswerPhraseMarking = b;
	}


	public boolean getNoAnswerPhraseMarking() {
		return noAnswerPhraseMarking;
	}
	


	/**
	 * main method for testing stage 2 in isolation.
	 * The QuestionAsker class's main method should be used to
	 * generate questions from the end-to-end system.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		QuestionTransducer qt = new QuestionTransducer();
		AnalysisUtilities.getInstance();

		String buf;
		Tree inputTree;
		boolean printParse = false;
		boolean printOriginal = false;
		boolean treeInput = false;
		boolean printFeatures = false;
		Set<Question> inputTrees = new HashSet<Question>();
		qt.setAvoidPronounsAndDemonstratives(true);
		
		for(int i=0;i<args.length;i++){
			if(args[i].equals("--debug")){
				GlobalProperties.setDebug(true);
			}else if(args[i].equals("--print-parse")){
				printParse = true;
			}else if(args[i].equals("--print-original")){
				printOriginal = true;
			}else if(args[i].equals("--print-features")){
				printFeatures = true;
			}else if(args[i].equals("--print-extracted-phrases")){
				qt.setPrintExtractedPhrases(true);
			}else if(args[i].equals("--tree-input")){
				treeInput = true;
			}else if(args[i].equals("--keep-pro")){
				qt.setAvoidPronounsAndDemonstratives(false);
			}else if(args[i].equals("--properties")){  
				GlobalProperties.loadProperties(args[i+1]);
			}
		}

		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			//take input from the user on stdin
			if(GlobalProperties.getDebug()) System.err.println("\nInput Declarative Sentence:");
			while((buf = br.readLine()) != null){
				if(treeInput){
					buf = AnalysisUtilities.preprocessTreeString(buf);
					inputTree = AnalysisUtilities.getInstance().readTreeFromString(buf);
					AnalysisUtilities.getInstance().normalizeTree(inputTree);
				}else{
					if(AnalysisUtilities.filterOutSentenceByPunctuation(buf)){
						continue;
					}
					buf = AnalysisUtilities.preprocess(buf);
					if(printOriginal) System.out.println("\n"+buf);
					ParseResult parseRes = AnalysisUtilities.getInstance().parseSentence(buf);
					inputTree = parseRes.parse;
					if(GlobalProperties.getDebug()) System.err.println("Parse Score: "+parseRes.score);
				}

				if (printParse) System.out.println(inputTree);

				inputTrees.clear();
				Question tmp = new Question();
				tmp.setIntermediateTree(inputTree.deeperCopy());
				tmp.setSourceTree(inputTree);
				inputTrees.add(tmp);
				

				//iterate over the trees given by the input
				List<Question> questions;
				for(Question q: inputTrees){
					try{
						qt.generateQuestionsFromParse(q);
						questions = qt.getQuestions();
						QuestionTransducer.removeDuplicateQuestions(questions);

						//iterate over the questions for each tree
						for(Question curQuestion: questions){
							System.out.print(curQuestion.yield());
							if(printFeatures){
								System.out.print("\t");
								int cnt = 0;
								for(Double val: curQuestion.featureValueList()){
									if(cnt > 0) System.out.print(";");
									System.out.print(NumberFormat.getInstance().format(val));
									cnt++;
								}
							}
							System.out.println();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}

				if(GlobalProperties.getDebug()) System.err.println("\nInput Declarative Sentence:");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	int numWHPhrases;  //the number of possible answer phrases identified in the source sentence.

	private boolean avoidPronounsAndDemonstratives;  //don't produce questions with pronouns
	private List<Question> questions;  //output questions, co-indexed with sourceTrees and featureValueLists

	private WhPhraseGenerator whGen;
	private boolean printExtractedPhrases;  //whether or not to print out answer phrases
	private boolean noAnswerPhraseMarking = false;
}


