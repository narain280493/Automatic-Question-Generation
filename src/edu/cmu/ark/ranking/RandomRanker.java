package edu.cmu.ark.ranking;

import java.util.*;

public class RandomRanker extends BaseRanker implements IRanker{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7454712011283682907L;

	@Override
	public void rank(List<Rankable> unranked, boolean doSort) {
		Random rnd = new Random();
		for(int i=0;i<unranked.size();i++){
			unranked.get(i).score = rnd.nextDouble();
		}
		if(doSort){ 
			Collections.sort(unranked);
			Collections.reverse(unranked);
		}
	}
	

	@Override
	public void train(List<List<Rankable>> trainData) {
		return;
	}





}
