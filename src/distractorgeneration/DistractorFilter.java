package distractorgeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.ark.PorterStemmer;

public class DistractorFilter {
	public static Set<String> answerPhraseWordSet=new HashSet<String>();
	public static List<String> removeAnswerPhraseWordsFromDistractorList(String answerPhrase,List<String> distractorList){
		List<String> removedList=new ArrayList<String>();
		//converting allwords in answerPhrase to lowercase
		answerPhrase=answerPhrase.toLowerCase();
		answerPhrase = answerPhrase.replaceAll("[!?,]", "");
		String[] strs = answerPhrase.split("\\s+");
		answerPhraseWordSet=new HashSet<String>(Arrays.asList(strs));
		//also remove the stemmed word of the answerPhrase
		String rootWord=PorterStemmer.getInstance().stem(answerPhrase);
		answerPhraseWordSet.add(rootWord.toLowerCase());
		//changing all distractors to lowercase words
		for(int i=0;i<distractorList.size();i++){
			distractorList.set(i,distractorList.get(i).toLowerCase());
		}
		removedList.addAll(distractorList);
		removedList.removeAll(answerPhraseWordSet);
		
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
		list=removeAnswerPhraseWordsFromDistractorList("an excellent farmer and carpenter", list);
		System.out.println("After removing:");
		for(String word:list)
			System.out.println(word);
	}
	
}
