package Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.cmu.ark.QuestionAsker;
import edu.stanford.nlp.trees.tregex.ParseException;
public class VideoClipper {
	public static void ClipVideo()
	{
		
		try {
			Process p = Runtime.getRuntime().exec("python /home/narain/workspace/questiongeneration/pythonscripts/Summary/getsummary.py ");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String summary = new String();
		
			 while((summary = in.readLine())!=null){
				 	System.out.println(summary);
			 }
	
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//public static void main(String [] args) throws ParseException
//	{
	//	QuestionAsker qa =new QuestionAsker();
		//String [] args1 = {"--debug", "--model models/linear-regression-ranker-reg500.ser.gz"};
		//String summary ="The other thing that's really important about sleep is that it doesn't arise from a single structure within the brain, but is to some extent a network property, and if we flip the brain on its back -- I love this little bit of spinal cord here -- this bit here is the hypothalamus, and right under there is a whole raft of interesting structures, not least the biological clock.";
	//	qa.main(args1);
		//new VideoClipper();
	//}
}
