package VideoQuestionGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import edu.cmu.ark.QuestionAsker;
import edu.stanford.nlp.trees.tregex.ParseException;
import Utility.TranscriptCrawler;
import Utility.VideoClipper;
import Utility.VideoPlayer;
import Utility.HtmlWriter;
public class VideoQuestionGeneration {
	/*public static String getSummary(String url){
		String text=TranscriptCrawler.getTranscript(url);
		String summary="";
		//System.out.println(text);
		try {
			 
			File file = new File("/home/vishnu/workspace/QuestionGeneration/transcript.txt");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(text);
			bw.close();
 
			//System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		Process p=null;
		try {
		 p=Runtime.getRuntime().exec("python /home/vishnu/workspace/Summarizer/getsummary.py  "+"/home/vishnu/workspace/QuestionGeneration/transcript.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		try {
			while ((line = in.readLine()) != null){
				System.out.println(line);
				summary+=line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		pla	e.printStackTrace();
		}
		return summary;
		
	}*/
	
	public static void createVideoQuestion(String url) throws IOException{
		
	
		  String[] arguments = new String[] {"--debug",
				  "--model",
				  "models/linear-regression-ranker-reg500.ser.gz","--flag"};
		  try {
			QuestionAsker.main(arguments);
			System.out.println("Main exited");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HtmlWriter.writeHtml();
		VideoPlayer.play();
	}
	public static void main(String[] args) throws IOException {
		//getSummary("http://www.ted.com/talks/russell_foster_why_do_we_sleep/transcript?language=en");
		//createVideoQuestion("http://www.ted.com/talks/russell_foster_why_do_we_sleep/transcript?language=en");
	      createVideoQuestion("http://www.ted.com/talks/jaap_de_roode_how_butterflies_self_medicate/transcript?language=en");
	}
	
}


