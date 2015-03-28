package Utility;

import java.io.BufferedReader;
import Configuration.Configuration;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.ark.QuestionAsker;
import edu.stanford.nlp.trees.tregex.ParseException;
public class VideoClipper {
	public static ArrayList<String> ClipVideo()
	{
		ArrayList<String> paragraph =new ArrayList<String>();
		
		try {
			Process p = Runtime.getRuntime().exec("python"+" "+Configuration.SUMMARY_PYTHON_SCRIPT_PATH);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String summaryPara = new String();
			String start_time =new String();
			String end_time =new String();
			String clippingStatus=new String();
			start_time=in.readLine();
			end_time=in.readLine();
			 while((summaryPara = in.readLine())!=null){
				 	paragraph.add(summaryPara);
				 //	System.out.println(summaryPara);
			 }
			 System.out.println("Start time:"+start_time);
			 System.out.println("End time:"+end_time);
			 
			/* p=Runtime.getRuntime().exec("python"+" "+Configuration.VIDEO_CROPPER_SCRIPT_PATH+" "+start_time+" "+end_time);
			BufferedReader inn=new BufferedReader(new InputStreamReader(p.getInputStream()));
			 while((clippingStatus = inn.readLine())!=null){
				 	System.out.println(clippingStatus);
				 	
			 }*/
			 return paragraph;
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String [] args) throws ParseException
	{
	//	QuestionAsker qa =new QuestionAsker();
		//String [] args1 = {"--debug", "--model models/linear-regression-ranker-reg500.ser.gz"};
		//String summary ="The other thing that's really important about sleep is that it doesn't arise from a single structure within the brain, but is to some extent a network property, and if we flip the brain on its back -- I love this little bit of spinal cord here -- this bit here is the hypothalamus, and right under there is a whole raft of interesting structures, not least the biological clock.";
	//	qa.main(args1);
		new VideoClipper().ClipVideo();
	//}
	}
}
