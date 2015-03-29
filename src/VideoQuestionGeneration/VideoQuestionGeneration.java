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


