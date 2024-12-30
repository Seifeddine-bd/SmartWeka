package main;
import java.io.BufferedReader;
import java.io.FileReader;
import weka.core.Instances;
import weka.clusterers.SimpleKMeans;



public class SimpleExample {
	
    public static void main(String[] args) {

        try {
            String datasetPath = "data/points_data.arff";

            BufferedReader bufferedReader = new BufferedReader(new FileReader(datasetPath));

            Instances datasetInstances = new Instances(bufferedReader);

            bufferedReader.close();

            SimpleKMeans km=new SimpleKMeans();
            km.setNumClusters(3);

            km.buildClusterer(datasetInstances);

            System.out.println(km);

        } catch (Exception e) {
            System.out.println("Error Occurred!!!! \n" + e.getMessage());
        }
    }


}
