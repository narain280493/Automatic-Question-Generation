package distractorgeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Configuration.Configuration;
import Utility.SuperSenseTagHelper;
import Utility.WordNetPythonAPI;
import edu.cmu.ark.PorterStemmer;

public class DistractorFilter {
	public static Set<String> filterWords=new HashSet<String>();
	public static List<String> applyFiltersToDistractorList(String resolvedAnswerPhrase,String originalAnswerPhrase,List<String> distractorList){
		List<String> removedList=new ArrayList<String>();
		
		//Filter 1:
		//converting all words in answerPhrase to lowercase
		originalAnswerPhrase=originalAnswerPhrase.toLowerCase();
		originalAnswerPhrase = originalAnswerPhrase.replaceAll("[!?,]", "");
		String[] strs = originalAnswerPhrase.split("\\s+");
		filterWords=new HashSet<String>(Arrays.asList(strs));
		
		//Filter 2:
		//also remove the stemmed word of the resolvedAnswerPhrase
		String rootWord=PorterStemmer.getInstance().stem(resolvedAnswerPhrase);
		filterWords.add(rootWord.toLowerCase());
		
		//Filter 3:
		//remove distractors that are synonymous to answephrase
		//we do 2 kinds of removal here
		// Given string A(answerPhrase) and B(distractor)
		// B cannot be distractor and thus should be added to filterWords list
		// a) if A's synonym set contain B
		// b) intersection of A's synonym set and B's synonym set is not null
		String sstOfResolvedAnswerPhrase = SuperSenseTagHelper.getSSTForGivenWord(Configuration.INPUT_FILE_PATH+Configuration.INPUT_FILE_NAME,resolvedAnswerPhrase);
		Set<String> synonymsOfResolvedAnswerPhrase=new HashSet<String>(WordNetPythonAPI.getResponse("synonym", resolvedAnswerPhrase,sstOfResolvedAnswerPhrase));
		filterWords.addAll(synonymsOfResolvedAnswerPhrase);
		//the following for loop is for filter 3 subtask b
		for(String distractor:distractorList){
			String sstOfDistractor=SuperSenseTagHelper.getSSTForGivenWord(Configuration.INPUT_FILE_PATH+Configuration.INPUT_FILE_NAME,distractor);
			List<String> distractorSynonyms = WordNetPythonAPI.getResponse("synonym",distractor,sstOfDistractor);
			for(String str:distractorSynonyms){
				if(synonymsOfResolvedAnswerPhrase.contains(str)){
					filterWords.add(distractor);
					break;
				}
			}
		}
		//changing all distractors to lowercase words
		for(int i=0;i<distractorList.size();i++){
			distractorList.set(i,distractorList.get(i).toLowerCase());
		}
		removedList.addAll(distractorList);
		removedList.removeAll(filterWords);
		
		return removedList;
	}
	
	
	public static List<String> removeSSTDistractorsFromPOSDistractorList(List<String> posDistractorList,List<String> sstDistractorList){
		for(String word:sstDistractorList){
			posDistractorList.remove(word);
		}
		return posDistractorList;
		
	}
	public static void main(String[] args) {
		List<String> list=new ArrayList<String>();
		list.add("member");
		list.add("Carpenter");
		list=applyFiltersToDistractorList("farmer","an excellent farmer and carpenter", list);
		System.out.println("After removing:");
		for(String word:list)
			System.out.println(word);
	}
	
}
