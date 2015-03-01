package Utility;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class HtmlWriter {

	public void writeHtml(){
		String text = "<html><head><title>Simple Page</title></head>";
		text = text + "<body bgcolor=\"white\"><hr/><font size=50>Video Question</font><hr/>";
		text = text + "<video width=\"500\" height=\"500\" controls> <source src=\"/home/narain/fyp_resources/Video/ted_cut.mp4\" type=\"video/mp4\">  Your browser does not support the video tag. </video>";
		text = text + "</body></html>";
		
		 try {
	          File file = new File("/home/narain/workspace/video.html");
	          BufferedWriter output = new BufferedWriter(new FileWriter(file));
	          output.write(text);
	          output.close();
	        } catch ( IOException e ) {
	           e.printStackTrace();
	        }
	}
}
