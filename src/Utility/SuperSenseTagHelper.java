package Utility;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SuperSenseTagHelper {
	public static String getSSTForGivenWord (String fileName, String queryString) {
		 
	    
	      URL u;
	      InputStream is = null;
	      DataInputStream dis;
	      String s;
	      
	      String str="";
	      String urlString="";
		try {
			urlString = "http://localhost:8081/hitMe?queryString="+URLEncoder.encode(queryString,"UTF-8")+"&&type=2&&fileName="+URLEncoder.encode(fileName,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	      
	      try {
	 
	             u = new URL(urlString);
	               is = u.openStream();         // throws an IOException
	 
	       
	         dis = new DataInputStream(new BufferedInputStream(is));
	               while ((s = dis.readLine()) != null) {
	                    str+=s;
	         }
	               
	 
	      } catch (MalformedURLException mue) {
	 
	         System.out.println("Ouch - a MalformedURLException happened.");
	         mue.printStackTrace();
	         System.exit(1);
	 
	      } catch (IOException ioe) {
	 
	         System.out.println("Oops- an IOException happened.");
	         ioe.printStackTrace();
	         System.exit(1);
	 
	      } finally {
	 
	       
	         try {
	            is.close();
	         } catch (IOException ioe) {
	            // just going to ignore this one
	         }
	 
	      } // end of 'finally' clause
	      
	      return str;
	   }
	
	
}
