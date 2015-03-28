package TopicExtraction;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 
	public static List<String> splitEqually(String text, int size) {
	    // Give the list the right capacity to start with. You could use an array
	    // instead if you wanted.
	    List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

	    for (int start = 0; start < text.length(); start += size) {
	        ret.add(text.substring(start, Math.min(text.length(), start + size)));
	    }
	    return ret;
	    
	}	  
   //@input list of words
   //@output probability of occurrence of that words
	public static List<Topic> getTopics (String text) {
	   
      String urlString="";
      List<Topic> topicList=new ArrayList<Topic>();
      Set<Topic> responseList=new HashSet<Topic>();
      Map<String,Double> topicMap=new HashMap<String,Double>();
      List<String> textList=splitEqually(text,1500);
      for(String textChunk:textList){
    	  try {
				urlString = "http://localhost:8000/?query="+URLEncoder.encode(textChunk,"UTF-8");
				responseList=ParseHtml.parse(urlString);
    	  } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    	  }
    	 for(Topic topic:responseList){ 
    		 if(topicMap.containsKey(topic.topicName)){
    			 Double existingProbability=topicMap.get(topic.topicName);
    			 if(topic.probability>existingProbability){
    				topicMap.put(topic.topicName, topic.probability);
    			 }
    		 }
    		 else{
    			 topicMap.put(topic.topicName, topic.probability);
    		 }
    	}
    		 
      }
      //convert topic map to topic list
      for (Map.Entry<String, Double> entry : topicMap.entrySet())
      {
    	  topicList.add(new Topic(entry.getKey(), entry.getValue()));
      }
      Collections.sort(topicList,new TopicComparator());
      return topicList;
   }
	
 
} // end of class definition