package edu.cmu.ark;

import java.util.*;

import edu.stanford.nlp.trees.Tree;

public class InitialTransformationStep {

	

	public InitialTransformationStep(){
		simplifier = new SentenceSimplifier();
		simplifier.setExtractFromVerbComplements(false);
		simplifier.setBreakNPs(false);
		doNonPronounNPC = false;
		doPronounNPC = true;
		
	}
	
	public List<Question> transform(List<Tree> sentences){
		List<Question> trees = new ArrayList<Question>();
		
		if (doPronounNPC || doNonPronounNPC){
			if(npc == null) npc = new NPClarification();
			npc.resolveCoreference(sentences);
		}
		
		
		Collection<Question> tmpSet;
		
		//extract simplifications for each input sentence and record their input sentence numbers
		int sentnum = 0;
		for(Tree sentence: sentences){
			if(AnalysisUtilities.filterOutSentenceByPunctuation(sentence.yield().toString())){
				sentnum++;
				continue;
			}
			
			tmpSet = simplifier.simplify(sentence, false);
			for(Question q: tmpSet){
				q.setSourceSentenceNumber(sentnum);
				if(doPronounNPC || doNonPronounNPC) q.setSourceDocument(npc.getDocument());
			}
			trees.addAll(tmpSet);
			
			sentnum++;
		}
		
		//add new sentences with clarified/resolved NPs
		if (doPronounNPC) trees.addAll(npc.clarifyNPs(trees, doPronounNPC, doNonPronounNPC));
		
		
		//upcase the first tokens of all output trees.
		for(Question q: trees){
			AnalysisUtilities.upcaseFirstToken(q.getIntermediateTree());
		}
		
		return trees;
	}

	public boolean doingPronounNPC() {
		return doPronounNPC;
	}

	public void setDoPronounNPC(boolean b) {
		this.doPronounNPC = b;
	}
	
	public boolean doingNonPronounNPC() {
		return doNonPronounNPC;
	}

	public void setDoNonPronounNPC(boolean b) {
		this.doNonPronounNPC = b;
	}
	
	public SentenceSimplifier getSimplifier() {
		return simplifier;
	}



	private SentenceSimplifier simplifier;
	private NPClarification npc;
	private boolean doPronounNPC;
	private boolean doNonPronounNPC;

	
}
