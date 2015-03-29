package Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import Configuration.Configuration;


public class HtmlWriter {

	public static void writeHtml() throws IOException{
		String line;
		String content ="";
		File questionfile = new File(Configuration.QUESTION_DISTRACTOR_LOG_PATH);
		if(questionfile.exists())
		{
			FileReader fr= new FileReader(questionfile);
		   BufferedReader bufferReader = new BufferedReader(fr);
		   while ((line = bufferReader.readLine()) != null)   {
	          //  System.out.println(line);
	            content = content +"<p>"+line+"</p>";
	          }
		   System.out.println(content);
		}
		//<html><head><title>Simple Page</title></head>
		String text = "";
		text = text + "<body bgcolor=\"white\"><hr/><font size=50>Video Question</font><hr/>";
		text = text + "<video width=\"500\" height=\"500\" controls> <source src=\"/home/narain/fyp_resources/Video/ted_cut.mp4\" type=\"video/mp4\">  Your browser does not support the video tag. </video>";
		text =  text +"<p>"+ content+"</p></body></html>";
		
		 try {
	          File file = new File(Configuration.HTML_FILE_PATH);
	          BufferedWriter output = new BufferedWriter(new FileWriter(file));
	          output.write(text);
	          output.close();
	        } catch ( IOException e ) {
	           e.printStackTrace();
	        }
	}
	public static void main(String args[]) throws IOException
	{
		new HtmlWriter().writeHtml();
		new VideoPlayer().play();
	}
}
