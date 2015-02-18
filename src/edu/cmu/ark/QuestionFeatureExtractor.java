package edu.cmu.ark;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.trees.Tree;

public class QuestionFeatureExtractor {
	private static QuestionFeatureExtractor instance;
	private static LanguageModel forwardLM;

	private QuestionFeatureExtractor(){
	}


	public static LanguageModel getForwardLM() {
		if(forwardLM == null){
			forwardLM = new LanguageModel(GlobalProperties.getProperties().getProperty("languageModelFile", "config"+File.separator+"languageModel.txt"));
		}
		return forwardLM;
	}

	/**
	 * Extracts the features that can be extracted just by looking
	 * at the output question, the source, and the answer phrase
	 * (not features like, e.g., whether adjunct phrases were removed).
	 */
	public void extractFinalFeatures(Question q) {
		String tregexOpStr;

		Tree tree = q.getTree();

		//leading modifiers in the question (e.g., "Despite the cold, who went outside?")
		if(Question.getFeatureNames().contains("numLeadingModifiersQuestion")){
			extractCountAndGreaterThanFeatures(q, "numLeadingModifiersQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("ROOT [ < (SBARQ !<, /^WH/) | < (SQ !<, /^(MD|VB)/) ]", tree));
		}

		//Negation present in question

		if(Question.getFeatureNames().contains("negation")){
			tregexOpStr = "VP << (RB < not|never)";
			if(TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find()){
				q.setFeatureValue("negation", 1.0);
			}
		}


		extractWHFeatures(q);
		extractGrammarCategoryFeatures(q);
		extractLengthFeatures(q);
		extractTenseFeatures(q);
		extractVaguenessFeatures(q);
		extractLangModelFeatures(q);
	}



	private void extractWHFeatures(Question q) {
		Tree tree = q.getTree();
		
		String yield = AnalysisUtilities.getCleanedUpYield(tree).toLowerCase();
		
		//if the question is a do question, none of these should fire
		if(yield.indexOf("did") == 0){
			return;
		}

		//Note: these features simply check whether a question contains 
		//one of the WH words, not necessarily whether the question is
		//a WH question of that type (e.g., "when" would fire for 
		//"What did John see WHEN he went to the park?")
		if(Question.getFeatureNames().contains("whQuestionWho")){
			if(yield.indexOf("who") != -1){
				q.setFeatureValue("whQuestionWho", 1.0);
			}
		}

		if(Question.getFeatureNames().contains("whQuestionWhat")){
			if(yield.indexOf("what") != -1 && yield.indexOf("what kind") == -1){
				q.setFeatureValue("whQuestionWhat", 1.0);
			}
		}
		

		if(Question.getFeatureNames().contains("whQuestionWhere")){
			if(yield.indexOf("where") != -1){
				q.setFeatureValue("whQuestionWhere", 1.0);
			}
		}

		if(Question.getFeatureNames().contains("whQuestionWhen")){
			if(yield.indexOf("when") != -1){
				q.setFeatureValue("whQuestionWhen", 1.0);
			}
		}

		if(Question.getFeatureNames().contains("whQuestionWhose")){
			if(yield.indexOf("whose") != -1){
				q.setFeatureValue("whQuestionWhose", 1.0);
			}
		}

		if(Question.getFeatureNames().contains("whQuestionHowMuch")){
			if(yield.indexOf("how much") != -1){
				q.setFeatureValue("whQuestionHowMuch", 1.0);
			}
		}

		if(Question.getFeatureNames().contains("whQuestionHowMany")){
			if(yield.indexOf("how many") != -1){
				q.setFeatureValue("whQuestionHowMany", 1.0);
			}
		}

		/*
		if(Question.getFeatureNames().contains("whQuestionPrep")){
			//is the question from a prepositional phrase
			tregexOpStr = "ROOT < (SBARQ << WHPP)";
			if(TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find()){
				q.setFeatureValue("whQuestionPrep", 1.0);
			}
		}
		*/
	}



	private void extractVaguenessFeatures(Question q) {
		Tree tree = q.getTree();
		Tree sourceTree = q.getSourceTree();
		Tree answerPhraseTree = q.getAnswerPhraseTree();

		//number of potentially vague NPs like pronouns "the company
		if(Question.getFeatureNames().contains("numVagueNPsSource")){			SpecificityAnalyzer.getInstance().analyze(sourceTree);			extractCountAndGreaterThanFeatures(q, "numVagueNPsSource", 1, 5, SpecificityAnalyzer.getInstance().getNumVagueNPs());
		};

		if(Question.getFeatureNames().contains("numVagueNPsQuestion")){				SpecificityAnalyzer.getInstance().analyze(tree);			extractCountAndGreaterThanFeatures(q, "numVagueNPsQuestion", 1, 5, SpecificityAnalyzer.getInstance().getNumVagueNPs());		}

		if(answerPhraseTree != null){
			if(Question.getFeatureNames().contains("numVagueNPsAnswer")){					SpecificityAnalyzer.getInstance().analyze(answerPhraseTree);				extractCountAndGreaterThanFeatures(q, "numVagueNPsAnswer", 1, 5, SpecificityAnalyzer.getInstance().getNumVagueNPs());
			}

		}
	}



	private void extractTenseFeatures(Question q) {
		Tree tree = q.getTree();

		String tregexOpStr;
		//main verb tense

		//past
		if(Question.getFeatureNames().contains("mainVerbPast")){
			boolean mainVerbPast = false;
			tregexOpStr = "ROOT < (SBARQ < (SQ !< VB|VBD|MD|VBP|VBZ < (VP < VBD)))";
			mainVerbPast |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SBARQ < (SQ < VBD))";
			mainVerbPast |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SQ  < VBD)";
			mainVerbPast |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			q.setFeatureValue("mainVerbPast", mainVerbPast? 1.0 : 0.0);
		}

		//present
		if(Question.getFeatureNames().contains("mainVerbPresent")){
			boolean mainVerbPresent = false;
			tregexOpStr = "ROOT < (SBARQ < (SQ !< VB|VBD|MD|VBP|VBZ < (VP < VB|VBP|VBZ)))";
			mainVerbPresent |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SBARQ < (SQ < VB|VBP|VBZ))";
			mainVerbPresent |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SQ  < VB|VBP|VBZ)";
			mainVerbPresent |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			q.setFeatureValue("mainVerbPresent", mainVerbPresent? 1.0 : 0.0);
		}

		//future
		if(Question.getFeatureNames().contains("mainVerbFuture")){
			boolean mainVerbFuture = false;
			tregexOpStr = "ROOT < (SBARQ < (SQ !< VB|VBD|MD|VBP|VBZ < (VP < (MD < will|shall))))";
			mainVerbFuture |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SBARQ < (SQ < (MD < will|shall)))";
			mainVerbFuture |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SQ  < (MD < will|shall))";
			mainVerbFuture |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			q.setFeatureValue("mainVerbFuture", mainVerbFuture? 1.0 : 0.0);
		}

		//copula
		if(Question.getFeatureNames().contains("mainVerbCopula")){
			boolean mainVerbCopula = false;
			tregexOpStr = "ROOT < (SBARQ < (SQ <+(VP) (/VB.?/ < is|are|was|were|am|been|being)))";
			mainVerbCopula |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SBARQ < (SQ < (/VB.?/ < is|are|was|were|am)))";
			mainVerbCopula |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			tregexOpStr = "ROOT < (SQ  << (/VB.?/ !> S < is|are|was|were|am|been|being))";
			mainVerbCopula |= TregexPatternFactory.getPattern(tregexOpStr).matcher(tree).find();
			q.setFeatureValue("mainVerbCopula", mainVerbCopula? 1.0 : 0.0);
		}
	}



	private void extractLengthFeatures(Question q) {
		Tree answerPhraseTree = q.getAnswerPhraseTree();
		Tree tree = q.getTree();
		Tree sourceTree = q.getSourceTree();

		if(Question.getFeatureNames().contains("lengthQuestion")) extractCountAndGreaterThanFeatures(q, "lengthQuestion", 4, 10, tree.yield().size());
		if(Question.getFeatureNames().contains("lengthSource")) extractCountAndGreaterThanFeatures(q, "lengthSource", 4, 10, sourceTree.yield().size());

		if(answerPhraseTree != null){			
			//answer phrase length
			if(Question.getFeatureNames().contains("lengthAnswerPhrase")) extractCountAndGreaterThanFeatures(q, "lengthAnswerPhrase", 4, 10, answerPhraseTree.yield().size());
		}
	}



	private void extractGrammarCategoryFeatures(Question q) {
		Tree tree = q.getTree();
		//Tree sourceTree = q.getSourceTree();
		Tree answerPhraseTree = q.getAnswerPhraseTree();

		//number unique noun phrases in the question
		if(Question.getFeatureNames().contains("numNPsQuestion")) extractCountAndGreaterThanFeatures(q, "numNPsQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("NP !> NP", tree));
		//proper nouns
		if(Question.getFeatureNames().contains("numProperNounsQuestion")) extractCountAndGreaterThanFeatures(q, "numProperNounsQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^NNP/", tree));
		//quantities
		if(Question.getFeatureNames().contains("numQuantitiesQuestion")) extractCountAndGreaterThanFeatures(q, "numQuantitiesQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("CD|QP", tree));
		//adjectives
		if(Question.getFeatureNames().contains("numAdjectivesQuestion")) extractCountAndGreaterThanFeatures(q, "numAdjectivesQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^JJ/", tree));
		//adverbs
		if(Question.getFeatureNames().contains("numAdverbsQuestion")) extractCountAndGreaterThanFeatures(q, "numAdverbsQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^RB/", tree));
		//prepositional phrases
		if(Question.getFeatureNames().contains("numPPsQuestion")) extractCountAndGreaterThanFeatures(q, "numPPsQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("PP", tree));
		//num subordinate clauses
		if(Question.getFeatureNames().contains("numSubordinateClausesQuestion")) extractCountAndGreaterThanFeatures(q, "numSubordinateClausesQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("SBAR", tree));
		//conjunctions
		if(Question.getFeatureNames().contains("numConjunctionsQuestion")) extractCountAndGreaterThanFeatures(q, "numConjunctionsQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("CC", tree));
		//pronouns
		if(Question.getFeatureNames().contains("numPronounsQuestion")) extractCountAndGreaterThanFeatures(q, "numPronounsQuestion", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^PRP/", tree));


		if(answerPhraseTree != null){			
			//counts of different parts of speech, etc. in the answer phrase:
			//noun phrases
			if(Question.getFeatureNames().contains("numNPsAnswer")) extractCountAndGreaterThanFeatures(q, "numNPsAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("NP !> NP", answerPhraseTree));
			//proper nouns
			if(Question.getFeatureNames().contains("numProperNounsAnswer")) extractCountAndGreaterThanFeatures(q, "numProperNounsAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^NNP/", answerPhraseTree));
			//quantities
			if(Question.getFeatureNames().contains("numQuantitiesAnswer")) extractCountAndGreaterThanFeatures(q, "numQuantitiesAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("CD|QP", answerPhraseTree));
			//adjectives
			if(Question.getFeatureNames().contains("numAdjectivesAnswer")) extractCountAndGreaterThanFeatures(q, "numAdjectivesAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^JJ/", answerPhraseTree));
			//adverbs
			if(Question.getFeatureNames().contains("numAdverbsAnswer")) extractCountAndGreaterThanFeatures(q, "numAdverbsAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^RB/", answerPhraseTree));
			//prepositional phrases
			if(Question.getFeatureNames().contains("numPPsAnswer")) extractCountAndGreaterThanFeatures(q, "numPPsAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("PP", answerPhraseTree));
			//num subordinate clauses
			if(Question.getFeatureNames().contains("numSubordinateClausesAnswer")) extractCountAndGreaterThanFeatures(q, "numSubordinateClausesAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("SBAR", answerPhraseTree));
			//conjunctions
			if(Question.getFeatureNames().contains("numConjunctionsAnswer")) extractCountAndGreaterThanFeatures(q, "numConjunctionsAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("CC", answerPhraseTree));
			//pronouns
			if(Question.getFeatureNames().contains("numPronounsAnswer")) extractCountAndGreaterThanFeatures(q, "numPronounsAnswer", 1, 5, AnalysisUtilities.getNumberOfMatchesInTree("/^PRP/", answerPhraseTree));
		}
	}



	private void extractLangModelFeatures(Question q) {
		Tree tree = q.getTree();
		Tree sourceTree = q.getSourceTree();
		Tree answerPhraseTree = q.getAnswerPhraseTree();

		List<String> sourceTokens = extractTokensFromTree(sourceTree);
		List<String> questionTokens = extractTokensFromTree(tree);

		double trigramSource = getForwardLM().logBase10ProbabilityOfSentence(sourceTokens); 
		double trigramQuestion = getForwardLM().logBase10ProbabilityOfSentence(questionTokens);
		double meanUnigramSource = getForwardLM().meanUnigramLogBase10Probability(sourceTokens);
		double meanUnigramQuestion = getForwardLM().meanUnigramLogBase10Probability(questionTokens);

		if(Question.getFeatureNames().contains("normalizedTrigramLMSource")) q.setFeatureValue("normalizedTrigramLMSource", trigramSource/sourceTokens.size());
		if(Question.getFeatureNames().contains("normalizedTrigramLMQuestion")) q.setFeatureValue("normalizedTrigramLMQuestion", trigramQuestion/questionTokens.size());
		//mean unigram frequency of words in source sentence
		if(Question.getFeatureNames().contains("normalizedUnigramLMSource")) q.setFeatureValue("normalizedUnigramLMSource", meanUnigramSource);
		//mean unigram frequency of words in question
		if(Question.getFeatureNames().contains("normalizedUnigramLMQuestion")) q.setFeatureValue("normalizedUnigramLMQuestion", meanUnigramQuestion);

		if(Question.getFeatureNames().contains("trigramLMSource")) q.setFeatureValue("trigramLMSource", trigramSource);
		if(Question.getFeatureNames().contains("trigramLMQuestion")) q.setFeatureValue("trigramLMQuestion", trigramQuestion);
		//mean unigram frequency of words in source sentence
		if(Question.getFeatureNames().contains("unigramLMSource")) q.setFeatureValue("unigramLMSource", meanUnigramSource * sourceTokens.size());
		//mean unigram frequency of words in question
		if(Question.getFeatureNames().contains("unigramLMQuestion")) q.setFeatureValue("unigramLMQuestion", meanUnigramQuestion * questionTokens.size());

		if(answerPhraseTree != null){
			List<String> answerTokens = extractTokensFromTree(answerPhraseTree);//.yield().toString().split("\\s+");

			double trigramAnswer = getForwardLM().logBase10ProbabilityOfSentence(answerTokens);
			double meanUnigramAnswer = getForwardLM().meanUnigramLogBase10Probability(answerTokens);

			if(Question.getFeatureNames().contains("normalizedTrigramLMAnswer")) q.setFeatureValue("normalizedTrigramLMAnswer", trigramAnswer / answerTokens.size());
			if(Question.getFeatureNames().contains("normalizedUnigramLMAnswer")) q.setFeatureValue("normalizedUnigramLMAnswer", meanUnigramAnswer);
			if(Question.getFeatureNames().contains("trigramLMAnswer")) q.setFeatureValue("trigramLMAnswer", trigramAnswer);
			if(Question.getFeatureNames().contains("unigramLMAnswer")) q.setFeatureValue("unigramLMAnswer", meanUnigramAnswer * answerTokens.size());
		}
	}


	private List<String> extractTokensFromTree(Tree tree) {
		List<String> res = new ArrayList<String>();
		List<Tree> leaves = tree.getLeaves();

		for(Tree leaf: leaves){
			res.add(leaf.yield().toString());
		}

		return res;
	}



	public List<String> extractTokensBeforeAnswer(List<String> intermediateToks, List<String> answerToks) {
		List<String> res = new ArrayList<String>();

		int start = -1;
		for(int i=0; i<intermediateToks.size() && start==-1; i++){
			for(int j=0; j<answerToks.size() && start==-1; j++){
				if(!intermediateToks.get(i+j).equalsIgnoreCase(answerToks.get(j))){
					break;
				}else if(j==answerToks.size()-1){
					start = i;
				}
			}
		}

		if(start == -1) return res;

		for(int k=0; k<start; k++){
			res.add(intermediateToks.get(k));
		}

		return res;
	}



	/**
	 * extract a set of features based on whether the given value is greater than 
	 * a specified number of thresholds 
	 * 
	 * @param featureValue
	 * @param featurePrefix
	 * @param increment
	 * @param numThresholds
	 */
	public static void extractCountAndGreaterThanFeatures(Question q, String featurePrefix, double increment, double numThresholds, double featureValue) {
		q.setFeatureValue(featurePrefix, featureValue);
		for(int i=0; i<numThresholds*increment; i+=increment){
			if(featureValue > i){
				q.setFeatureValue(featurePrefix+"GreaterThan"+i, 1.0);
			}
		}
	}


	public static QuestionFeatureExtractor getInstance() {
		if(instance == null){
			instance = new QuestionFeatureExtractor();
		}
		return instance;
	}

}
