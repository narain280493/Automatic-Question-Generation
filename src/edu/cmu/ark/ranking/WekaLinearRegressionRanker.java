package edu.cmu.ark.ranking;

import java.util.*;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.*;

import java.util.List;

import edu.cmu.ark.GlobalProperties;


public class WekaLinearRegressionRanker extends BaseRanker implements IRanker {

	private static final long serialVersionUID =  -2634242092831187354L;
	public WekaLinearRegressionRanker(){
		setParameter("regularizer", 0.0001);
	}
	
	@Override
	public void rank(List<Rankable> unranked, boolean doSort) {
		for(Rankable r: unranked){
			r.score = predict(r);
		}
		
		if(doSort){ 
			Collections.sort(unranked);
			Collections.reverse(unranked);
		}
	}


	@Override
	public void train(List<List<Rankable>> trainData) {
		int numFeatures = trainData.get(0).get(0).features.length;
		
		//perform training
		Instances instances = new Instances("rating", wekaAttributes(numFeatures), 0);
		instances.setClassIndex(instances.numAttributes()-1);

		classifier = createClassifierObject();

		//if(QuestionTransducer.DEBUG) classifier.setDebug(true);

		for(int i=0;i<trainData.size();i++){
			for(int j=0;j<trainData.get(i).size();j++){
				Rankable point = trainData.get(i).get(j);
				Instance inst = makeInstance(getDefaultInstances(numFeatures), point.features, point.label);
				instances.add(inst);
			}
		}
		
		try {
			classifier.buildClassifier(instances);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if(GlobalProperties.getDebug()) System.err.println(classifier.toString());
	}
	
	public Classifier getClassifier(){
		return classifier;
	}
	
	private Classifier createClassifierObject() {
		Classifier res;
		
		
		//res = new SMOreg();
		
		
		String[] opts;
		res = new LinearRegression();
		//((LinearRegression)res).turnChecksOff(); //option to turn off standardization of feature values
		opts = new String[5];
		//opts = new String[6];
		opts[0] = "-R";
		opts[1] = ((Double)getParameter("regularizer")).toString();
		opts[2] = "-S";
		opts[3] = "1"; //no attribute selection
		opts[4] = "-C"; //do not try to eliminate colinear attributes
		//opts[5] = "-D";
		
		try {
			res.setOptions(opts);
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
		

		return res;
	}
	
	
	private Instance makeInstance(Instances dataset, double [] featureValues, double labelValue){
		Instance res = null;
		res = new Instance(featureValues.length+1);
		res.setDataset(dataset);
		for(int i=0; i<featureValues.length; i++){
			res.setValue(i, featureValues[i]); 
		}
	
		res.setClassValue(labelValue);


		return res;
	}
	
	

	
	protected FastVector wekaAttributes(int numFeatures) {
		if(wekaAttributes == null){
			wekaAttributes = new FastVector();
			Attribute att;
			//System.err.println("NUM FEATURES "+ numFeatures + "\t"+Question.getFeatureNames().size());
			for(int i=0; i<numFeatures; i++){
				att = new Attribute("feature"+i);
				//att = new Attribute(Question.getFeatureNames().get(i));
				wekaAttributes.addElement(att);
			}
			
			att = new Attribute("class");
			wekaAttributes.addElement(att);
		}
		
		return wekaAttributes;
	}
	

	
	private Instances getDefaultInstances(int numFeatures){
		if(defaultInstances == null){
			defaultInstances = new Instances("default", wekaAttributes(numFeatures), 0);
			defaultInstances.setClassIndex(defaultInstances.numAttributes()-1);
		}
		return defaultInstances;
	}
	
	
	/**
	 * Predict the labels for a given (question) instance.
	 * 
	 * @return list of estimated scores (probabilities) for the numDimensions factors 
	 */
	public Double predict(Rankable point){
		Double res = 0.0;
		Instance inst;

		inst = makeInstance(getDefaultInstances(point.features.length), point.features, 0.0);
	
		try {
			res = classifier.classifyInstance(inst);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	
	private FastVector wekaAttributes;
	private Classifier classifier;
	private Instances defaultInstances;
}
