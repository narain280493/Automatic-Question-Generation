package edu.cmu.ark.ranking;

import java.util.List;

public interface RankScorer {

	double computeRankingScore(List<Rankable> x);
	double computeItemScore(Rankable r, List<Rankable> prevRanked);
}
