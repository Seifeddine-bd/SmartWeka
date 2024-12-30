package main.algorithms.classification;

import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import java.util.Random;

public class J48Classifier {
    private J48 classifier;
    private Evaluation evaluation;

    public J48Classifier() {
        classifier = new J48();
    }

    public J48Classifier(double d, int i) {
		// TODO Auto-generated constructor stub
	}

	public void train(Instances trainingData) throws Exception {
        if (trainingData.classIndex() < 0) {
            throw new Exception("Class attribute must be set before training the classifier.");
        }
        
        evaluation = new Evaluation(trainingData);
        Random rand = new Random(1);
        int folds = 10;
        evaluation.crossValidateModel(classifier, trainingData, folds, rand);
        
        classifier.buildClassifier(trainingData);
    }

    public double classifyInstance(Instance instance) throws Exception {
        return classifier.classifyInstance(instance);
    }

    public String getModelSummary() throws Exception {
        StringBuilder summary = new StringBuilder();
        summary.append("=== J48 Decision Tree Model Summary ===\n\n");
        
        summary.append("Model Structure:\n");
        summary.append(classifier.toString());
        summary.append("\n\n");
        
        if (evaluation != null) {
            summary.append("=== Model Performance ===\n");
            summary.append(String.format("Accuracy: %.2f%%\n", evaluation.pctCorrect()));
            summary.append(String.format("Precision: %.3f\n", evaluation.weightedPrecision()));
            summary.append(String.format("Recall: %.3f\n", evaluation.weightedRecall()));
            summary.append(String.format("F1-Score: %.3f\n", evaluation.weightedFMeasure()));
            summary.append("\nConfusion Matrix:\n");
            summary.append(evaluation.toMatrixString());
        }
        
        return summary.toString();
    }

    public void setOptions(String[] options) throws Exception {
        classifier.setOptions(options);
    }

    public String[] getOptions() {
        return classifier.getOptions();
    }
}