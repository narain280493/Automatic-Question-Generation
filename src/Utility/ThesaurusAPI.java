package Utility;

import java.io.BufferedReader; 
import java.net.HttpURLConnection; 
import java.net.URL; 
import java.net.URLEncoder; 
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.*; // json package, download at http://code.google.com/p/json-simple/ 

public class ThesaurusAPI {
  public static List<String> similarTerms = new ArrayList<String>();
  public static List<String> relatedTerms = new ArrayList<String>();
  public static List<String> antonyms = new ArrayList<String>();
  
  public static void main(String[] args) { 
	 List<String> result= getSynonyms("bunk");
	 System.out.println("RESULT:");
	 for(String word:result){
		 System.out.println(word);
	 }
  }
  public static List<String> processResponseString(String response,String query){
	  JSONObject obj = (JSONObject) JSONValue.parse(response); 
      JSONArray array = (JSONArray)obj.get("response"); 
      for (int i=0; i < array.size(); i++) { 
        JSONObject list = (JSONObject) ((JSONObject)array.get(i)).get("list"); 
      //  System.out.println(list.get("category")+":"+list.get("synonyms"));
        String[] synonymsList = list.get("synonyms").toString().split("\\|");
        //System.out.println("SynonymsList :");
        for(String str:synonymsList){
      	  //System.out.println(str);
      	  Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(str);
      	  String word=str.replaceAll("\\(.*?\\) ?", "");
      	  while(m.find()) {
      		  String type=m.group(1);
      		  if(type.contains("related term")){
      			  relatedTerms.add(word);
      		  }
      		  else if(type.contains("antonym")){
      			  antonyms.add(word);
      		  }
      		  else{
      			  similarTerms.add(word);
      		  }
      	  }
      		  
      	  
        }
      }
      if(query.equals("synonym")){
    	  if(similarTerms.size()>=1)
    		  return similarTerms;
    	  else
    		  return relatedTerms;
      }
      else
    	  	return antonyms;
  }
  public static String getResponse(String word){
	  similarTerms.clear();
	  relatedTerms.clear();
	  antonyms.clear();
	  final String endpoint = "http://thesaurus.altervista.org/thesaurus/v1"; 

	  try { 
      URL serverAddress = new URL(endpoint + "?word="+URLEncoder.encode(word, "UTF-8")+"&language="+"en_US"+"&key="+"Y66A4JYm3Pp1pSZqt9DD"+"&output="+"json"); 
      HttpURLConnection connection = (HttpURLConnection)serverAddress.openConnection(); 
      connection.connect(); 
      int rc = connection.getResponseCode(); 
      if (rc == 200) { 
        String line = null; 
        BufferedReader br = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream())); 
        StringBuilder sb = new StringBuilder(); 
        while ((line = br.readLine()) != null) 
          sb.append(line + '\n'); 
        return sb.toString();
      } else System.out.println("HTTP error:"+rc); 
      connection.disconnect(); 
    } catch (java.net.MalformedURLException e) { 
      e.printStackTrace(); 
    } catch (java.net.ProtocolException e) { 
      e.printStackTrace(); 
    } catch (java.io.IOException e) { 
      e.printStackTrace(); 
    } 
	  return null;
  }
  public static List<String> getSynonyms(String word) { 
	      String response = getResponse(word);
	      List<String> result=new ArrayList<String>();
	      if(response!=null){
	    	   result=processResponseString(response, "synonym");
	    	   return result;
	      }
	      return null;
  }
  public static List<String> getAntonyms(String word) { 
      String response = getResponse(word);
      List<String> result=new ArrayList<String>();
      if(response!=null){
    	   result=processResponseString(response, "antonym");
    	   return result;
      }
      return null;
  }
  
} // end of SendRequest
