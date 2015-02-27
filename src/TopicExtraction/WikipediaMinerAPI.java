package TopicExtraction;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class TopicComparator implements Comparator<Topic>{
	public int compare(Topic o1, Topic o2) {
		if(o1.probability > o2.probability){
            return -1;
        } else {
            return 1;
        }
	}

	
}
public class WikipediaMinerAPI {
 
   
   //@input list of words
   //@output probability of occurrence of that words
public static List<Topic> getTopics (String text) {
	   
      String urlString="";
      Set<Topic> topicList=new HashSet<Topic>();
      List<Topic> list=new ArrayList<Topic>();
      try {
				urlString = "http://localhost:8000/?query="+URLEncoder.encode(text,"UTF-8");
				topicList=ParseHtml.parse(urlString);
    		} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     list.addAll(topicList);
      	Collections.sort(list,new TopicComparator());
      
          return list;
   }  
 
} // end of class definition