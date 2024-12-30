package main.algorithms.classification;

import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

public class KNNClassifier {
    private IBk classifier;
    private Evaluation evaluation;
    private int k;
    private DistanceFunction distanceFunction;

    public enum DistanceMetric {
        EUCLIDEAN,
        MANHATTAN
    }

    public KNNClassifier(int k, DistanceMetric metric) {
        this.k = k;
        this.classifier = new IBk(k);
        setDistanceMetric(metric);
    }

    public KNNClassifier(int k) {
        this(k, DistanceMetric.EUCLIDEAN); // Métrique par défaut
    }

    private void setDistanceMetric(DistanceMetric metric) {
        switch (metric) {
            case MANHATTAN:
                this.distanceFunction = new ManhattanDistance();
                break;
            case EUCLIDEAN:
            default:
                this.distanceFunction = new EuclideanDistance();
                break;
        }
        try {
			classifier.getNearestNeighbourSearchAlgorithm().setDistanceFunction(distanceFunction);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void train(Instances trainingData) throws Exception {
        if (trainingData.classIndex() < 0) {
            throw new Exception("Class attribute must be set before training the classifier.");
        }
        
        // Configuration de la fonction de distance pour les données d'entraînement
        distanceFunction.setInstances(trainingData);
        
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
        summary.append("=== K-Nearest Neighbors Classifier Summary ===\n\n");
        summary.append("Model Configuration:\n");
        summary.append("K value: " + k + "\n");
        summary.append("Distance metric: " + distanceFunction.getClass().getSimpleName() + "\n\n");

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

    // Getters et setters
    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
        classifier.setKNN(k);
    }

    public DistanceFunction getDistanceFunction() {
        return distanceFunction;
    }

    public void setDistanceFunction(DistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
        try {
			classifier.getNearestNeighbourSearchAlgorithm().setDistanceFunction(distanceFunction);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}