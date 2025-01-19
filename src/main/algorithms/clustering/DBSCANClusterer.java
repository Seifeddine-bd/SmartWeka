package main.algorithms.clustering;

import weka.clusterers.AbstractClusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * DBSCAN clustering algorithm implementation.
 */
public class DBSCANClusterer extends AbstractClusterer implements OptionHandler, TechnicalInformationHandler {

    private double epsilon;        // Epsilon distance
    private int minPoints;         // Minimum points to form a cluster
    private int[] clusterAssignments; // Cluster assignments for instances
    private int numClusters;       // Number of clusters
    private DistanceFunction distanceFunction; // Distance function for instance distance calculations
    private PointType[] pointTypes; // Types of points

    // Enum to represent point types
    private static enum PointType {
        CORE, BORDER, NOISE
    }

    public DBSCANClusterer() {
        // Default constructor
        this.epsilon = 0.5; // default value
        this.minPoints = 5; // default value
        distanceFunction = new EuclideanDistance(); // default distance function
    }

    public DBSCANClusterer(double epsilon, int minPoints) {
        this.epsilon = epsilon;
        this.minPoints = minPoints;
        distanceFunction = new EuclideanDistance(); // default distance function
    }

    @Override
    public void buildClusterer(Instances data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        // Check for missing values
        for (int i = 0; i < data.numInstances(); i++) {
            if (data.instance(i).hasMissingValue()) {
                throw new IllegalArgumentException("Data contains missing values at instance: " + i);
            }
        }

        // Initialize assignments and clusters
        clusterAssignments = new int[data.numInstances()];
        pointTypes = new PointType[data.numInstances()];
        numClusters = 0;

        // Initialize the distance function with the dataset
        distanceFunction.setInstances(data);

        // DBSCAN algorithm implementation
        boolean[] visited = new boolean[data.numInstances()];
        for (int i = 0; i < data.numInstances(); i++) {
            if (!visited[i]) {
                visited[i] = true;
                List<Integer> neighbors = getNeighbors(data, i);

                if (neighbors.size() < minPoints) {
                    clusterAssignments[i] = -1; // Mark as noise
                    pointTypes[i] = PointType.NOISE;
                } else {
                    numClusters++;
                    expandCluster(data, i, neighbors, visited);
                }
            }
        }

        // Determine border points
        for (int i = 0; i < data.numInstances(); i++) {
            if (clusterAssignments[i] != -1 && pointTypes[i] == null) {
                pointTypes[i] = PointType.BORDER;
            }
        }
    }

    private List<Integer> getNeighbors(Instances data, int index) {
        List<Integer> neighbors = new ArrayList<>();
        Instance instance = data.instance(index);

        for (int i = 0; i < data.numInstances(); i++) {
            Instance otherInstance = data.instance(i);
            double distance = distanceFunction.distance(instance, otherInstance);
            if (instance != otherInstance && distance <= epsilon) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    private void expandCluster(Instances data, int index, List<Integer> neighbors, boolean[] visited) {
        clusterAssignments[index] = numClusters; // Assign cluster number
        pointTypes[index] = PointType.CORE; // Mark as core

        for (int i = 0; i < neighbors.size(); i++) {
            int neighborIndex = neighbors.get(i);

            if (!visited[neighborIndex]) {
                visited[neighborIndex] = true;
                List<Integer> neighborNeighbors = getNeighbors(data, neighborIndex);

                if (neighborNeighbors.size() >= minPoints) {
                    neighbors.addAll(neighborNeighbors); // Add to neighbors if it's a core point
                    pointTypes[neighborIndex] = PointType.CORE;
                } else {
                    pointTypes[neighborIndex] = PointType.BORDER;
                }
            }

            if (clusterAssignments[neighborIndex] == 0) {
                clusterAssignments[neighborIndex] = numClusters; // Assign cluster number
            }
        }
    }

    @Override
    public int numberOfClusters() {
        return numClusters;
    }

    @Override
    public int clusterInstance(Instance instance) throws Exception {
        int index = instance.classIndex(); // Get the index of the instance
        if (index >= 0 && index < clusterAssignments.length) {
            return clusterAssignments[index]; // Return the cluster assignment
        } else {
            throw new IllegalArgumentException("Instance index out of bounds.");
        }
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        double[] distribution = new double[numberOfClusters()];
        // Logic for generating distribution can be added here
        return distribution;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DBSCAN Clustering Algorithm\n");
        sb.append("Epsilon: ").append(epsilon).append("\n");
        sb.append("Minimum Points: ").append(minPoints).append("\n");
        sb.append("Number of clusters: ").append(numClusters).append("\n");
        sb.append("Cluster Assignments and Point Types:\n");

        for (int i = 0; i < clusterAssignments.length; i++) {
            sb.append("Instance ").append(i).append(": Cluster ")
              .append(clusterAssignments[i]).append(", Type ")
              .append(pointTypes[i]).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String[] getOptions() {
        return new String[]{
                "-E", String.valueOf(epsilon),
                "-M", String.valueOf(minPoints)
        };
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        String epsilonString = Utils.getOption("E", options);
        if (epsilonString.length() != 0) {
            epsilon = Double.parseDouble(epsilonString);
        }

        String minPointsString = Utils.getOption("M", options);
        if (minPointsString.length() != 0) {
            minPoints = Integer.parseInt(minPointsString);
        }
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        // Implement technical information if necessary
        return null;
    }
}