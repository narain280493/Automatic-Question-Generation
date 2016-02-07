package Utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Configuration.Configuration;

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
		JSONArray jsonArray=new JSONArray();
		for(Element timeDataTag:timeDataTags){
			//System.out.println(timeDataTag.text()+" "+timeDataTag.nextElementSibling().text());
			String text=timeDataTag.nextElementSibling().text();
			JSONObject obj=new JSONObject();
			obj.put(timeDataTag.text(), text);
			jsonArray.add(obj);
			transcript+=(text+" ");
			
		}
		System.out.println("JSON :"+jsonArray.toJSONString());
		try {
			 
			File file = new File(Configuration.TRANSCRIPT_JSON_FILE_PATH);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(jsonArray.toJSONString());
			bw.close();
			return "Write Success";
			//System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
			
		}

		//System.out.println(transcript.length());
		return "Write Failure";
		
	}
	

	public static void main(String[] args) {
		ArrayList<String> summaryPara =new ArrayList<String>();
	//	getTranscript("http://www.ted.com/talks/annette_heuser_the_3_agencies_with_the_power_to_make_or_break_economies/transcript?language=en");
		getTranscript("http://www.ted.com/talks/jason_pontin_can_technology_solve_our_big_problems/transcript?language=en");
	//	getTranscript("http://www.ted.com/talks/ron_gutman_the_hidden_power_of_smiling/transcript?language=en");
	//	VideoClipper vc=new VideoClipper();
	//	vc.ClipVideo();
		
	//	VideoPlayer vp =new VideoPlayer();
		//vp.play();
	}
	
}
