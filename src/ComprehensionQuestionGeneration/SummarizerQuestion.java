package ComprehensionQuestionGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;


public class SummarizerQuestion {

	public static void generateSummarizerQuestion() {
		 
		try{
			 
			Process p = Runtime.getRuntime().exec("python foo.py");
			try{
			FileReader inputFile = new FileReader("fee.txt");
			BufferedReader bufferReader = new BufferedReader(inputFile);
			 String line;
			  while ((line = bufferReader.readLine()) != null)   {
		            System.out.println(line);
		          }
			  bufferReader.close();
			}
			
			  catch(Exception e){
		          System.out.println("Error while reading file line by line:" + e.getMessage());                      
		       }
			//BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//String ret = new String(in.readLine());
			//String retur = new String(in.readLine());
			//System.out.println("1) value is : "+ret);
			//System.out.println("2) value is : "+retur);
			
			}catch(Exception e){ System.out.println("Exception:"+e);}
	}

}
