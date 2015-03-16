package TopicExtraction;


import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Example program to list links from a URL.
 */
public class ParseHtml {
    public static Set<Topic> parse(String url) {
    	Document doc = null;
    	Set<Topic> list=new HashSet<Topic>();
		try {
			doc = Jsoup.connect(url).timeout(10*1000).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(doc.toString());
        Element topicDiv = doc.select("div#tab_topics").first();
        Element topicList = topicDiv.select("ul#topics").first();
        Elements topics = topicList.select("li");
        for(Element topic:topics){
        	System.out.println(topic.text()+" "+topic.select("a").attr("linkprob"));
        	list.add(new Topic(topic.text(),Double.valueOf(topic.select("a").attr("linkprob"))));
        }
        return list;
    }
    public static void main(String[] args) {
		parse("http://localhost:8000/?query=hello+world+vishnu");
	}
}