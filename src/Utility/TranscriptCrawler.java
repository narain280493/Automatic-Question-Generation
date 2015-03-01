package Utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TranscriptCrawler {
	public static Map<String,String> TRANSCRIPT_MAP=new HashMap<String,String>();

	public static String getTranscript(String url){
		Document doc=null;
		String transcript="";
		try {
			doc = Jsoup.connect(url).timeout(10*1000).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements timeDataTags=doc.select("data[class=talk-transcript__para__time]");
		//JSONArray jsonArray=new JSONArray();
		JSONObject obj=new JSONObject();
		for(Element timeDataTag:timeDataTags){
			//System.out.println(timeDataTag.text()+" "+timeDataTag.nextElementSibling().text());
			String text=timeDataTag.nextElementSibling().text();
			
			obj.put(timeDataTag.text(), text);
			transcript+=(text+" ");
			
		}
		System.out.println("JSON :"+obj.toJSONString());
		try {
			 
			File file = new File("/home/vishnu/workspace/QuestionGeneration/transcript.json");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(obj.toJSONString());
			bw.close();
 
			//System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}

		//System.out.println(transcript.length());
		return transcript;
		
	}
	
	public static void main(String[] args) {
		getTranscript("http://www.ted.com/talks/russell_foster_why_do_we_sleep/transcript?language=en");
	}
	

}
