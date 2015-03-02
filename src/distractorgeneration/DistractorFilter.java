package distractorgeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.ark.PorterStemmer;

public class DistractorFilter {
	public static Set<String> answerPhraseWordSet=new HashSet<String>();
	public static List<String> removeAnswerPhraseWordsFromDistractorList(String answerPhrase,List<String> distractorList){
		List<String> removedList=new ArrayList<String>();
		answerPhrase = answerPhrase.replaceAll("[!?,]", "");
		String[] strs = answerPhrase.split("\\s+");
		answerPhraseWordSet=new HashSet<String>(Arrays.asList(strs));
		//also remove the stemmed word of the answerPhrase
		String rootWord=PorterStemmer.getInstance().stem(answerPhrase);
		answerPhraseWordSet.remove(rootWord);
		
		for(String word:distractorList){
			if(answerPhraseWordSet.contains(word)){
			//	System.out.println("REMOVING word: "+word);
				continue;
			}
			else
				removedList.add(word);
		}
		
		return removedList;
	}
	
	public static void main(String[] args) {
		List<String> list=new ArrayList<String>();
		list.add("member");
		list.add("carpenter");
		list=removeAnswerPhraseWordsFromDistractorList("an excellent farmer and carpenter", list);
		System.out.println("After removing:");
		for(String word:list)
			System.out.println(word);
	}
	
}
