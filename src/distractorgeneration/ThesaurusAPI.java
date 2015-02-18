package distractorgeneration;

import java.io.BufferedReader; 
import java.net.HttpURLConnection; 
import java.net.URL; 
import java.net.URLEncoder; 
import org.json.simple.*; // json package, download at http://code.google.com/p/json-simple/ 

public class ThesaurusAPI { 
  public static void main(String[] args) { 
// NOTE: replace test_only with your own key 
    new SendRequest("abrupt", "en_US", "Y66A4JYm3Pp1pSZqt9DD", "json"); 
  } 
} // end of Thesaurus 

class SendRequest { 
  final String endpoint = "http://thesaurus.altervista.org/thesaurus/v1"; 

  public SendRequest(String word, String language, String key, String output) { 
    try { 
      URL serverAddress = new URL(endpoint + "?word="+URLEncoder.encode(word, "UTF-8")+"&language="+language+"&key="+key+"&output="+output); 
      HttpURLConnection connection = (HttpURLConnection)serverAddress.openConnection(); 
      connection.connect(); 
      int rc = connection.getResponseCode(); 
      if (rc == 200) { 
        String line = null; 
        BufferedReader br = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream())); 
        StringBuilder sb = new StringBuilder(); 
        while ((line = br.readLine()) != null) 
          sb.append(line + '\n'); 
        JSONObject obj = (JSONObject) JSONValue.parse(sb.toString()); 
        JSONArray array = (JSONArray)obj.get("response"); 
        for (int i=0; i < array.size(); i++) { 
          JSONObject list = (JSONObject) ((JSONObject)array.get(i)).get("list"); 
          System.out.println(list.get("category")+":"+list.get("synonyms"));
          String[] synonymsList = list.get("synonyms").toString().split("\\|");
          System.out.println("SynonymsList :");
          for(String str:synonymsList){
        	  System.out.println(str);
          }
        } 
      } else System.out.println("HTTP error:"+rc); 
      connection.disconnect(); 
    } catch (java.net.MalformedURLException e) { 
      e.printStackTrace(); 
    } catch (java.net.ProtocolException e) { 
      e.printStackTrace(); 
    } catch (java.io.IOException e) { 
      e.printStackTrace(); 
    } 
  } 
} // end of SendRequest
