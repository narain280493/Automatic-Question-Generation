package edu.cmu.ark;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.*;

import edu.cmu.ark.ranking.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


/**
 * This class predicts a set/vector of values corresponding to different aspects of a question 
 * (e.g., grammaticality, "makes sense", use of the correct WH word, etc.).
 * 
 * This class constitutes "stage 3" as discussed in the original technical report on the system. 
 * 
 *  
 * @author mheilman@cs.cmu.edu
 *
 */
public class QuestionRanker implements Serializable{
	private static double minCountForFreqWords = 5.0;
	private static double minProportionForFreqWords = 0.05;
	private IRanker ranker;
	private static final long serialVersionUID = 15632527517313745L;
	private double minimumAcceptability = 3.5;
	
	
	public QuestionRanker(String rankerType){
		ranker = RankerFactory.createRanker(rankerType);
	}

	
	
	public QuestionRanker() {
		ranker = null;
	}



	public Set<String> getArticleIDList(List<Question> questions){
		Set<String> res = new HashSet<String>();
		
		for(Question q: questions){
			res.add(q.getSourceArticleName());
		}
		
		return res;
	}
	
		
	public void saveModel(String modelPath) {
		try {

			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			out.writeObject(ranker);
			out.flush();
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void loadModel(String modelPath){
		try {
			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			ranker = (IRanker) in.readObject();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static Comparator<Question> questionComparatorWithSentenceOrdering = new Comparator<Question>(){
		
		public int compare(Question q1, Question q2) {
			int comparison = 0;
			Integer sentnum1, sentnum2;
			Double score1, score2;
			
			sentnum1 = q1.getSourceSentenceNumber();
			sentnum2 = q2.getSourceSentenceNumber();
			
			comparison = sentnum1.compareTo(sentnum2);
			if(comparison == 0){
				score1 = q1.getScore();
				score2 = q2.getScore();
				comparison = score2.compareTo(score1); 
			}
				
			return comparison;
		}
	};
	
	
	/**
	 * Generates predictions for the data in featureLists. 
	 */
	public void rank(List<Question> questions){
		List<Rankable> unranked = new ArrayList<Rankable>();
		for(Question q: questions){
			unranked.add(createRankableFromQuestion(q));
		}
		ranker.rank(unranked);
	}
	
	
	
	/**
	 * ranks a set of questions.  For use with new questions, not for evaluation.
	 * 
	 * @param givenQuestions
	 */
	public void scoreGivenQuestions(List<Question> questions) {
		List<List<Rankable>> lists = createQuestionLists(questions);
		ranker.rankAll(lists);
		
		for(List<Rankable> list: lists){
			for(Rankable r: list){
				((Question)r.pointer1).setScore(r.score);
			}
		}
		
	}
	

	/**
	 * Loads training or testing data from a file.
	 * The format is:
	 * -one (question) instance per line
	 * -tab-delimited
	 * -the first column is the question yield (string).
	 * -the second column indicates an article ID, to group the questions by article
	 * -the next column corresponds to the label
	 * -the rest of the columns are feature values
	 * 
	 */
	public List<Question> loadQuestionDataWithFeatures(BufferedReader br){
		List<Question> questions = new ArrayList<Question>();
		Question question;
		String [] parts;
		String buf;
		List<Double> tmpFeatures;
		int i;
		String articleID;
		String yield;

		try {
			while((buf = br.readLine()) != null){
				parts = buf.split("\\t");
				tmpFeatures = new ArrayList<Double>();

				articleID = new String(parts[1]);
				yield = new String(parts[0]);

				i=2;
				Double tmpLabelScore = new Double(parts[i]);
				i++;

				for(; i < parts.length; i++){
					parts[i] = parts[i].replaceAll(",", "");
					tmpFeatures.add(new Double(parts[i]));
				}

				//add to list
				Tree tmptree = AnalysisUtilities.getInstance().readTreeFromString("(ROOT (. .))");
				question = new Question(tmptree);
				question.setYield(yield);
				question.setLabelScore(tmpLabelScore);
				question.setFeatureValues(tmpFeatures);
				question.setSourceArticleName(articleID);
				questions.add(question);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return questions;
	}
	
	
	public List<Question> loadQuestionDataWithTrees(BufferedReader br){
		List<Question> questions = new ArrayList<Question>();
		Question question;
		String [] parts;
		String buf;		
		
		try {
			buf = br.readLine(); //skip header
			
			while((buf = br.readLine()) != null){
				parts = buf.split("\\t");
	
				Double tmpLabelScore = new Double(parts[0]);
				
				Tree questionTree = AnalysisUtilities.getInstance().readTreeFromString(parts[2]);
				Tree answerPhraseTree = AnalysisUtilities.getInstance().readTreeFromString(parts[4]);
				Tree intermediateTree = AnalysisUtilities.getInstance().readTreeFromString(parts[6]);
				Tree sourceTree = AnalysisUtilities.getInstance().readTreeFromString(parts[8]);
				String articleID = parts[9]; 
				
				question = new Question();
				question.setLabelScore(tmpLabelScore);
				
				question.setAnswerPhraseTree(answerPhraseTree);
				question.setIntermediateTree(intermediateTree);
				question.setTree(questionTree);
				
				question.setSourceTree(sourceTree);
				question.setSourceArticleName(articleID);
				
				double removedAppositives =  new Double(parts[12]);
				double removedParentheticals =  new Double(parts[13]);
				double removedLeadModifyingPhrases =  new Double(parts[14]);
				double removedVerbalModifiersAfterCommas =  new Double(parts[15]);
				if(removedAppositives > 0 || removedParentheticals > 0 || removedLeadModifyingPhrases > 0 || removedVerbalModifiersAfterCommas > 0){
					if(Question.getFeatureNames().contains("removedNestedElements")) question.setFeatureValue("removedNestedElements", new Double(parts[10]));
				}
				
				if(Question.getFeatureNames().contains("isSubjectMovement")) question.setFeatureValue("isSubjectMovement", new Double(parts[10]));
				if(Question.getFeatureNames().contains("whQuestion")) question.setFeatureValue("whQuestion", new Double(parts[11]));
				if(Question.getFeatureNames().contains("removedAppositives")) question.setFeatureValue("removedAppositives", removedAppositives);
				if(Question.getFeatureNames().contains("removedParentheticals")) question.setFeatureValue("removedParentheticals", removedParentheticals);
				if(Question.getFeatureNames().contains("removedLeadModifyingPhrases")) question.setFeatureValue("removedLeadModifyingPhrases", removedLeadModifyingPhrases);
				if(Question.getFeatureNames().contains("removedVerbalModifiersAfterCommas")) question.setFeatureValue("removedVerbalModifiersAfterCommas", removedVerbalModifiersAfterCommas);
				if(Question.getFeatureNames().contains("extractedFromConjoinedPhrases")) question.setFeatureValue("extractedFromConjoinedPhrases", new Double(parts[16]));
				if(Question.getFeatureNames().contains("extractedFromFiniteClause")) question.setFeatureValue("extractedFromFiniteClause", new Double(parts[17]));
				if(Question.getFeatureNames().contains("extractedFromAppositive")) question.setFeatureValue("extractedFromAppositive", new Double(parts[18]));
				if(Question.getFeatureNames().contains("extractedFromRelativeClause")) question.setFeatureValue("extractedFromRelativeClause", new Double(parts[19]));
				if(Question.getFeatureNames().contains("extractByMovingLeadingModifiers")) question.setFeatureValue("extractByMovingLeadingModifiers", new Double(parts[20]));
				if(Question.getFeatureNames().contains("extractedFromParticipial")) question.setFeatureValue("extractedFromParticipial", new Double(parts[21]));
				if(Question.getFeatureNames().contains("performedNPClarification")) question.setFeatureValue("performedNPClarification", new Double(parts[22]));
				
				QuestionFeatureExtractor.getInstance().extractFinalFeatures(question);
				questions.add(question);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return questions;
	}
	
	
	public List<Question> findQuestionsAtOrAboveThreshold(List<Question> input, double threshold) {
		List<Question> res = new ArrayList<Question>();

		for(Question q: input){
			if(q.getScore() >= threshold) res.add(q);
		}

		return res;
	}
	
		
	private void test(List<List<Rankable>> list) {
		ranker.rankAll(list);
		System.out.println(RankingEval.precisionAtN(list, 10, minimumAcceptability)+"\t"
				+RankingEval.precisionAtN(list, 4, minimumAcceptability)+"\t"
				+RankingEval.precisionAtN(list, 1, minimumAcceptability)+"\t"
				+RankingEval.computeKendallsTau(list)+"\t"
				+RankingEval.computeMAP(list, minimumAcceptability));
		
	}

	
	private void leaveOneOutCrossValidate(List<List<Rankable>> lists) {	
		List<List<Rankable>> trainingFolds = new ArrayList<List<Rankable>>();
		List<List<Rankable>> testingFold = new ArrayList<List<Rankable>>();
		for(int i=0;i<lists.size();i++){
			trainingFolds.clear();
			trainingFolds.addAll(lists);
			trainingFolds.remove(i);
			testingFold.clear();
			testingFold.add(lists.get(i));
			
			if(trainingFolds.contains(testingFold.get(0))){
				System.exit(0);
			}
			
			ranker.train(trainingFolds);
			System.out.print(i+"\t");
			test(testingFold);
			
		}
	}
		
	

	public void setMinimumAcceptability(double minimumAcceptability) {
		this.minimumAcceptability = minimumAcceptability;
	}


	private List<List<Rankable>> createQuestionLists(List<Question> questions) {
		List<List<Rankable>> res = new ArrayList<List<Rankable>>();

		for(String articleID: getArticleIDList(questions)){
			List<Rankable> list = new ArrayList<Rankable>();
			for(Question q: questions){
				if(q.getSourceArticleName().equals(articleID)){
					Rankable r = createRankableFromQuestion(q);
					list.add(r);
				}
			}
			res.add(list);
		}
		return res;
	}


	private Rankable createRankableFromQuestion(Question q) {
		Rankable r = new Rankable();
		r.label = q.getLabelScore();
		r.features = RankingUtils.convertToArray(q.featureValueList());		
		r.pointer1 = q;
		return r;
	}
	
	
	public IRanker getRanker(){
		return ranker;
	}
	
	
	public static void adjustScores(List<Question> questions, List<Tree> parsedSentences, boolean avoidFreqWords,
			boolean preferWH, boolean downweightPronouns, boolean doStemming)
	{

		if(avoidFreqWords){
			List<String> wordTokens = BagOfWordsExtractor.getInstance().extractNounTokensFromTrees(parsedSentences);
			Map<String, Double> typeCounts = BagOfWordsExtractor.getInstance().extractCounts(wordTokens);
			
			if(GlobalProperties.getDebug()) System.err.println("Frequent Words: "+findFrequentWords(typeCounts, wordTokens.size()).toString());
			
			//downweight any questions whose answer's syntactic head word (or for PPs, the pp-object's head)
			// appears at least 5 times 
			// and constitutes at least 5% of the non-stopword nouns in the text
			double threshold = Math.max(minCountForFreqWords, minProportionForFreqWords*wordTokens.size());	
			for(Question q: questions){
				Tree answerTree = q.getAnswerPhraseTree();
				if(answerTree == null) continue;
				String headWord = extractHeadNounToken(answerTree).toLowerCase();
				if(doStemming) headWord = PorterStemmer.getInstance().stem(headWord);
				if(typeCounts.containsKey(headWord) && typeCounts.get(headWord) >= threshold){
					q.setScore(q.getScore() - 1.0);
					q.setFeatureValue("answerIsFrequentWord", 1.0);
					if(GlobalProperties.getDebug()) System.err.println("Question Ranker: downweighting due to frequent word ("+headWord+") in answer: " + q.yield());
				}
			}
		}
		
		if(preferWH){
			for(Question q: questions){
				if(q.getFeatureValue("whQuestion") == 0.0){
					q.setScore(q.getScore()-1.0);
				}
			}
		}
		
		if(downweightPronouns){
			for(Question q: questions){
				Tree answerTree = q.getAnswerPhraseTree();
				if(QuestionTransducer.containsUnresolvedPronounsOrDemonstratives(q)){
					if(GlobalProperties.getDebug()) System.err.println("Question Ranker: downweighting due to pronoun in question: " + q.yield() );
					q.setScore(q.getScore()-1.0);
				}else if(answerTree != null && isHeadedByPronoun(answerTree)){
					q.setScore(q.getScore() - 1.0);
					q.setFeatureValue("answerIsHeadedByPronoun", 1.0);
					if(GlobalProperties.getDebug()) System.err.println("Question Ranker: downweighting due to pronoun answer ("+answerTree.yield()+"): " + q.yield() );
				}
				
			}
			
		}
			
	}



	private static List<String> findFrequentWords(Map<String, Double> typeCounts, int total) {
		List<String> res = new ArrayList<String>();
		
		for(String word: typeCounts.keySet()){
			double cnt = typeCounts.get(word);
			if(cnt >= minCountForFreqWords && cnt/(double)total >= minProportionForFreqWords){
				res.add(word);
			}
		}
		
		return res;
	}



	private static boolean isHeadedByPronoun(Tree tree) {
		String tregexOpStr;
		TregexPattern matchPattern;
		tregexOpStr = "NP !>> NP <<# (/^PRP.*/ < __=head)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(tree);
		return matcher.find();
	}



	private static String extractHeadNounToken(Tree tree) {
		String tregexOpStr;
		TregexPattern matchPattern;
		String res = "";
		
		tregexOpStr = "NP !>> NP <<# (NNS|NN|NNP|NNPS < __=head)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(tree);
		if(matcher.find()){
			res = matcher.getNode("head").label().toString();
		}
		
		return res;
	}



	public static void sortQuestions(List<Question> questions, boolean groupQuestionsBySourceSentence) {
		if(groupQuestionsBySourceSentence){
			Collections.sort(questions, QuestionRanker.questionComparatorWithSentenceOrdering);
			//QuestionRanker.rankAndGroupQuestionsBySourceSentence(outputQuestionList);
		}else{
			Collections.sort(questions);
			Collections.reverse(questions);
		}
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException{
		QuestionRanker r;

		String trainFile = null;
		String testFile = null;
		BufferedReader br;
		String loadModelPath = null;
		String saveModelPath = null;
		boolean crossValidate = false;
		double minimumAcceptability = 3.5;
		String saveQuestionsPath = null;
		String loadTrainingQuestionsPath = null;
		String rankerType = "linear-regression";
		List<String> paramNames = new ArrayList<String>();
		List<Double> paramValues = new ArrayList<Double>();
		
		for(int i=0;i<args.length;i++){
			if(args[i].equals("--trainfile")){
				trainFile = args[i+1];
				i++;
			}else if(args[i].equals("--testfile")){
				testFile = args[i+1];
				i++;
			}else if(args[i].equals("--debug")){
				GlobalProperties.setDebug(true);	
			}else if(args[i].equals("--load-model")){
				loadModelPath = args[i+1]; 
				i++;
			}else if(args[i].equals("--save-model")){
				saveModelPath = args[i+1]; 
				i++;
			}else if(args[i].equals("--cv")){
				crossValidate = true;
			}else if(args[i].equals("--min")){
				minimumAcceptability = new Double(args[i+1]);
				i++;
			}else if(args[i].equals("--properties")){  
				GlobalProperties.loadProperties(args[i+1]);
			}else if(args[i].equals("--save-questions")){
				saveQuestionsPath = args[i+1];
				i++;
			}else if(args[i].equals("--load-questions")){
				loadTrainingQuestionsPath = args[i+1];
				i++;
			}else if(args[i].equals("--type")){
				rankerType = args[i+1];
				i++;
			}else if(args[i].startsWith("--")){
				paramNames.add(args[i].substring(2));
				paramValues.add(new Double(args[i+1]));
				i++;
			}
			
		}
		

		r = new QuestionRanker();
		if(loadModelPath != null){
			r.loadModel(loadModelPath);
		}else if(trainFile == null && loadTrainingQuestionsPath == null){
			System.err.println("no training file!");
			System.exit(0);
		}else{
			r = new QuestionRanker(rankerType);
			for(int i=0;i<paramNames.size();i++){
				r.getRanker().setParameter(paramNames.get(i), paramValues.get(i));
			}
		}
		
		r.setMinimumAcceptability(minimumAcceptability);
		
		List<Question> trainingQuestions = null;
		
		if(loadTrainingQuestionsPath != null){
			//load serialized lists of Question objects
			try {
				ObjectInputStream instream = new ObjectInputStream(new GZIPInputStream(new FileInputStream(loadTrainingQuestionsPath)));
				trainingQuestions = (List<Question>) instream.readObject();
				instream.close();
				//recalculate features
				for(Question q:trainingQuestions){
					q.removeUnusedFeatures();
					QuestionFeatureExtractor.getInstance().extractFinalFeatures(q);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else if(loadModelPath == null){
			br = new BufferedReader(new InputStreamReader(new FileInputStream(trainFile)));
			trainingQuestions = r.loadQuestionDataWithFeatures(br);
			//trainingQuestions = r.loadQuestionDataWithTrees(br);
		}
		
		if(saveQuestionsPath != null){
			//save questions
			ObjectOutputStream outstream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(saveQuestionsPath)));
			outstream.writeObject(trainingQuestions);
			outstream.close();
		}
		
		if(crossValidate){				
			r.leaveOneOutCrossValidate(r.createQuestionLists(trainingQuestions));
		}else if(loadModelPath == null && trainingQuestions != null){	
			r.getRanker().train(r.createQuestionLists(trainingQuestions));
			
			if(saveModelPath != null){
				r.saveModel(saveModelPath);
			}
		}
		
		if(testFile != null){
			br = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));

			List<Question> testQuestions = null;
			testQuestions = r.loadQuestionDataWithFeatures(br);
			//testQuestions = r.loadQuestionDataWithTrees(br);
						
			r.test(r.createQuestionLists(testQuestions));
		}			
	}

}

