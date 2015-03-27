package TopicExtraction;

import java.util.List;
import java.util.Scanner;

public class WikipediaMinerAPIDriver {
	//start the server first using command: phantomjs test.js 8000

	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		String string=sc.nextLine();
		System.out.println("Detected topics and their probability");
		List<Topic> list=WikipediaMinerAPI.getTopics(string);
		System.out.println("Number of topics found :"+list.size());
		for(Topic topic:list){
			System.out.println(topic.topicName+" "+topic.probability);
			
		}
		sc.close();
	}
}
