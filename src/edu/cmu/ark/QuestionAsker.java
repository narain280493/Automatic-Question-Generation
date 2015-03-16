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


// Adding OpenAryhype to the build path of this project also works!

///import info.ephyra.nlp.StanfordParser;
//import info.ephyra.nlp.TreeUtil;

import java.io.*;
//import java.text.NumberFormat;
import java.util.*;

import ComprehensionQuestionGeneration.VocabularyQuestion;
import Configuration.Configuration;
import distractorgeneration.Distractor;
import distractorgeneration.DistractorFilter;
import distractorgeneration.DistractorGenerator;
import edu.stanford.nlp.trees.CollinsHeadFinder;
//import edu.cmu.ark.ranking.WekaLinearRegressionRanker;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.ParseException;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


/**
 * Wrapper class for outputting a (ranked) list of questions given an entire document,
 * not just a sentence.  It wraps the three stages discussed in the technical report and calls each in turn 
 * (along with parsing and other preprocessing) to produce questions.
 * 
 * This is the typical class to use for running the system via the command line. 
 * 
 * Example usage:
 * 
    java -server -Xmx800m -cp lib/weka-3-6.jar:lib/stanford-parser-2008-10-26.jar:bin:lib/jwnl.jar:lib/commons-logging.jar:lib/commons-lang-2.4.jar:lib/supersense-tagger.jar:lib/stanford-ner-2008-05-07.jar:lib/arkref.jar \
	edu/cmu/ark/QuestionAsker \
	--verbose --simplify --group \
	--model models/linear-regression-ranker-06-24-2010.ser.gz \
	--prefer-wh --max-length 30 --downweight-pro
 * 
 * @author mheilman@cs.cmu.edu
 *
 */
public class QuestionAsker {

	public static HashSet<String>nounPhraseSet = new HashSet<String>();
	public static boolean isNounPhraseSetPopulated = false;
	public static String INPUT_FILE_NAME;
	public static QuestionRanker qr = null;
	
	public QuestionAsker(){
		try {
			StanfordParser.initialize();
		} catch (Exception e) {
			System.out.println("Stanford Parser-Error:"+e.toString());
			//MsgPrinter.printErrorMsg("Could not create Stanford parser."+e.toString());
		}

	}
	
	
	public static void generateDistractor(String fileName,String originalAnsPhrase,String answerPhrase,String answerSentence){
		//our code for distractor generator
		int distractorCount=0;
		System.out.println("Distractor generation starts:");
		//NOTE: call POS Tagger before SST because POS Tagger will group all adjacent proper noun together
		//which is then used by SST tagger
		List<String>posDistractorList= new ArrayList<String>();
		List<String>sstDistractorList= new ArrayList<String>();
		List<Distractor> selectedDistractors = new ArrayList<Distractor>();
		List<String>posList=DistractorGenerator.getPOSTaggerDistractors(Configuration.INPUT_FILE_PATH+INPUT_FILE_NAME, answerPhrase);
		//NOTE: Call populateNounPhraseSet function only after POSTagger
		populateNounPhraseSet();
		List<String>sstList=DistractorGenerator.getSSTTaggerDistractors(Configuration.INPUT_FILE_PATH+INPUT_FILE_NAME, answerPhrase);
		sstList=DistractorFilter.removeAnswerPhraseWordsFromDistractorList(originalAnsPhrase, sstList);
		
		System.out.println("No. of SST Distractors found :"+(sstList.size()-1));
		//stage 1 SuperSenseTagger
								   
		if(sstList.size()>=2){
			distractorCount+=(sstList.size()-1);
		    
		}
		//Distractor ranking
		//substitute the answer phrase with the distractor and check the probability of N-gram using
		//microsoft bing api 
		
		
		//stage 2 POSTagger
		System.out.println("No. of POS Distractors found :"+(posList.size()-1));

		 
		posDistractorList.addAll(posList);
		sstDistractorList.addAll(sstList);
		//remove "yes" or "no" which is the first element in the sst and pos list 
		posDistractorList.remove(0);
		sstDistractorList.remove(0);
		if(isAnsPhraseProperNoun(answerPhrase)){
			System.out.println("SST distractors: ");
			for(String word:sstDistractorList){
				selectedDistractors.add(new Distractor(word, 1));
				System.out.println(word);
			}
			
		}
		else{
		System.out.println("Ranking SST distractors");
		List<Distractor> selectedSSTDistractors=DistractorGenerator.rankDistractor(answerSentence, answerPhrase, sstDistractorList);
		 if(selectedSSTDistractors!=null){
			 for(Distractor distractor:selectedSSTDistractors){
				 System.out.println(distractor.distractorWord+" "+distractor.weight);
				 selectedDistractors.add(distractor);
			 }
			 
		 }
		
		}
		if(distractorCount<3){
			 posDistractorList=DistractorFilter.removeSSTDistractorsFromPOSDistractorList(posDistractorList,sstDistractorList);
			 posDistractorList=DistractorFilter.removeAnswerPhraseWordsFromDistractorList(originalAnsPhrase, posDistractorList);
			 if(isAnsPhraseProperNoun(answerPhrase)){
					System.out.println("POS distractors: ");
					for(String word:posDistractorList){
						selectedDistractors.add(new Distractor(word, 1));
						System.out.println(word);
					}
			}
			else{
			 System.out.println("Ranking POS distractors");
			 List<Distractor> selectedPOSDistractors=DistractorGenerator.rankDistractor(answerSentence, answerPhrase, posDistractorList);
			 if(selectedPOSDistractors!=null){
				 for(Distractor distractor:selectedPOSDistractors){
					 System.out.println(distractor.distractorWord+" "+distractor.weight);
					 selectedDistractors.add(distractor);
				 }
			 }
			}
		 }
		List<String> multipleChoices=new ArrayList<String>();
		int i=1;
		for(Distractor distractor:selectedDistractors){
			if(i==3)
				break;
			multipleChoices.add(distractor.distractorWord);
			i++;
		}
		multipleChoices.add(answerPhrase);
		if(multipleChoices.size()>=3){
		Collections.shuffle(multipleChoices);
		System.out.println("****************************************************************************************");
		System.out.println("Multiple choices:");
		int choiceNumber=1;
		for(String choice:multipleChoices){
			System.out.print(choiceNumber+++")"+choice+"\t");
		}
		System.out.println();
		System.out.println("****************************************************************************************");
		}
		
	}
	public static  void getQuestionsForSentence(String sentence){
		
		System.out.println("Summary:"+sentence);
		String modelPath = "models/linear-regression-ranker-reg500.ser.gz";
		List<Tree> inputTrees = new ArrayList<Tree>();
		Tree parsed;
		InitialTransformationStep trans = new InitialTransformationStep();
		QuestionTransducer qt = new QuestionTransducer();
		boolean preferWH = false;
		Integer maxLength = 1000;
		boolean downweightPronouns = false;
		boolean avoidFreqWords = false;
		boolean justWH = false;
		
		
		List<Question> outputQuestionList = new ArrayList<Question>();
		AnalysisUtilities.getInstance();
		GlobalProperties.setDebug(true);
		
		if(modelPath != null){
			System.err.println("Loading question ranking models from "+modelPath+"...");
			qr = new QuestionRanker();
			qr.loadModel(modelPath);
		}
		
		parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
		inputTrees.add(parsed);
		
	
		System.out.println("Parse Tree-"+parsed);
		//step 1 transformations
		List<Question> transformationOutput = trans.transform(inputTrees);
		System.out.println("Step 1");
		//step 2 question transducer
		for(Question t: transformationOutput){
			if(GlobalProperties.getDebug()) System.err.println("Stage 2 Input: "+t.getIntermediateTree().yield().toString());
			qt.generateQuestionsFromParse(t);
			outputQuestionList.addAll(qt.getQuestions());
		}			
		
		//remove duplicates
		QuestionTransducer.removeDuplicateQuestions(outputQuestionList);
		System.out.println("step2");
		//step 3 ranking
		if(qr != null){
			qr.scoreGivenQuestions(outputQuestionList);
			boolean doStemming = true;
			QuestionRanker.adjustScores(outputQuestionList, inputTrees, avoidFreqWords, preferWH, downweightPronouns, doStemming);
			QuestionRanker.sortQuestions(outputQuestionList, false);
		}
		System.out.println("step3");
		//now print the questions
		
		for(Question question: outputQuestionList){
			String ansPhrase="";
			String originalAnsPhrase="";
				
			if(question.getTree().getLeaves().size() > maxLength){
				continue;
			}
			if(justWH && question.getFeatureValue("whQuestion") != 1.0){
				continue;
			}
			System.out.println("Question :"+question.yield());
			System.out.println("Score :"+question.getScore());
			Tree ansTree = question.getAnswerPhraseTree();
			if(ansTree != null){
				ansPhrase = AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree());
				System.out.println("Answer Phrase detected :"+ansPhrase);
				if (!isAnsPhraseProperNoun(ansPhrase)&&(countWords(ansPhrase)>=2)) {
					System.out.println("Resolving answerphrase to a single word...");
					String headWord=null;
					try {
						headWord = resolveHead(ansPhrase);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(headWord!=null){
						ansPhrase=headWord;
					}
				}
				System.out.println("Answer Phrase :"+ansPhrase);
				String ansSentence = question.getSourceTree().yield().toString();
				System.out.println("Answer Sentence :"+ansSentence);
				generateDistractor(INPUT_FILE_NAME, originalAnsPhrase, ansPhrase, ansSentence);
				
			}
			else{
		
			//	System.out.println("VISHNU yes/no");
				String ansSentence = question.getSourceTree().yield().toString();
				System.out.println("Answer Sentence :"+ansSentence);
				
			}
			//System.out.println("Question type : "+question.);
			//if(printVerbose) 
			
		}

		System.out.println("qg over");
	
	}
	/**
	 * @param args
	 * @return 
	 * @throws ParseException 
	 */
	/*public void getQues(String summary)
	{
		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep trans = new InitialTransformationStep();
		
		
		qt.setAvoidPronounsAndDemonstratives(false);
		
		//pre-load
		AnalysisUtilities.getInstance();
		
		String buf;
		Tree parsed;
		boolean printVerbose = true;//setting printVerbose true always
		String modelPath = "models/linear-regression-ranker-reg500.ser.gz";
		
		List<Question> outputQuestionList = new ArrayList<Question>();
		boolean preferWH = false;
		boolean doNonPronounNPC = false;
		boolean doPronounNPC = true;
		Integer maxLength = 1000;
		boolean downweightPronouns = false;
		boolean avoidFreqWords = false;
		boolean dropPro = true;
		boolean justWH = false;
		
		
				GlobalProperties.setDebug(true);
			
		qt.setAvoidPronounsAndDemonstratives(dropPro);
		trans.setDoPronounNPC(doPronounNPC);
		trans.setDoNonPronounNPC(doNonPronounNPC);
		
		if(modelPath != null){
			System.err.println("Loading question ranking models from "+modelPath+"...");
			qr = new QuestionRanker();
			qr.loadModel(modelPath);
		}
		
		try{
			
				if(GlobalProperties.getDebug()) System.err.println("\nInput TextFile:");
				String doc;
	
				String query = summary;
				System.out.println("Query read is :"+query);
				
				outputQuestionList.clear();
				
				
				
				
				//iterate over each segmented sentence and generate questions
				ArrayList<Tree> inputTrees =new ArrayList<Tree>();
				
				
					parsed = AnalysisUtilities.getInstance().parseSentence(summary).parse;
					inputTrees.add(parsed);
				
				
			//	if(GlobalProperties.getDebug()) System.err.println("Seconds Elapsed Parsing:\t"+((System.currentTimeMillis()-startTime)/1000.0));
				
				//step 1 transformations
				Question transformationOutput = (Question) trans.transform(inputTrees);
				
				
				//	if(GlobalProperties.getDebug()) System.err.println("Stage 2 Input: "+t.getIntermediateTree().yield().toString());
					qt.generateQuestionsFromParse(transformationOutput);
					outputQuestionList.addAll(qt.getQuestions());
							
				
				//remove duplicates
				QuestionTransducer.removeDuplicateQuestions(outputQuestionList);
				
				//step 3 ranking
				if(qr != null){
					qr.scoreGivenQuestions(outputQuestionList);
					boolean doStemming = true;
					QuestionRanker.adjustScores(outputQuestionList, inputTrees, avoidFreqWords, preferWH, downweightPronouns, doStemming);
					QuestionRanker.sortQuestions(outputQuestionList, false);
				}
				
				//now print the questions
				
				int questionCount=1;
				for(Question question: outputQuestionList){
					String ansPhrase="";
					String originalAnsPhrase="";
						
					if(question.getTree().getLeaves().size() > maxLength){
						continue;
					}
					if(justWH && question.getFeatureValue("whQuestion") != 1.0){
						continue;
					}
					System.out.println();
					System.out.println("=====================================================================================================");
					System.out.println();
					System.out.println("QuestionCount: "+questionCount);
					questionCount++;
					System.out.println("Question :"+question.yield());
					System.out.println("Score :"+question.getScore());
					Tree ansTree = question.getAnswerPhraseTree();
					if(ansTree != null){
						ansPhrase = AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree());
						System.out.println("Answer Phrase detected :"+ansPhrase);
						if (!isAnsPhraseProperNoun(ansPhrase)&&(countWords(ansPhrase)>=2)) {
							System.out.println("Resolving answerphrase to a single word...");
							String headWord=null;
							try {
								headWord = resolveHead(ansPhrase);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(headWord!=null){
								ansPhrase=headWord;
							}
						}
						System.out.println("Answer Phrase :"+ansPhrase);
						String ansSentence = question.getSourceTree().yield().toString();
						System.out.println("Answer Sentence :"+ansSentence);
						generateDistractor(INPUT_FILE_NAME, originalAnsPhrase, ansPhrase, ansSentence);
						
					}
					else{
				
						//System.out.println("VISHNU yes/no");
						System.out.println("Answer Phrase :"+"Yes");
						
						String ansSentence = question.getSourceTree().yield().toString();
						System.out.println("Answer Sentence :"+ansSentence);
						
					}
					//System.out.println("Question type : "+question.);
					//if(printVerbose) 
					
				}
	
			
			

			//	VocabularyQuestion.populateTagMap();
			//parent while block ends
		}//try block ends
		catch(Exception e){
			e.printStackTrace();
		}
	}*/
	public static void main(String [] args) throws ParseException {
		
		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep trans = new InitialTransformationStep();
		
		
		qt.setAvoidPronounsAndDemonstratives(false);
		
		//pre-load
		AnalysisUtilities.getInstance();
		
		String buf;
		Tree parsed;
		boolean printVerbose = true;//setting printVerbose true always
		String modelPath = null;
		
		List<Question> outputQuestionList = new ArrayList<Question>();
		boolean preferWH = false;
		boolean doNonPronounNPC = false;
		boolean doPronounNPC = true;
		Integer maxLength = 1000;
		boolean downweightPronouns = false;
		boolean avoidFreqWords = false;
		boolean dropPro = true;
		boolean justWH = false;
		
		for(int i=0;i<args.length;i++){
			if(args[i].equals("--debug")){
				GlobalProperties.setDebug(true);
			}else if(args[i].equals("--verbose")){
				printVerbose = true;
			}else if(args[i].equals("--model")){ //ranking model path
				modelPath = args[i+1]; 
				i++;
			}else if(args[i].equals("--keep-pro")){
				dropPro = false;
			}else if(args[i].equals("--downweight-pro")){
				dropPro = false;
				downweightPronouns = true;
			}else if(args[i].equals("--downweight-frequent-answers")){
				avoidFreqWords = true;
			}else if(args[i].equals("--properties")){  
				GlobalProperties.loadProperties(args[i+1]);
			}else if(args[i].equals("--prefer-wh")){  
				preferWH = true;
			}else if(args[i].equals("--just-wh")){  
				justWH = true;
			}else if(args[i].equals("--full-npc")){  
				doNonPronounNPC = true;
			}else if(args[i].equals("--no-npc")){  
				doPronounNPC = false;
			}else if(args[i].equals("--max-length")){  
				maxLength = new Integer(args[i+1]);
				i++;
			}
		}
		
		qt.setAvoidPronounsAndDemonstratives(dropPro);
		trans.setDoPronounNPC(doPronounNPC);
		trans.setDoNonPronounNPC(doNonPronounNPC);
		
		if(modelPath != null){
			System.err.println("Loading question ranking models from "+modelPath+"...");
			qr = new QuestionRanker();
			qr.loadModel(modelPath);
		}
		
		try{
			while(true){
				if(GlobalProperties.getDebug()) System.err.println("\nInput TextFile:");
				String doc;
	
				String query = readLine().trim();
				System.out.println("Query read is :"+query);
				//Right now input takes only File TODO: Do it for other types of inputstream also
				if (query.startsWith("FILE: ")||query.startsWith("file: ")) {
						// when input is the following format:
						// FILE: in.txt out.txt
						// read text from in.txt and output to out.txt
						String fileLine = query.substring(6).trim();
						String[] files = fileLine.split("\\s+");
						if (files.length != 2) {
							System.out.println("FILE field must only contain two valid files. e.g.:");
							System.out.println("FILE: input.txt output.txt");
							continue;
						}
						INPUT_FILE_NAME=files[0];
						FileReader in = new FileReader(files[0]);
					    BufferedReader br = new BufferedReader(in);	
				
			
			while(true){
				outputQuestionList.clear();
				doc = "";
				buf = "";
				
				buf = br.readLine();
				if(buf == null){
					break;
				}
				doc += buf;
				
				while(br.ready()){
					buf = br.readLine();
					if(buf == null){
						break;
					}
					if(buf.matches("^.*\\S.*$")){
						doc += buf + " ";
					}else{
						doc += "\n";
					}
				}
				if(doc.length() == 0){
					break;
				}
				Configuration.INPUT_TEXT=doc;
				System.out.println("Saving input text in Configuration.INPUT_TEXT variable");
				long startTime = System.currentTimeMillis();
				List<String> sentences = AnalysisUtilities.getSentences(doc);
				
				//iterate over each segmented sentence and generate questions
				List<Tree> inputTrees = new ArrayList<Tree>();
				
				for(String sentence: sentences){
					//if(GlobalProperties.getDebug()) System.err.println("Question Asker: sentence: "+sentence);
					
					parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
					inputTrees.add(parsed);
				}
				
			//	if(GlobalProperties.getDebug()) System.err.println("Seconds Elapsed Parsing:\t"+((System.currentTimeMillis()-startTime)/1000.0));
				
				//step 1 transformations
				List<Question> transformationOutput = trans.transform(inputTrees);
				
				//step 2 question transducer
				for(Question t: transformationOutput){
				//	if(GlobalProperties.getDebug()) System.err.println("Stage 2 Input: "+t.getIntermediateTree().yield().toString());
					qt.generateQuestionsFromParse(t);
					outputQuestionList.addAll(qt.getQuestions());
				}			
				
				//remove duplicates
				QuestionTransducer.removeDuplicateQuestions(outputQuestionList);
				
				//step 3 ranking
				if(qr != null){
					qr.scoreGivenQuestions(outputQuestionList);
					boolean doStemming = true;
					QuestionRanker.adjustScores(outputQuestionList, inputTrees, avoidFreqWords, preferWH, downweightPronouns, doStemming);
					QuestionRanker.sortQuestions(outputQuestionList, false);
				}
				
				//now print the questions
				
				int questionCount=1;
				for(Question question: outputQuestionList){
					String ansPhrase="";
					String originalAnsPhrase="";
						
					if(question.getTree().getLeaves().size() > maxLength){
						continue;
					}
					if(justWH && question.getFeatureValue("whQuestion") != 1.0){
						continue;
					}
					System.out.println();
					System.out.println("=====================================================================================================");
					System.out.println();
					System.out.println("QuestionCount: "+questionCount);
					questionCount++;
					System.out.println("Question :"+question.yield());
					System.out.println("Score :"+question.getScore());
					Tree ansTree = question.getAnswerPhraseTree();
					if(ansTree != null){
						ansPhrase = AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree());
						System.out.println("Answer Phrase detected :"+ansPhrase);
						if (!isAnsPhraseProperNoun(ansPhrase)&&(countWords(ansPhrase)>=2)) {
							System.out.println("Resolving answerphrase to a single word...");
							String headWord=null;
							try {
								headWord = resolveHead(ansPhrase);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(headWord!=null){
								ansPhrase=headWord;
							}
						}
						System.out.println("Answer Phrase :"+ansPhrase);
						String ansSentence = question.getSourceTree().yield().toString();
						System.out.println("Answer Sentence :"+ansSentence);
						generateDistractor(INPUT_FILE_NAME, originalAnsPhrase, ansPhrase, ansSentence);
						
					}
					else{
				
						//System.out.println("VISHNU yes/no");
						System.out.println("Answer Phrase :"+"Yes");
						
						String ansSentence = question.getSourceTree().yield().toString();
						System.out.println("Answer Sentence :"+ansSentence);
						
					}
					//System.out.println("Question type : "+question.);
					//if(printVerbose) 
					
				}
	
			}//child while block ends
			}

			//	VocabularyQuestion.populateTagMap();
			}//parent while block ends
		}//try block ends
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void printFeatureNames(){
		List<String> featureNames = Question.getFeatureNames();
		for(int i=0;i<featureNames.size();i++){
			if(i>0){
				System.out.print("\n");
			}
			System.out.print(featureNames.get(i));
		}
		System.out.println();
	}
	public static String HeadWordResolver(String ansPhrase) throws ParseException{
		int index=0;
		boolean ruleMatch = false;
		boolean commonnounPresent = false;
		boolean propernounPresent = false;
		
		String[] output=new String[50];
		
		String tregexMatchNounPhraseConjunctionModifier = "NP=nounphrase1$..CC=conj$..NP=nounphrase2";
		String tregexMatchProperNounConjunctionModifier ="NNP=noun1$..CC=conj$..NNP=noun2";
		String tregexMatchCommonNounModifier ="NN=commonnoun";
		String tregexMatchProperNounModifier ="NNP=propernounsingular";
		String tregexMatchProperNounPluralModifier ="NNPS=propernounplural";
		String tregexMatchNounPhraseModifier = "NP=nounphrase";
		
		TregexPattern tregexPatternMatchNounPhraseConjunctionModifier;
		TregexPattern tregexPatternMatchProperNounConjunctionModifier;
		TregexPattern tregexPatternMatchCommonNounModifier;
		TregexPattern tregexPatternMatchNounPhraseModifier;
		TregexPattern tregexPatternMatchProperNounModifier;
		TregexPattern tregexPatternMatchProperNounPluralModifier;
		
		TregexMatcher tregexPatternMatchNounPhraseConjunctionMatcher;
		TregexMatcher tregexPatternMatchProperNounConjunctionMatcher;
		TregexMatcher tregexPatternMatchCommonNounMatcher;
		TregexMatcher tregexPatternMatchNounPhraseMatcher;
		TregexMatcher tregexPatternMatchProperNounMatcher;
		
		tregexPatternMatchNounPhraseConjunctionModifier = TregexPattern.compile(tregexMatchNounPhraseConjunctionModifier);
		tregexPatternMatchProperNounConjunctionModifier = TregexPattern.compile(tregexMatchProperNounConjunctionModifier);
		tregexPatternMatchCommonNounModifier = TregexPattern.compile(tregexMatchCommonNounModifier);
		tregexPatternMatchNounPhraseModifier = TregexPattern.compile(tregexMatchNounPhraseModifier);
		tregexPatternMatchProperNounModifier = TregexPattern.compile(tregexMatchProperNounModifier);
		tregexPatternMatchProperNounPluralModifier = TregexPattern.compile(tregexMatchProperNounPluralModifier);
		
		CollinsHeadFinder headFinder = new CollinsHeadFinder();
		
		try {
			StanfordParser.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Tree tree = StanfordParser.parseTree(ansPhrase);
		System.out.println(tree);
	
		Tree npHeadTree = headFinder.determineHead(tree);
		String tag = npHeadTree.label().toString();
		if(tag.contains("PP"))
		{
			String originalString = TreeUtil.getLabel(npHeadTree);
			if(originalString.contains("for"))
			{
				originalString = originalString.replace("for","");
				System.out.println("Removed for:"+originalString);
				//System.out.println("for present");
			    tree = StanfordParser.parseTree(originalString);
				//System.out.println(tree);
			} 
			else if(originalString.contains("to"))
			{
				originalString = originalString.replace("to","");
				tree = StanfordParser.parseTree(originalString);
				System.out.println("to present");
			}
			
			//Add other prepositions here
		}
		//System.out.println("Tag"+tag);
		
		tregexPatternMatchNounPhraseConjunctionMatcher = tregexPatternMatchNounPhraseConjunctionModifier.matcher(tree);
		tregexPatternMatchProperNounConjunctionMatcher = tregexPatternMatchProperNounConjunctionModifier.matcher(tree);
		tregexPatternMatchNounPhraseMatcher = tregexPatternMatchNounPhraseModifier.matcher(tree);
		
		// Tregex Rule NP CC NP
		while (tregexPatternMatchNounPhraseConjunctionMatcher.find()) {
			System.out.println("RuleMatch: NP CC NP");
			Tree nounTree = tregexPatternMatchNounPhraseConjunctionMatcher.getNode("nounphrase1");
       		String NP1 = TreeUtil.getLabel(nounTree);
       		tregexPatternMatchCommonNounMatcher = tregexPatternMatchCommonNounModifier.matcher(nounTree);
       		while(tregexPatternMatchCommonNounMatcher.find())
       		{
       			System.out.println("NP1 has a common noun");
       			Tree commonNoun=tregexPatternMatchCommonNounMatcher.getNode("commonnoun");
       			String commonnoun = TreeUtil.getLabel(commonNoun);
       			System.out.println("CommonNoun:"+commonnoun);
       			output[index++]=commonnoun;
       			commonnounPresent=true;
           	}
       		if(commonnounPresent == false)
       		{
       			output[index++]=NP1;
       			//commonnounPresent= false;
       		}
       		
       		Tree conjTree = tregexPatternMatchNounPhraseConjunctionMatcher.getNode("conj");
       		String conjheadTag = TreeUtil.getLabel(conjTree);
       		output[index++]=conjheadTag;
       		
       		commonnounPresent= false;
       		Tree nounTree2 = tregexPatternMatchNounPhraseConjunctionMatcher.getNode("nounphrase2");
       		String NP2 = TreeUtil.getLabel(nounTree2);
       		tregexPatternMatchCommonNounMatcher = tregexPatternMatchCommonNounModifier.matcher(nounTree2);
       		while(tregexPatternMatchCommonNounMatcher.find())
       		{
       			System.out.println("NP2 has a common noun");
       			Tree commonNoun=tregexPatternMatchCommonNounMatcher.getNode("commonnoun");
       			String commonnoun = TreeUtil.getLabel(commonNoun);
       			System.out.println("CommonNoun:"+commonnoun);
       			output[index++]=commonnoun;
       			commonnounPresent = true;
       		}
       		if(commonnounPresent == false)
       		{
       			output[index++]=NP2;
       			
       		}
       		
       
			System.out.println("Noun1: "+NP1+"\tConj:"+conjheadTag+"\t Noun2:"+NP2);
						
				
				
			
			
		System.out.println("SIize of output array="+index);
		System.out.println("Output"+output[0]+output[1]+output[2]);
		ruleMatch= true;
		}
		
		
		
		if(ruleMatch==false){
		while (tregexPatternMatchProperNounConjunctionMatcher.find()){
		Tree nounTree = tregexPatternMatchProperNounConjunctionMatcher.getNode("noun1");
   		String properNoun1 = TreeUtil.getLabel(nounTree);
   		output[index++]=properNoun1;

   		Tree conjTree = tregexPatternMatchProperNounConjunctionMatcher.getNode("conj");
   		String conjheadTag = TreeUtil.getLabel(conjTree);
   		output[index++]=conjheadTag;
   		
   		Tree nounTree2 = tregexPatternMatchProperNounConjunctionMatcher.getNode("noun2");
   		String properNoun2 = TreeUtil.getLabel(nounTree2);
   		output[index++]=properNoun2;

   		
   		System.out.println("Noun1: "+properNoun1+"\tConj:"+conjheadTag+"\t Noun2:"+properNoun2);
   		
		System.out.println("SIize of output array="+index);
		System.out.println("Output"+output[0]+output[1]+output[2]);
   		ruleMatch=true;
		}
		}
		
		if(ruleMatch==false){
		tregexPatternMatchNounPhraseMatcher.find();
			
			Tree nounTree = tregexPatternMatchNounPhraseMatcher.getNode("nounphrase");
       		tregexPatternMatchProperNounMatcher = tregexPatternMatchProperNounModifier.matcher(nounTree);
       		while(tregexPatternMatchProperNounMatcher.find())
       		{
       			System.out.println("ProperNoun present");
       			Tree properNoun=tregexPatternMatchProperNounMatcher.getNode("propernounsingular");
       	//		Tree properNounPlural=tregexPatternMatchProperNounMatcher.getNode("propernounplural");
       		//	String propernounplural= TreeUtil.getLabel(properNounPlural);
       			//System.out.println(propernounplural);
       			String propernoun = TreeUtil.getLabel(properNoun);
       			output[index++]=propernoun;
       			propernounPresent=true;
       		}
       		tregexPatternMatchProperNounMatcher = tregexPatternMatchProperNounPluralModifier.matcher(nounTree);
       		while(tregexPatternMatchProperNounMatcher.find())
       		{
       			System.out.println("ProperNoun plural present");
       			Tree properNounPlural=tregexPatternMatchProperNounMatcher.getNode("propernounplural");
       			String propernounplural= TreeUtil.getLabel(properNounPlural);
       			System.out.println(propernounplural);
       			String propernoun = TreeUtil.getLabel(properNounPlural);
       			output[index++]=propernoun;
       			propernounPresent=true;
       		}
			
       		if(propernounPresent==false){
       		
       		System.out.println("Proper noun not present");
       		tregexPatternMatchCommonNounMatcher = tregexPatternMatchCommonNounModifier.matcher(nounTree);
       		while(tregexPatternMatchCommonNounMatcher.find())
       		{
       			Tree commonNoun=tregexPatternMatchCommonNounMatcher.getNode("commonnoun");
       			String commonnoun = TreeUtil.getLabel(commonNoun);
       			System.out.println("CommonNoun:"+commonnoun);
       			output[index++]=commonnoun;
       			commonnounPresent=true;
           	}
       		if(commonnounPresent==false){
       		Tree npHead = headFinder.determineHead(nounTree);
       		String headTag = TreeUtil.getLabel(npHead);
       		output[index++]=headTag;
       		}
       		System.out.println("Size of output array="+index);
       		for(int i=0;i<index;i++)
       		{
       			System.out.print(output[i]+" ");
       		}
       		//System.out.println("Just Noun Phrase:"+headTag);
       		}
       		System.out.println("Size of output array="+index);
       		for(int i=0;i<index;i++)
       		{
       			System.out.print(output[i]+" ");
       		}
		}
		
		return null;
	}
	
	public static String resolveHead(String ansPhrase) throws ParseException{
		int count=0;
		String tregexMatchNounModifier = "NP=noun";
		TregexPattern tregexPatternMatchNounModifier;
		TregexMatcher tregexMatcher;
		tregexPatternMatchNounModifier = TregexPattern.compile(tregexMatchNounModifier);
		CollinsHeadFinder headFinder = new CollinsHeadFinder();
		try {
			StanfordParser.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tree tree = StanfordParser.parseTree(ansPhrase);
		System.out.println(tree);
		tregexMatcher = tregexPatternMatchNounModifier.matcher(tree);
		while (tregexMatcher.find()) {
			
			Tree nounTree = tregexMatcher.getNode("noun");
			Tree npHeadTree = headFinder.determineHead(nounTree);
       		String headTag = TreeUtil.getLabel(npHeadTree);
       		
       	
       				
		   //System.out.println(npHeadTree.toString());
			count=countWords(headTag);
			
			
				
				
				return headTag;
			
			
		
		}
		return null;
	}
	
	public static int countWords(String s){

	    int wordCount = 0;

	    boolean word = false;
	    int endOfLine = s.length() - 1;

	    for (int i = 0; i < s.length(); i++) {
	
	        if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
	            word = true;
	            
	        } else if (!Character.isLetter(s.charAt(i)) && word) {
	            wordCount++;
	            word = false;
	          
	        } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
	            wordCount++;
	        }
	    }
	    return wordCount;
	}
	//after POS tag merges all proper nouns together, it writes the proper noun list as file
	//read the file and populate the nounPhrase Set
	 public static boolean populateNounPhraseSet(){
		 //System.out.println("VISHNU is in: populateNounPhraseSet()");
	    	if(isNounPhraseSetPopulated==false){
	    		System.out.println("populating nounPhrase Set");
	    		BufferedReader in = null;
	    		try {
	    			in = new BufferedReader(new FileReader(new File(Configuration.NOUN_PHRASES_FILE_PATH)));
	    		} catch (FileNotFoundException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		String nounPhraseFileText="";
	    		try {
	    			while (in.ready()) {
	    				nounPhraseFileText+= in.readLine().trim();
	    			}

	    	 		in.close();
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}

	    		System.out.println("Text read from nounPhraseFile :"+nounPhraseFileText);
	    		String[] nounPhraseList=nounPhraseFileText.split("\\|");
	    		System.out.println("Reading nounphrases");  
	    		for(String nounPhraseWithStar:nounPhraseList){
	    			String[] nounWords=nounPhraseWithStar.split("\\*");
	    			boolean firstTime = true;
	    		    String nounPhrase = "";
	    		    for (String word : nounWords) {
	    		        if (firstTime) {
	    		            firstTime = false;
	    		        } else {
	    		            nounPhrase+=" ";
	    		        }
	    		        nounPhrase+=word;
	    		    }
	    		    nounPhraseSet.add(nounPhrase);
	    		    
	    		}
	    		  isNounPhraseSetPopulated=true; 
	    	}
	    	
	    	return true;
	    }
		public static boolean isAnsPhraseProperNoun(String ansPhrase) {
			// TODO Auto-generated method stub
			if(nounPhraseSet.contains(ansPhrase)){
				return true;
			}
			return false;
		}
		protected static String readLine() {
			try {
				return new java.io.BufferedReader(new
					java.io.InputStreamReader(System.in)).readLine();
			}
			catch(java.io.IOException e) {
				return new String("");
			}
		}


	
}
