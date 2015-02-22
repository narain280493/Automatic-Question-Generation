package distractorgeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tester {
	public static void main(String[] args) {
		List<Distractor> distractorList=new ArrayList<Distractor>();
		distractorList.add(new Distractor("apple",3.0));
		distractorList.add(new Distractor("hello",1.23));
		distractorList.add(new Distractor("world",2.23));
		distractorList.add(new Distractor("apple",1.0));
		distractorList.add(new Distractor("apple",1.22));
		
		Collections.sort(distractorList,new DistractorComparator());
		 System.out.println("Distractor Ranking :");
		   for(Distractor distractor:distractorList){
			   System.out.println(distractor.distractorWord+" "+distractor.weight);
			    
		   }
		  
	}
	
}
