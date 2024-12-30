package main.utils;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import java.io.File;

public class DataLoader {
    private static DataLoader instance;
    private Instances employeeData;
    private Instances clusteringData;

    private DataLoader() {}

    public static DataLoader getInstance() {
        if (instance == null) {
            instance = new DataLoader();
        }
        return instance;
    }

    public Instances loadEmployeeData() throws Exception {
        if (employeeData == null) {
            File dataFile = new File("data/BDD.arff");
            if (!dataFile.exists()) {
                throw new Exception("Le fichier BDD.arff n'existe pas dans le dossier data/");
            }
            employeeData = DataSource.read("data/BDD.arff");
            employeeData.setClassIndex(employeeData.numAttributes() - 1);
        }
        return employeeData;
    }

    public Instances loadClusteringData() throws Exception {
        if (clusteringData == null) {
            File dataFile = new File("data/clustering_data.arff");
            if (!dataFile.exists()) {
                throw new Exception("Le fichier clustering_data.arff n'existe pas dans le dossier data/");
            }
            clusteringData = DataSource.read("data/clustering_data.arff");
        }
        return clusteringData;
    }

    public void refreshData() {
        employeeData = null;
        clusteringData = null;
    }
}