package edu.cmu.ark.ranking;

import java.util.List;

public class RankingUtils {
	public static double computeL2Norm(double[] x) {
		double res = 0.0;
		for(int i=0;i<x.length;i++)	res += Math.pow(x[i],2);
		return res;
	}

	public static  double dotProd(double [] w, double [] x){
		double res = 0.0;
		for(int i=0;i<w.length;i++) res+=w[i]*x[i];
		return res;
	}
	
	public static  void multiplyByScalar(double[] weights, double scalar) {
		for(int i=0;i<weights.length; i++){
			weights[i] *= scalar;
		}
	}

	public static double[] vectorSubtraction(double[] features1, double[] features2) {
		double [] res = new double[features1.length];
		for(int i=0;i<features1.length;i++){
			res[i] = features1[i]-features2[i];
		}
		return res;
	}

	public static double sign(double d) {
		if(d>0){
			return 1.0;
		}else{
			return -1.0;
		}
	}

	public static double[] vectorAddition(double[] features1, double[] features2) {
		double [] res = new double[features1.length];
		for(int i=0;i<features1.length;i++){
			res[i] = features1[i]+features2[i];
		}
		return res;
	}
	
	public static double[] convertToArray(List<Double> list){
		double[] res = new double[list.size()];
		for(int i=0;i<list.size();i++){
			res[i] = list.get(i);
		}
		return res;
	}
}
