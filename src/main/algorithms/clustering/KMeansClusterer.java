package main.algorithms.clustering;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.ManhattanDistance;
import weka.core.SelectedTag;
import weka.core.Instance;

public class KMeansClusterer {
    private SimpleKMeans clusterer;
    private int numClasses;
    private Instances initialCenters;
    private DistanceMetric distanceMetric;
    private Instances data;

    // Enum for distance metrics
    public enum DistanceMetric {
        EUCLIDEAN,
        MANHATTAN
    }

    // Constructor with all parameters
    public KMeansClusterer(int numClasses, Instances initialCenters, DistanceMetric metric) {
        this.numClasses = numClasses;
        this.initialCenters = initialCenters;
        this.distanceMetric = metric;
        this.clusterer = new SimpleKMeans();
        
        initializeClusterer();
    }

    // Constructor with only number of classes and metric
    public KMeansClusterer(int numClasses, DistanceMetric metric) {
        this(numClasses, null, metric);
    }

    // Default constructor
    public KMeansClusterer() {
        this(3, null, DistanceMetric.EUCLIDEAN);
    }

    private void initializeClusterer() {
        try {
            // Set number of clusters
            clusterer.setNumClusters(numClasses);
            
            // Set distance function based on metric
            DistanceFunction distanceFunction = switch(distanceMetric) {
                case MANHATTAN -> new ManhattanDistance();
                case EUCLIDEAN -> new EuclideanDistance();
            };
            clusterer.setDistanceFunction(distanceFunction);

            // Configure other parameters
            clusterer.setPreserveInstancesOrder(true);
            
            // If we have initial centers, we'll use a specific seed and initialization method
            if (initialCenters != null) {
                clusterer.setSeed(1);  // Fixed seed for reproducibility
                clusterer.setInitializationMethod(new SelectedTag(
                    SimpleKMeans.KMEANS_PLUS_PLUS, SimpleKMeans.TAGS_SELECTION));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildClusterer(Instances data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Les données ne peuvent pas être nulles");
        }

        // Remove class attribute if it exists
        if (data.classIndex() >= 0) {
            data = new Instances(data);
            data.setClassIndex(-1);
        }

        // Check for missing values
        for (int i = 0; i < data.numInstances(); i++) {
            if (data.instance(i).hasMissingValue()) {
                throw new IllegalArgumentException("Les données contiennent des valeurs manquantes à l'instance: " + i);
            }
        }

        // If initial centers are provided, replace the first numClasses instances with them
        if (initialCenters != null) {
            validateInitialCenters(data);
            Instances modifiedData = new Instances(data);
            
            // Replace first numClasses instances with initial centers
            for (int i = 0; i < numClasses && i < initialCenters.numInstances(); i++) {
                if (i < modifiedData.numInstances()) {
                    modifiedData.set(i, initialCenters.instance(i));
                } else {
                    modifiedData.add(initialCenters.instance(i));
                }
            }
            
            this.data = modifiedData;
            clusterer.buildClusterer(modifiedData);
        } else {
            this.data = data;
            clusterer.buildClusterer(data);
        }
    }

    private void validateInitialCenters(Instances data) throws Exception {
        if (initialCenters.numAttributes() != data.numAttributes()) {
            throw new IllegalArgumentException(
                "Les centres initiaux doivent avoir le même nombre d'attributs que les données");
        }
        if (initialCenters.numInstances() != numClasses) {
            throw new IllegalArgumentException(
                "Le nombre de centres initiaux doit être égal au nombre de classes");
        }
    }

    public String getModelSummary() throws Exception {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Résultats du Clustering K-Means ===\n\n");
        
        summary.append("Paramètres du Clustering:\n");
        summary.append("Nombre de classes: ").append(numClasses).append("\n");
        summary.append("Métrique de distance: ").append(distanceMetric).append("\n");
        summary.append("Centres initiaux: ")
               .append(initialCenters != null ? "Personnalisés" : "Aléatoires")
               .append("\n\n");

        summary.append("Statistiques du Clustering:\n");
        summary.append("Nombre d'itérations: ").append(clusterer.getMaxIterations()).append("\n");
        summary.append("Somme des erreurs quadratiques: ")
               .append(String.format("%.2f", clusterer.getSquaredError()))
               .append("\n\n");

        summary.append("Centroïdes des Clusters:\n");
        Instances centroids = clusterer.getClusterCentroids();
        for (int i = 0; i < numClasses; i++) {
            summary.append("Cluster ").append(i).append(": ").append(centroids.instance(i)).append("\n");
        }

        summary.append("\nTaille des Clusters:\n");
        double[] sizes = clusterer.getClusterSizes();
        for (int i = 0; i < numClasses; i++) {
            summary.append("Cluster ").append(i).append(": ")
                   .append((int)sizes[i])
                   .append(" instances (")
                   .append(String.format("%.2f", (sizes[i] * 100.0 / data.numInstances())))
                   .append("%)\n");
        }

        return summary.toString();
    }

    // Getters
    public SimpleKMeans getClusterer() {
        return clusterer;
    }

    public int[] getClusterAssignments() throws Exception {
        if (data == null) {
            throw new IllegalStateException("Le clusterer n'a pas encore été construit.");
        }
        return clusterer.getAssignments();
    }

    public void setInitialCenters(Instances centers) {
        this.initialCenters = centers;
        initializeClusterer(); // Reinitialize with new centers
    }

    public void setDistanceMetric(DistanceMetric metric) throws Exception {
        this.distanceMetric = metric;
        DistanceFunction distanceFunction = switch(metric) {
            case MANHATTAN -> new ManhattanDistance();
            case EUCLIDEAN -> new EuclideanDistance();
        };
        clusterer.setDistanceFunction(distanceFunction);
    }

    public int getNumClasses() {
        return numClasses;
    }

    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    public Instances getInitialCenters() {
        return initialCenters;
    }
}