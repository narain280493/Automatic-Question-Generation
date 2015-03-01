package Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VideoClipper {
	public static void ClipVideo()
	{
		
		try {
			Process p = Runtime.getRuntime().exec("python /home/narain/workspace/questiongeneration/pythonscripts/Summary/getsummary.py ");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = new String();
			while ((line = in.readLine()) != null)
			 {
				 System.out.println(line);
			 }
			//System.out.println("Status:"+line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
