package edu.cmu.ark.tests;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.InitialTransformationStep;
import edu.cmu.ark.Question;
import edu.cmu.ark.QuestionRanker;
import edu.cmu.ark.QuestionTransducer;
import edu.stanford.nlp.trees.Tree;
import junit.framework.TestCase;

public class TestCheckForChanges extends TestCase{
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
	
	
	/**
	 * for the stanford parser, use --maxLength 40 and the EnglishFactored model
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void testGenerateFromWikipediaArticle() throws IOException, ClassNotFoundException{
		String doc = "";
		String articlePath = "article461.Mary_II_of_England.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("tests"+File.separator+articlePath)));
		String buf;
		
		while((buf = br.readLine()) != null){
			if(buf.matches("^.*\\S.*$")){
				doc += buf + " ";
			}else{
				doc += "\n";
			}
		}
		
		List<String> sentences = AnalysisUtilities.getSentences(doc);
		
		//iterate over each segmented sentence and generate questions
		List<Tree> inputTrees = new ArrayList<Tree>();
		Tree parsed;
		
		for(String sentence: sentences){
			parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
			inputTrees.add(parsed);
		}
		
		//step 1 transformations
		InitialTransformationStep trans = new InitialTransformationStep();
		List<Question> transformationOutput = trans.transform(inputTrees);
		
		//step 2 question transducer
		List<Question> outputQuestionList = new ArrayList<Question>();
		for(Question t: transformationOutput){
			qt.generateQuestionsFromParse(t);
			outputQuestionList .addAll(qt.getQuestions());
		}			
		
		//remove duplicates
		QuestionTransducer.removeDuplicateQuestions(outputQuestionList);
		
		//step 3 ranking
		String modelPath = "models" + File.separator + "linear-regression-ranker-reg500.ser.gz";
		QuestionRanker qr = new QuestionRanker();
		qr.loadModel(modelPath);
		qr.scoreGivenQuestions(outputQuestionList);
		boolean doStemming = true;
		QuestionRanker.adjustScores(outputQuestionList, inputTrees, true, true, true, doStemming);
		QuestionRanker.sortQuestions(outputQuestionList, false);
		
		
		//now print the questions
		for(Question question: outputQuestionList){
			System.err.println(AnalysisUtilities.getCleanedUpYield(question.getTree())+"\t"+question.getScore());
		}
		
		//ObjectOutputStream outstream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream("tests"+File.separator+articlePath+".questions")));
		//outstream.writeObject(outputQuestionList);
		//outstream.close();
		
		ObjectInputStream instream = new ObjectInputStream(new GZIPInputStream(new FileInputStream("tests"+File.separator+articlePath+".questions")));
		List<Question> savedQuestions = (List<Question>) instream.readObject();
		instream.close();
			
		int numDiff = 0;
		int numDiffScore = 0;
		List<String> diffs = new ArrayList<String>();
		List<String> diffScores = new ArrayList<String>();
		
		assertTrue("#saved="+savedQuestions.size()+", #output="+outputQuestionList.size(), outputQuestionList.size()==savedQuestions.size());
		for(int i=0; i<outputQuestionList.size(); i++){
			Question q1 = outputQuestionList.get(i);
			Question q2 = null;
			boolean found = false;
			for(int j=0; j<savedQuestions.size(); j++){
				q2 = savedQuestions.get(j);
				if(q1.toString().equals(q2.toString())){
					found = true;
					break;
				}
			}
			if(!found){
				numDiff++;
				diffs.add(q1.toString());
			}else if(q1.getScore() != q2.getScore()){
				numDiffScore++;
				diffScores.add(q1.getScore()+"\t"+q1.yield()+"\t"+q2.getScore()+q2.yield());
			}
		}
		
		assertTrue(numDiff+"\t"+diffs.toString(), numDiff== 0);
		assertTrue(numDiffScore+"\t"+diffScores.toString(), numDiffScore == 0);
	}
	
}

