package Utility;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Example program to list links from a URL.
 */

public class GoogleWordFrequencyCrawler {
	
	public static Float getFreq(String word)
	{
		String url = "http://app.aspell.net/lookup?dict=en_US&words="+word;
             Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Elements tables = doc.select("table");
        Element table=tables.first();
        //  print("table:"+table.toString());
                
        //Element freq=table.first();
        Element last=table.child(0).child(0).lastElementSibling();
        Element secondlast=last.child(0).lastElementSibling().previousElementSibling();
        String s;
        s=(secondlast.ownText().toString());
        //s.trim();
        //s.replaceAll("\\S","");
        
        s=s.substring(0,s.length()-1);
        s=s.replaceAll(",", "");
		
//        System.out.print(s.length());
        if(s.isEmpty())
        return Float.valueOf(0);
        else
        return Float.valueOf(s);
	}
    

}
