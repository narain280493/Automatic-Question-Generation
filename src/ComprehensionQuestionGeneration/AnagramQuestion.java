package ComprehensionQuestionGeneration;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.util.*;
import java.*;
/**
 *
 * @author aditya
 */
public class AnagramQuestion 
{
	public static void main (String args[]) 
        {		
		Scanner in=new Scanner(System.in);
		//Read a string from the user
		System.out.print("Input:"); 
		String s = in.nextLine();
		//We convert the string to an array of characters. Basically
		//we want to freely change the letters in the string, and this
		//is not possible with class String, and is too cumbersome
		//with class StringBuffer.
		char[] text = new char[s.length()]; 
		for (int i=0; i<s.length(); i++) text[i] = s.charAt(i);			
		System.out.println("Anagram of " + s);
		anagramMaker(text, s.length());
		
	}
        static void anagramMaker(char[] a,int len)
        {
            Random rand=new Random();
            int j=0;
            int[] num=new int[len];
            boolean[] b=new boolean[len]; 
            char[] anagram = new char[len];
            for (int i=0; i<len; i++)
            {
                num[i] =0;
                b[i]=false;
                if(Character.isUpperCase(a[i]))
                {
                    a[i]=Character.toLowerCase(a[i]);
                    //System.out.print(a);
                }
            }
            //Random Sequnce
            while(j!=len)
            {
                int randomInt = rand.nextInt(len);
                if(b[randomInt]==false)
                {
                    b[randomInt]=true;
                    num[j]=randomInt;
                    j++;
                }
            }
            //Creating anagram according to random sequence
            for (int i=0; i<len; i++) 
                anagram[i]=a[num[i]];
            //System.out.println("");
            printArray(anagram);
                
        }
        
        static void printArray(char [] a) {
		for (int i=0; i< a.length; i++) System.out.print(a[i]); 
		System.out.println();
	} 
}