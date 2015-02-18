package distractorgeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class preprocessTextFile {
	public static void preProcess(String inputFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(new File("/home/vishnu/fyp_resources/abelincoln.txt")));
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/vishnu/fyp_resources/check.txt"));
        String paragraph="";
		while (in.ready()) {
			 paragraph +=(in.readLine().trim()+" ");
			 
		}
		String[] sentenceList = paragraph.split("(?<=[.?!])\\s?(?=[a-zA-Z])");
//		paragraph="";
		for(String str:sentenceList){
			System.out.println("Sentence :"+str);
		}
		out.close();
		System.out.println("Done");
	}
	public static void main(String[] args) throws IOException {
		preprocessTextFile.preProcess("sds");
	}
}
		
		
		
		
