package main.algorithms.clustering;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Instances;
import weka.core.EuclideanDistance;
import weka.core.SelectedTag;

public class CAHClusterer {
    private HierarchicalClusterer clusterer;
    private int numClusters;
    private Instances data;
    private int linkageType; // 0: SINGLE, 1: COMPLETE, 2: AVERAGE

    public CAHClusterer(int numClusters, int linkageType) {
        this.numClusters = numClusters;
        this.linkageType = linkageType;
        this.clusterer = new HierarchicalClusterer();
        try {
            clusterer.setNumClusters(numClusters);
            clusterer.setDistanceFunction(new EuclideanDistance());
            // Set the chosen linkage type
            clusterer.setLinkType(new SelectedTag(linkageType, HierarchicalClusterer.TAGS_LINK_TYPE));
            // Print the entire cluster hierarchy
            clusterer.setPrintNewick(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildClusterer(Instances data) throws Exception {
        this.data = data;
        // Remove class attribute if it exists
        if (data.classIndex() >= 0) {
            data = new Instances(data);
            data.setClassIndex(-1);
        }
        // Check if the number of clusters is less than or equal to the number of instances
        if (numClusters > data.numInstances()) {
            throw new IllegalArgumentException("Number of clusters cannot exceed the number of instances.");
        }
        clusterer.buildClusterer(data);
    }

    public String getModelSummary() throws Exception {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Hierarchical Clustering Results ===\n\n");
        
        summary.append("Clustering Parameters:\n");
        summary.append("Number of clusters: ").append(numClusters).append("\n");

        // Map linkageType to a string for summary
        String linkageTypeString;
        switch (linkageType) {
            case 0:
                linkageTypeString = "SINGLE";
                break;
            case 1:
                linkageTypeString = "COMPLETE";
                break;
            case 2:
                linkageTypeString = "AVERAGE";
                break;
            default:
                linkageTypeString = "UNKNOWN";
        }
        
        summary.append("Linkage type: ").append(linkageTypeString).append("\n");
        summary.append("Distance function: Euclidean Distance\n\n");
        
        summary.append("Clustering Statistics:\n");
        // Get assignments for each instance
        int[] assignments = new int[data.numInstances()];
        for (int i = 0; i < data.numInstances(); i++) {
            assignments[i] = clusterer.clusterInstance(data.instance(i));
        }
        
        // Count instances per cluster
        int[] clusterSizes = new int[numClusters];
        for (int assignment : assignments) {
            clusterSizes[assignment]++;
        }
        
        summary.append("\nCluster Sizes:\n");
        for (int i = 0; i < clusterSizes.length; i++) {
            summary.append("Cluster ").append(i).append(": ").append(clusterSizes[i]).append(" instances\n");
        }

        return summary.toString();
    }

    // New method to get the Newick representation of the dendrogram
    public String getDendrogramNewick() throws Exception {
        return clusterer.graph(); // Get the graph in Newick format
    }
}
