package Utility;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class VideoPlayer {
	public static void play() {
		try {
	         
			HtmlWriter hw= new HtmlWriter();
			hw.writeHtml();
		
			String htmlFilePath = "/home/vishnu/workspace/video.html";
			File htmlFile = new File(htmlFilePath);
	        Desktop.getDesktop().browse(htmlFile.toURI());
	        
	       }
	       catch (java.io.IOException e) {
	           System.out.println(e.getMessage());
	       }
		
	}
		public static void main(String [] args)
		{
			new VideoPlayer().play();
		}
		
	
}
