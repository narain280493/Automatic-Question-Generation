package Utility;

import java.util.*;

class VocabComparator implements Comparator<word>{
	public int compare(word o1, word o2) {
		if(o1.freq < o2.freq){
            return -1;
        } else {
            return 1;
        }
	}
}
class word
{
	public String w;
	public Float freq;
	word(String a,Float f)
	{
		w=a;
		freq=f;
	}
}

public class VocabularyRanker {
	
	public static List<String> getRankedlist(List<String> words)
	{
	
		List<word> rankedWords=new ArrayList<word>();
		List<String> rankedVocabularies=new ArrayList<String>();
		for(String w:words)
		{
			word e=new word(w,GoogleWordFrequencyCrawler.getFreq(w));
			rankedWords.add(e);
		}
		
		Collections.sort(rankedWords,new VocabComparator());
		
		for(word r:rankedWords)
		{
			rankedVocabularies.add(r.w);
			//System.out.print(r.w+"LLLL\n");
		}
		return rankedVocabularies;
	}
	

	public static void main(String[] args)
	{
		//crawler freq = null;
		
		//System.out.println("This:"+freq.getFreq("cool"));
		List <String> test=new ArrayList<String>();
		test.add("Awesome");
		test.add("cool");
		test.add("Frequency");
		List <String> ranked=null;
		ranked=getRankedlist(test);
		
		
		for(String w:ranked)
		{
			System.out.print(w+"\n");
		}
	}

}