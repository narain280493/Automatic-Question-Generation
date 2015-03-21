package distractorgeneration;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
class DistractorComparator implements Comparator<Distractor>{
	public int compare(Distractor o1, Distractor o2) {
		if(o1.weight > o2.weight){
            return -1;
        } else {
            return 1;
        }
	}
}
public class DistractorGenerator {
 
public static boolean ngramSwitch=true;
   //@input list of words
   //@output probability of occurrence of that words
public static List<List<String>> getNgrams(String[] wordList,String distractor,int index,int N){
	wordList[index]=distractor;
	int start,end;
	if((index-(N-1))>=0){
		start=index-(N-1);
	}
	else{
		start=0;
	}
	if((index+(N-1))<wordList.length){
		end=index+(N-1);
	}
	else{
		end=wordList.length-1;
	}
	List<List<String>> ngramList=new ArrayList<List<String>>();
	for(int i=start;i<=end;i++){
		List<String> ngram=new ArrayList<String>();
		if(i+(N-1)<=end)
		for(int j=i;j<=i+(N-1);j++){
			ngram.add(wordList[j]);
		}
		ngramList.add(ngram);
	}		
		
	
	return ngramList;
	   
}
public static List<Distractor> rankDistractor(String sentence,String ans,List<String> distractors){
	String[] wordList=sentence.split(" ");
	int N;
	int index=0;
	for(String word:wordList){
		if(word.equalsIgnoreCase(ans))
			break;
		index++;
	}
	if(index==wordList.length){
		System.out.println("ansWord: "+ans+" is not found in the sentence: "+sentence);
		return null;
	}
	if(wordList.length>=5)
		N=5;
	else
		N=wordList.length;
	List<Distractor> distractorList=new ArrayList<Distractor>();
	for(String distractor:distractors){
		List<List<String>> ngramList=getNgrams(wordList, distractor, index, N);
		double probabilitySum=0;
		//instead of checking all ngrams check only the first ngram alone to reduce the number of requests for
		//ngram bing api
		List<String> ngram=new ArrayList<String>();
		for(List<String> ngram1:ngramList){
			if(ngram1.size()>=2){
				ngram=ngram1;
				break;
			}
		}
		probabilitySum=getProbability(ngram); 
		if(ngramList.size()>=2){
			for(int i=ngramList.size()-1;i>=0;i--){
				if(ngramList.get(i).size()>=2){
					ngram=ngramList.get(i);
					break;
				}
			}
		probabilitySum+=getProbability(ngram);
		probabilitySum/=new Double(2);
		}
		
		distractorList.add(new Distractor(distractor,probabilitySum));
	}
   Collections.sort(distractorList,new DistractorComparator());
  // System.out.println("Distractor Ranking :");
  // for(Distractor distractor:distractorList){
	//   System.out.println(distractor.distractorWord+" "+distractor.weight);
	    
   //}
   return distractorList;
	
}
public static List<String> getPOSTaggerDistractors (String fileName, String queryString) {
 
    
      URL u;
      InputStream is = null;
      DataInputStream dis;
      String s;
      
      String str="";
      String urlString="";
	try {
		urlString = "http://localhost:8080/hitMe?queryString="+URLEncoder.encode(queryString,"UTF-8")+"&&type=1&&fileName="+URLEncoder.encode(fileName,"UTF-8");
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
      List<String> list = Arrays.asList(str.split(","));
      
      return list;
   }  
   public static List<String> getSSTTaggerDistractors (String fileName, String queryString) {
	   
	      
	      URL u;
	      InputStream is = null;
	      DataInputStream dis;
	      String s;
	      
	      String str="";
	      String urlString="";
		try {
			urlString = "http://localhost:8081/hitMe?queryString="+URLEncoder.encode(queryString,"UTF-8")+"&&fileName="+URLEncoder.encode(fileName,"UTF-8");
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

	      List<String> list = Arrays.asList(str.split(","));
	      return list;
	   }
   public static double getProbability (List<String> list) {
	   
	    
	      URL u;
	      InputStream is = null;
	      DataInputStream dis;
	      String s;
	      
	      if(list==null||list.size()==0){
	    	 System.out.println("Got empty list");
	    	  return 0;
	      }
	      String str="";
	      String urlString = "http://weblm.research.microsoft.com/rest.svc/bing-body/2013-12/3/cp?u=10151e05-1396-417c-bc92-ac6de3cabf96&p=";
	      for(String word : list){
	    	  try {
				urlString+=URLEncoder.encode(word,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	  urlString+="+";
	      }
	      if (urlString.length() > 0 && urlString.charAt(urlString.length()-1)=='+') {
	          urlString = urlString.substring(0, urlString.length()-1);
	        }
	    /*  System.out.println("List of words in ngram");
	      for(String word : list){
	  	    System.out.print(word+"\t");
	      }
	  	  System.out.println();
	      *///System.out.println(urlString);
	      if(ngramSwitch){
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
	      Double logx=Double.valueOf(str);
	      //finding antilog for given log value
	      Double x = Math.pow(10, logx);
	      //System.out.println("antilog "+x);
	      return x;
	      }
	      return 0;
	   }  
	 
  /* public static void main(String[] args) {
	   List<List<String>> list=getNgrams("hello world is the first program that i coded", "program", 5);
	   for(List<String> ngram:list){
		   for(String word:ngram)
			   System.out.print(word+" ");
		   System.out.println();
		   
	   }
   }*/
} // end of class definition