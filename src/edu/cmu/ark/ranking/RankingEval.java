package edu.cmu.ark.ranking;

import java.util.*;


public class RankingEval {

	public static double computeKendallsTau(double[] xArray, double[] yArray){
		double numDiscordant = 0;
		double numConcordant = 0;
		double tau = 0.0;
		for(int i=0;i<xArray.length;i++){
			for(int j=i+1;j<xArray.length;j++){				
				numConcordant += Math.signum(xArray[i]-xArray[j])*Math.signum(yArray[i]-yArray[j]);
			}
		}
		
		tau = 2 * (numConcordant - numDiscordant)/(xArray.length * (xArray.length-1));
		
		if(Double.isNaN(tau)){
			tau = 0.0;
		}
		return tau;
	}
	
	public static double computeKendallsTau(List<List<Rankable>> ranked){
		double avg = 0;
		
		for(List<Rankable> list: ranked){
			double [] xArray = new double [list.size()];
			double [] yArray = new double [list.size()];
			for(int i=0;i<list.size();i++){
				xArray[i]=list.get(i).score;
				yArray[i]=list.get(i).label;
			}
			avg += computeKendallsTau(xArray, yArray)/ranked.size();
		}
		
		return avg;
	}
	
	
	
	public static double computeMeanNDCG(List<List<Rankable>> ranked, int k, boolean binaryValues){
		double ndcg, avg = 0.0, numLists = 0.0;
		
		for(int i=0;i<ranked.size();i++){
			ndcg = computeNDCG(ranked.get(i), k, binaryValues);	
			if(ndcg < 0) continue;
			avg += ndcg;
			numLists++;
		}
		avg /= numLists;
		return avg;
	}
	
	public static double computeNDCG(List<Rankable> curList, int k, boolean binaryValues){
		double ndcg = 0.0;
		Rankable point;
		
		ndcg = 0.0;
		double gain;
		List<Double> labels = new ArrayList<Double>();
		for(int j=0;j<curList.size() && j<k; j++){
			point = curList.get(j);
			if(binaryValues){
				if(point.label>0) gain = 1.0;
				else gain = 0.0;
			}else{
				gain = point.label;
			}
			ndcg += gain * ndcgDiscount(j);
		}
		
		double max = 0.0;
		for(int j=0;j<curList.size(); j++) labels.add(curList.get(j).label);
		Collections.sort(labels);
		Collections.reverse(labels);
		for(int j=0;j<labels.size() && j<k; j++){
			if(binaryValues){
				if(labels.get(j)>0) gain = 1.0;
				else gain = 0.0;
			}else{
				gain = labels.get(j);
			}
			max += gain * ndcgDiscount(j);
		}
		
		if(max == 0.0){
			return -1.0;
		}
		
		ndcg /= max;
		if(Double.isNaN(ndcg)) ndcg = 0.0;
		return ndcg;
	}
	
	
	private static double ndcgDiscount(int i){
		double res = 0.0;
		if(i==0){
			res = 1.0;
		}else{
			res = 1.0/(Math.log(1.0+i)/Math.log(2.0));
		}
		return res;
	}
	
	public static double precisionAtN(List<List<Rankable>> ranked, int k){
		return  precisionAtN(ranked, k, 0.5);
	}
	
	public static double precisionAtN(List<List<Rankable>> ranked, int k, double threshold){
		double avg = 0.0;
		
		for(int i=0;i<ranked.size();i++){
			List<Rankable> tmpList = ranked.get(i);
			double prec = 0.0;
			for(int j=0;j<tmpList.size() && j<k; j++){
				if(tmpList.get(j).label > threshold) prec+=1.0/k;
			}
			
			avg += prec/ranked.size();
		}
		
		return avg;
	}

	public static double computeMAP(List<List<Rankable>> lists) {
		return computeMAP(lists, 0.5);
	}

	public static double computeMAP(List<List<Rankable>> lists, double threshold) {
		double res = 0.0;
		double numLists = 0.0;
		
		for(List<Rankable> list: lists){
			double ap = computeAveragePrecision(list, threshold);
			if(ap < 0) continue;
			res += ap;
			numLists++;
		}
		
		res /= numLists;
		if(Double.isNaN(res)) res = 0.0;
		return res;
	}
	
	
	public static double computeAveragePrecision(List<Rankable> list) {
		return computeAveragePrecision(list, 0.5);
	}
	
	public static double computeAveragePrecision(List<Rankable> list, double threshold) {
		int numPositive = 0;
		double res = 0.0;
		for(Rankable r: list){
			if(r.label>threshold) numPositive++;
		}
		if(numPositive == 0) return -1.0;
		
		double curNumPositive = 0;
		for(int i=0; i<list.size(); i++){
			if(list.get(i).label > threshold){
				curNumPositive++;
				res += curNumPositive/(i+1);
			}
		}
		
		res/=numPositive;
		
		return res;
	}
	
	public static void main(String[] args) {
		double[] x = {0,1,2,3,4,5,6,7,8,9,10};
		//double[] y = {0,0,0,1,0,1,1,2,2,2,2};
		//double[] y = {0,1,2,3,4,5,6,7,8,9,10};
		//double[] y = {10,9,8,7,6,5,4,3,2,1,0};
		//double[] y = {0,2,0,1,0,1,1,2,2,2,2};
		double[] y = {0,0,0,0,0,0,0,0,0,0,1};
		
		double tau = RankingEval.computeKendallsTau(x, y);
		System.out.println("tau="+tau);
	}
	
}
