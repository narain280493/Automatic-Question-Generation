package edu.cmu.ark;



import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.trees.Tree;

public class TreeUtil {
	
    /** Index all the leaves in a tree and return the tree. This appends
     * "dash-index", such as -1, -2, -3, ..., to the leaves
     * @param tree the Tree whose leaves need to be indexed
     * @return the tree whose leaves are indexed
     * @ref another way to do this: https://mailman.stanford.edu/pipermail/parser-user/2009-July/000247.html
     */
    public static Tree indexLeaves (Tree tree) {
    	if (tree == null) return null;
    	List<Tree> leavesList = tree.getLeaves();
    	int i=1;
    	String lab;
    	for (Tree leaf:leavesList) {
    		lab = leaf.label().value();
    		lab = lab+"-"+i;
    		leaf.label().setValue(lab);
    		i++;
    	}

    	return tree;
    }
    	
	/* Return the labels of a tree, without any spaces
	 */
	public static String getTightLabel(Tree tree) {
		List<LabeledWord> labelList = tree.labeledYield();
		
		String label = "";
		Iterator<LabeledWord> labelIter = labelList.iterator();
		LabeledWord labeledWord;
		while (labelIter.hasNext()) {
			// this is a bit confusing, but don't blame me...
			// labeled Word has a form of paper/NN, paper is the value() and NN is the tag()
			labeledWord = labelIter.next();
			if (labeledWord.tag().value().equals("-NONE-")) continue;
			//label += labeledWord.value().replaceAll("-LRB-", "(").replaceAll("-RRB-", ")");
			label += labeledWord.value();
		}
		
		return label;
	}
	
	/* Return the labels of a tree, without any spaces, and removed the index of leaves
	 */
	public static String getTightLabelNoIndex(Tree tree) {
		List<LabeledWord> labelList = tree.labeledYield();
		
		String label = "";
		Iterator<LabeledWord> labelIter = labelList.iterator();
		LabeledWord labeledWord;
		while (labelIter.hasNext()) {
			// this is a bit confusing, but don't blame me...
			// labeled Word has a form of paper/NN, paper is the value() and NN is the tag()
			labeledWord = labelIter.next();
			if (labeledWord.tag().value().equals("-NONE-")) continue;
			label += labeledWord.value().replaceFirst("-\\d+$", "");
		}
		
		return label;
	}
	
	/* Return the labels of a tree, joined with spaces
	 */
	public static String getLabel(Tree tree) {
		List<LabeledWord> labelList = tree.labeledYield();
		
		String label = "";
		Iterator<LabeledWord> labelIter = labelList.iterator();
		LabeledWord labeledWord;
		while (labelIter.hasNext()) {
			// this is a bit confusing, but don't blame me...
			// labeled Word has a form of paper/NN, paper is the value() and NN is the tag()
			labeledWord = labelIter.next();
			if (labeledWord.tag().value().equals("-NONE-")) continue;
			label += labeledWord.value().replaceAll("-LRB-", "(").replaceAll("-RRB-", ")")+" ";
			//label += labeledWord.value()+" ";
		}
		
		return label.trim();
	}
	/* Return the labels of a tree, joined with spaces, and removed the index of leaves
	 */
	public static String getLabelNoIndex(Tree tree) {
		List<LabeledWord> labelList = tree.labeledYield();
		
		String label = "";
		Iterator<LabeledWord> labelIter = labelList.iterator();
		LabeledWord labeledWord;
		while (labelIter.hasNext()) {
			// this is a bit confusing, but don't blame me...
			// labeled Word has a form of paper/NN, paper is the value() and NN is the tag()
			labeledWord = labelIter.next();
			if (labeledWord.tag().value().equals("-NONE-")) continue;
			label += labeledWord.value().replaceFirst("-\\d+$", "")+" ";
		}
		
		return label.trim();
	}
}
