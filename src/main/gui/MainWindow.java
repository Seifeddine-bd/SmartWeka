package main.gui;
import weka.core.converters.ConverterUtils.DataSource;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import main.algorithms.classification.J48Classifier;
import main.algorithms.classification.KNNClassifier;
import main.algorithms.clustering.CAHClusterer;
import main.algorithms.clustering.DBSCANClusterer;
import main.algorithms.clustering.KMeansClusterer;
import main.utils.ResultVisualizer;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

public class MainWindow extends JFrame {
    private JTable dataTable;
    private JComboBox<String> algorithmTypeCombo;
    private JComboBox<String> specificAlgorithmCombo;
    private JRadioButton defaultParamsRadio;
    private JRadioButton customParamsRadio;
    private JPanel parameterPanel;
    private JPanel visualizationPanel ;
    private JTextArea resultsArea;
    private Instances data;
    private Map<String, Object> customParameters; // Store custom parameters
    private JTextField centersField; // To store centers input field reference
    ResultVisualizer visualizer ;

    public MainWindow() {
    	
        setTitle("SmartWeka");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(10, 10));

        
        // Create and set up the menu bar
        setupMenuBar();

        // Main content panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left panel for data and algorithm selection
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        setupDataPanel(leftPanel);
        setupAlgorithmPanel(leftPanel);
        leftPanel.setPreferredSize(new Dimension(400, getHeight()));

        // Center panel for results
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        setupResultsPanel(centerPanel);

        // Add panels to main container
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
        
        visualizer = new ResultVisualizer();


        resultsArea.setFont(new Font("Serif", Font.ITALIC, 12));
        resultsArea.setMargin(new Insets(5, 5, 5, 5));
        
        customParameters = new HashMap<>();
        
        // Add listener for custom parameters radio button
        customParamsRadio.addActionListener(e -> {
            if (customParamsRadio.isSelected()) {
                showCustomParametersDialog();
            }
        });
    
    }
    
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadItem = new JMenuItem("Load Dataset");
        JMenuItem saveItem = new JMenuItem("Save Results");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        loadItem.addActionListener(e -> loadData());
        saveItem.addActionListener(e -> saveResults());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem userManualItem = new JMenuItem("User Manual");
        JMenuItem aboutItem = new JMenuItem("About");
        
        userManualItem.addActionListener(e -> showUserManual());
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(userManualItem);
        helpMenu.add(aboutItem);
        
        
        
        // File Menu
        JMenu datasetMenu = new JMenu("Datasets");
        JMenuItem employeesItem = new JMenuItem("Employees Dataset");
        JMenuItem pointsItem = new JMenuItem("Points Dataset");

        
        employeesItem.addActionListener(e -> employeesData());
        pointsItem.addActionListener(e -> pointsData());
 
        

        datasetMenu.add(employeesItem);
        datasetMenu.addSeparator();
        datasetMenu.add(pointsItem);
     
        
        menuBar.add(fileMenu);
        menuBar.add(datasetMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupDataPanel(JPanel container) {
        JPanel dataPanel = new JPanel(new BorderLayout(5, 5));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));

        JButton loadButton = new JButton("Load Dataset");
        loadButton.addActionListener(e -> loadData());

        dataTable = new JTable();
        JScrollPane tableScroll = new JScrollPane(dataTable);
        
        dataPanel.add(loadButton, BorderLayout.NORTH);
        dataPanel.add(tableScroll, BorderLayout.CENTER);
        
        container.add(dataPanel, BorderLayout.CENTER);
    }

    private void setupAlgorithmPanel(JPanel container) {
        JPanel algoPanel = new JPanel(new GridBagLayout());
        algoPanel.setBorder(BorderFactory.createTitledBorder("Algorithm"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Algorithm type selection
        algorithmTypeCombo = new JComboBox<>(new String[]{"Classification", "Clustering"});
        specificAlgorithmCombo = new JComboBox<>();
        algorithmTypeCombo.setSelectedIndex(-1);
        algorithmTypeCombo.addActionListener(e -> updateSpecificAlgorithms());
        
        // Parameter selection
        ButtonGroup paramGroup = new ButtonGroup();
        defaultParamsRadio = new JRadioButton("Default Parameters");
        customParamsRadio = new JRadioButton("Custom Parameters");
        paramGroup.add(defaultParamsRadio);
        paramGroup.add(customParamsRadio);
        defaultParamsRadio.setSelected(true);

        // Parameter panel
        parameterPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        parameterPanel.setVisible(false);
        
        // Control buttons
        JButton refreshButton = new JButton("Refresh");
        JButton runButton = new JButton("Run");
        
        refreshButton.addActionListener(e -> refresh());
        runButton.addActionListener(e -> runAlgorithm());

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        algoPanel.add(new JLabel("Algorithm Type:"), gbc);
        
        gbc.gridx = 1;
        algoPanel.add(algorithmTypeCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        algoPanel.add(new JLabel("Specific Algorithm:"), gbc);
        
        gbc.gridx = 1;
        algoPanel.add(specificAlgorithmCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        algoPanel.add(defaultParamsRadio, gbc);
        
        gbc.gridy = 3;
        algoPanel.add(customParamsRadio, gbc);
        
        gbc.gridy = 4;
        algoPanel.add(parameterPanel, gbc);
        
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        algoPanel.add(refreshButton, gbc);
        
        gbc.gridx = 1;
        algoPanel.add(runButton, gbc);
        
        container.add(algoPanel, BorderLayout.SOUTH);
    }


    private void setupResultsPanel(JPanel container) {
        // Create results panel
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultsPanel.setPreferredSize(new Dimension(400, 300)); // Set fixed size for results panel
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        
        JButton saveButton = new JButton("Save Results");
        saveButton.addActionListener(e -> saveResults());
        
        resultsPanel.add(resultsScroll, BorderLayout.CENTER);
        resultsPanel.add(saveButton, BorderLayout.SOUTH);
        
        // Create visualization panel
        visualizationPanel = new JPanel(new BorderLayout(5, 5));
        visualizationPanel.setBorder(BorderFactory.createTitledBorder("Visualization"));
        visualizationPanel.setPreferredSize(new Dimension(400, 300)); // Set fixed size for visualization panel
        JTextArea visualizationLabel = new JTextArea();
        visualizationPanel.add(visualizationLabel, BorderLayout.CENTER);
        
        // Create a Box to hold the results and visualization panels
        Box box = Box.createVerticalBox();
        box.add(resultsPanel);
        box.add(visualizationPanel);
        
        // Add the box to the container
        container.add(box);
    }

    private void updateSpecificAlgorithms() {
        specificAlgorithmCombo.removeAllItems();
        if (algorithmTypeCombo.getSelectedItem().equals("Classification")) {
            for (String algo : new String[]{"Decision Tree", "KNN"}) {
                specificAlgorithmCombo.addItem(algo);
            }
        } else {
            for (String algo : new String[]{"K-means", "DBSCAN", "Hierarchical Clustering"}) {
                specificAlgorithmCombo.addItem(algo);
            }
        }
    }

    private void selectClassAttribute() {
        if (data == null) return;
        
        String[] attributeNames = new String[data.numAttributes()];
        for (int i = 0; i < data.numAttributes(); i++) {
            attributeNames[i] = data.attribute(i).name();
        }
        
        String selected = (String) JOptionPane.showInputDialog(this,
            "Select the class attribute:",
            "Class Attribute Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            attributeNames,
            attributeNames[attributeNames.length - 1]);
            
        if (selected != null) {
            int selectedIndex = -1;
            for (int i = 0; i < attributeNames.length; i++) {
                if (attributeNames[i].equals(selected)) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex != -1) {
                data.setClassIndex(selectedIndex);
            }
        }
    }
 
    private void loadData() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (file.getName().endsWith(".arff")) {
                    ArffLoader loader = new ArffLoader();
                    loader.setFile(file);
                    data = loader.getDataSet();
                } else if (file.getName().endsWith(".csv")) {
                    CSVLoader loader = new CSVLoader();
                    loader.setSource(file);
                    data = loader.getDataSet();
                }
                
                // Set the last attribute as the class attribute by default
                if (data.numAttributes() > 0) {
                    data.setClassIndex(data.numAttributes() - 1);
                }
                
                updateDataTable();
                selectClassAttribute(); 
                } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateDataTable() {
        if (data == null) return;
        
        DefaultTableModel model = new DefaultTableModel();
        
        // Add column headers
        for (int i = 0; i < data.numAttributes(); i++) {
            model.addColumn(data.attribute(i).name());
        }
        
        // Add data rows
        for (int i = 0; i < data.numInstances(); i++) {
            Object[] row = new Object[data.numAttributes()];
            for (int j = 0; j < data.numAttributes(); j++) {
                row[j] = data.instance(i).value(j);
            }
            model.addRow(row);
        }
        
        dataTable.setModel(model);
    }

    public  void pointsData() {
        try {
            // Define the path to the 'data' folder
            String folderPath = "data";

            // File names
            String fileName = "points.arff";

            // Loop through each file
                String filePath = folderPath + File.separator + fileName;

                // Load the ARFF file
                DataSource source = new DataSource(filePath);
              
				 data = source.getDataSet();
					 
                updateDataTable();
                selectClassAttribute(); 
             

           
          }
         catch (Exception e) {
            System.err.println("Error reading ARFF files.");
            e.printStackTrace();
        }}
    
    public  void employeesData()  {
        try {
            // Define the path to the 'data' folder
            String folderPath = "data";

            // File names
            String fileName = "Employees.arff";

            // Loop through each file
                String filePath = folderPath + File.separator + fileName;

                // Load the ARFF file
                DataSource source = new DataSource(filePath);
           
					 data = source.getDataSet();
				 
	                updateDataTable();
	                selectClassAttribute(); 
			
           
          }
         catch (Exception e) {
            System.err.println("Error reading ARFF files.");
            e.printStackTrace();
        }}
   
    private void showCustomParametersDialog() {
        if (specificAlgorithmCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an algorithm first.", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            defaultParamsRadio.setSelected(true);
            return;
        }

        String selectedAlgorithm = (String) specificAlgorithmCombo.getSelectedItem();
        customParameters.clear(); // Clear previous parameters

        switch (selectedAlgorithm) {
            case "KNN":
                showKNNParams();
                break;
            case "K-means":
                showKMeansParams();
                break;
            case "DBSCAN":
                showDBSCANParams();
                break;
            case "Hierarchical Clustering":
                showHierarchicalParams();
                break;
        }
    }
  

    private void showKNNParams() {
        JTextField kField = new JTextField("5");
        String[] metrics = {"EUCLIDEAN", "MANHATTAN"};
        JComboBox<String> metricCombo = new JComboBox<>(metrics);

        Object[] message = {
            "Number of neighbors (k):", kField,
            "Distance metric:", metricCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message, 
            "KNN Parameters", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int k = Integer.parseInt(kField.getText());
                String metric = (String) metricCombo.getSelectedItem();
                
                customParameters.put("k", k);
                customParameters.put("metric", KNNClassifier.DistanceMetric.valueOf(metric));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Using default parameters.");
                defaultParamsRadio.setSelected(true);
            }
        } else {
            defaultParamsRadio.setSelected(true);
        }
    }

    private Instance newIndividuKNN() {
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Dataset is not loaded. Please load a dataset first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Prepare input fields for the user
        int numAttributes = data.numAttributes() - 1; // Exclude class attribute
        String[] attributeNames = new String[numAttributes];
        for (int i = 0; i < numAttributes; i++) {
            attributeNames[i] = data.attribute(i).name();
        }

        JTextField[] inputFields = new JTextField[numAttributes];
        JPanel inputPanel = new JPanel(new GridLayout(numAttributes, 2));
        for (int i = 0; i < numAttributes; i++) {
            inputPanel.add(new JLabel(attributeNames[i] + ":"));
            inputFields[i] = new JTextField();
            inputPanel.add(inputFields[i]);
        }

        // Display dialog to collect input
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Enter values for the new individual", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Create a new instance
            	DenseInstance newInstance = new DenseInstance(numAttributes + 1); // +1 for class attribute
                newInstance.setDataset(data); // Attach dataset to include attribute information

                for (int i = 0; i < numAttributes; i++) {
                    double value = Double.parseDouble(inputFields[i].getText());
                    newInstance.setValue(i, value);
                }

                return newInstance;

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating new instance: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        return null;
    }

    /*
    private void showKMeansParams() {
        JTextField numClassesField = new JTextField("3");
        centersField = new JTextField(""); // Field for centers file path
        JButton browseCentersButton = new JButton("Browse");
        String[] metrics = {"EUCLIDEAN", "MANHATTAN"};
        JComboBox<String> metricCombo = new JComboBox<>(metrics);

        // Create a panel for centers selection
        JPanel centersPanel = new JPanel(new BorderLayout(5, 0));
        centersPanel.add(centersField, BorderLayout.CENTER);
        centersPanel.add(browseCentersButton, BorderLayout.EAST);

        browseCentersButton.addActionListener(e -> browseCentersFile());

        Object[] message = {
            "Nombre de classes:", numClassesField,
            "Centres initiaux:", centersPanel,
            "Métrique de distance:", metricCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message,
            "Paramètres K-means", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int numClasses = Integer.parseInt(numClassesField.getText());
                String metric = (String) metricCombo.getSelectedItem();
                
                customParameters.put("numClasses", numClasses);
                customParameters.put("metric", KMeansClusterer.DistanceMetric.valueOf(metric));
                
                // Handle initial centers if provided
                if (!centersField.getText().isEmpty()) {
                    File centersFile = new File(centersField.getText());
                    if (centersFile.exists()) {
                        Instances centers = loadCenters(centersFile);
                        customParameters.put("initialCenters", centers);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Entrée invalide. Utilisation des paramètres par défaut.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                defaultParamsRadio.setSelected(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des centres: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                defaultParamsRadio.setSelected(true);
            }
        } else {
            defaultParamsRadio.setSelected(true);
        }
    }
*/

    private void showKMeansParams() {
        JTextField numClassesField = new JTextField("3");
        centersField = new JTextField(""); // Field for centers input (file path or comma-separated values)
        JButton browseCentersButton = new JButton("Browse");
        String[] metrics = {"EUCLIDEAN", "MANHATTAN"};
        JComboBox<String> metricCombo = new JComboBox<>(metrics);

        // Panel for initial centers input
        JPanel centersPanel = new JPanel(new BorderLayout(5, 0));
        centersPanel.add(centersField, BorderLayout.CENTER);
        centersPanel.add(browseCentersButton, BorderLayout.EAST);

        browseCentersButton.addActionListener(e -> browseCentersFile());

        Object[] message = {
            "Nombre de classes:", numClassesField,
            "Centres initiaux (fichier ou valeurs séparées par des virgules):", centersPanel,
            "Métrique de distance:", metricCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message,
            "Paramètres K-means", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int numClasses = Integer.parseInt(numClassesField.getText());
                String metric = (String) metricCombo.getSelectedItem();

                customParameters.put("numClasses", numClasses);
                customParameters.put("metric", KMeansClusterer.DistanceMetric.valueOf(metric));

                // Handle initial centers input
                if (!centersField.getText().isEmpty()) {
                    Instances centers;
                    File centersFile = new File(centersField.getText());
                    if (centersFile.exists()) {
                        // Load centers from file
                        centers = loadCenters(centersFile);
                    } else {
                        // Parse centers from the input text
                        centers = parseCentersFromInput(centersField.getText());
                    }

                    // Validate attribute count
                    if (centers.numAttributes() != data.numAttributes()) {
                        throw new Exception("Mismatch between dataset attributes and initial centers.");
                    }

                    // Check if number of centers matches numClasses
                    if (centers.numInstances() != numClasses) {
                        throw new Exception("Number of initial centers must equal the number of classes.");
                    }

                    customParameters.put("initialCenters", centers);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Entrée invalide. Utilisation des paramètres par défaut.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                defaultParamsRadio.setSelected(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des centres: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                defaultParamsRadio.setSelected(true);
            }
        } else {
            defaultParamsRadio.setSelected(true);
        }
    }

    // Parse centers dynamically from text input
    private Instances parseCentersFromInput(String input) throws Exception {
        if (data == null) {
            throw new Exception("Dataset not loaded. Load the dataset first.");
        }

        int numAttributes = data.numAttributes(); // Dynamically determine the number of attributes
        String[] rows = input.split(";");
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < numAttributes; i++) {
            attributes.add(data.attribute(i)); // Use the dataset's attributes
        }

        Instances centers = new Instances("InitialCenters", attributes, rows.length);

        for (String row : rows) {
            String[] values = row.split(",");
            if (values.length != numAttributes) {
                throw new Exception("Invalid number of attributes in input: " + row);
            }
            double[] instanceValues = new double[numAttributes];
            for (int i = 0; i < numAttributes; i++) {
                instanceValues[i] = Double.parseDouble(values[i].trim());
            }
            centers.add(new DenseInstance(1.0, instanceValues));
        }

        return centers;
    }



private void showDBSCANParams() {
        JTextField epsilonField = new JTextField("0.1");
        JTextField minPtsField = new JTextField("6");

        Object[] message = {
            "Epsilon (radius):", epsilonField,
            "MinPoints:", minPtsField
        };

        int option = JOptionPane.showConfirmDialog(this, message, 
            "DBSCAN Parameters", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                double epsilon = Double.parseDouble(epsilonField.getText());
                int minPts = Integer.parseInt(minPtsField.getText());
                
                customParameters.put("epsilon", epsilon);
                customParameters.put("minPts", minPts);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Using default parameters.");
                defaultParamsRadio.setSelected(true);
            }
        } else {
            defaultParamsRadio.setSelected(true);
        }
    }

    private void showHierarchicalParams() {
        JTextField numClustersField = new JTextField("3");
        String[] linkTypes = {"SINGLE", "COMPLETE", "AVERAGE"};
        JComboBox<String> linkTypeCombo = new JComboBox<>(linkTypes);

        Object[] message = {
            "Number of clusters:", numClustersField,
            "Linkage type:", linkTypeCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message, 
            "Hierarchical Clustering Parameters", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int numClusters = Integer.parseInt(numClustersField.getText());
                int linkType = linkTypeCombo.getSelectedIndex() + 1;
                
                customParameters.put("numClusters", numClusters);
                customParameters.put("linkType", linkType);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Using default parameters.");
                defaultParamsRadio.setSelected(true);
            }
        } else {
            defaultParamsRadio.setSelected(true);
        }
    }

       
    private void validateSetup() {
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please load data first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Example: Enable custom parameters if selected
        if (customParamsRadio.isSelected()) {
            parameterPanel.removeAll();
            // Dynamically add input fields based on the selected algorithm
   
            // Add more conditions for other algorithms
            parameterPanel.revalidate();
            parameterPanel.repaint();
        }

        JOptionPane.showMessageDialog(this, "Setup validated successfully!", "Validation", JOptionPane.INFORMATION_MESSAGE);
    }
   
    
    private void browseCentersFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".arff");
            }
            public String getDescription() {
                return "ARFF Files (*.arff)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            centersField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private Instances loadCenters(File file) throws Exception {
        ArffLoader loader = new ArffLoader();
        loader.setFile(file);
        Instances centers = loader.getDataSet();
        
        // Ensure centers have no class attribute
        if (centers.classIndex() >= 0) {
            centers.setClassIndex(-1);
        }
        
        return centers;
    }

    private void runAlgorithm() {
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please load data first.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedAlgorithm = (String) specificAlgorithmCombo.getSelectedItem();
        StringBuilder resultsBuilder = new StringBuilder();
        visualizationPanel.removeAll();

        try {
            if (customParamsRadio.isSelected() && customParameters.isEmpty()) {
                showCustomParametersDialog();
            }

            switch (selectedAlgorithm) {
           
            case "Decision Tree":
                J48Classifier tree = new J48Classifier();
                tree.train(data); // Train the classifier with the data
                visualizationPanel.removeAll();
                // Append model summary to results
                resultsBuilder.append(tree.getModelSummary());

                // Create an instance of ResultVisualizer and visualize the decision tree
                visualizer.visualizeDecisionTree(data); // Ensure you're passing the Instances object

                // Add the visualization panel to the container
                visualizationPanel.removeAll();
                visualizationPanel.setLayout(new BorderLayout());
                visualizationPanel.add(visualizer.getPanel(), BorderLayout.CENTER);
                visualizationPanel.revalidate();
                visualizationPanel.repaint();

                System.out.println(tree.toString()); // Print the string representation of the tree

                break;

                
            case "KNN":
                KNNClassifier knn;
                visualizationPanel.removeAll();
                if (customParamsRadio.isSelected()) {
                    knn = new KNNClassifier(
                        (int) customParameters.get("k"),
                        (KNNClassifier.DistanceMetric) customParameters.get("metric")
                    );
                } else {
                    knn = new KNNClassifier(5, KNNClassifier.DistanceMetric.EUCLIDEAN);
                }

                try {
                    knn.train(data);
                    resultsBuilder.append(knn.getModelSummary());

                    // Prompt user for new individual input
                    Instance newIndividual = newIndividuKNN();
                    if (newIndividual != null) {
                        double predictedClassIndex = knn.classifyInstance(newIndividual);
                        String predictedClass = data.classAttribute().value((int) predictedClassIndex);
                        JOptionPane.showMessageDialog(this, "Predicted Class for the new individual: " + predictedClass,
                                "Classification Result", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error during KNN execution: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;

            /*
            	case "KNN":
                    KNNClassifier knn;
                    visualizationPanel.removeAll();
                    if (customParamsRadio.isSelected()) {
                        knn = new KNNClassifier(
                            (int) customParameters.get("k"),
                            (KNNClassifier.DistanceMetric) customParameters.get("metric")
                        );
                    } else {
                        knn = new KNNClassifier(5, KNNClassifier.DistanceMetric.EUCLIDEAN);
                    }
                    knn.train(data);
                    resultsBuilder.append(knn.getModelSummary());
                    break;
             */       
                case "K-means":
                    KMeansClusterer kmeans;
                    visualizationPanel.removeAll();
                    if (customParamsRadio.isSelected() && !customParameters.isEmpty()) {
                        int numClasses = (int) customParameters.get("numClasses");
                        KMeansClusterer.DistanceMetric metric = 
                            (KMeansClusterer.DistanceMetric) customParameters.get("metric");
                        
                        if (customParameters.containsKey("initialCenters")) {
                            Instances centers = (Instances) customParameters.get("initialCenters");
                            kmeans = new KMeansClusterer(numClasses, centers, metric);
                        } else {
                            kmeans = new KMeansClusterer(numClasses, metric);
                        }
                    } else {
                        kmeans = new KMeansClusterer();
                    }
                    
                    try {
                        kmeans.buildClusterer(data);
                        resultsBuilder.append(kmeans.getModelSummary());
                    } catch (Exception e) {
                        resultsBuilder.append("Erreur lors de l'exécution de K-means: ")
                                    .append(e.getMessage()).append("\n");
                        e.printStackTrace();
                    }
                    break;

                    
                case "DBSCAN":
                    DBSCANClusterer dbscan;
                    visualizationPanel.removeAll();
                
                    if (customParamsRadio.isSelected()) {
                        dbscan = new DBSCANClusterer(
                            (double) customParameters.get("epsilon"),
                            (int) customParameters.get("minPts")
                        );
                    } else {
                        dbscan = new DBSCANClusterer(0.1, 6);
                    }
                    dbscan.buildClusterer(data);
                    resultsBuilder.append(dbscan.toString());
                    break;

                case "Hierarchical Clustering":
	                    CAHClusterer cah;
	                    visualizationPanel.removeAll();
	
	                    // Configure the CAHClusterer with parameters
	                    if (customParamsRadio.isSelected()) {
	                        cah = new CAHClusterer(
	                            (int) customParameters.get("numClusters"),
	                            (int) customParameters.get("linkType")
	                        );
	                    } else {
	                        cah = new CAHClusterer(3, 1); // Default: 3 clusters, COMPLETE linkage
	                    }
	
	                    try {
	                        cah.buildClusterer(data);
	
	                        // Append clustering results to resultsBuilder
	                        resultsBuilder.append(cah.getModelSummary());
	
	                        // Visualize the dendrogram
	                        ResultVisualizer vi = new ResultVisualizer();
	                        JPanel dendrogramPanel = new JPanel(new BorderLayout());
	                        vi.visualizeCAHDendrogram(data, cah, dendrogramPanel);
	
	                        // Add the dendrogram visualization to the main panel
	                        visualizationPanel.setLayout(new BorderLayout());
	                        visualizationPanel.add(dendrogramPanel, BorderLayout.CENTER);
	                        visualizationPanel.revalidate();
	                        visualizationPanel.repaint();
	
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                        resultsBuilder.append("\nError: ").append(e.getMessage());
	                    }
	                    break;
                default:
                    resultsBuilder.append("Algorithm not implemented.\n");
            }
            
            resultsArea.setText(resultsBuilder.toString());
            
        } catch (Exception e) {
            String errorMessage = "Error running algorithm: " + e.getMessage();
            resultsArea.setText(errorMessage);
            e.printStackTrace();
        }
    }
    
    private void saveResults() {
        if (resultsArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results to save.",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.write(resultsArea.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving results: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUserManual() {
        JDialog dialog = new JDialog(this, "User Manual", true);
        JTextArea manualText = new JTextArea(
            "Smart Weka Application User Manual\n\n" +
            "1. Loading Data:\n" +
            "   - Click 'Load Dataset' to import ARFF or CSV files\n" +
            "   - Data will be displayed in the table\n\n" +
            "2. Selecting Algorithm:\n" +
            "   - Choose between Classification and Clustering\n" +
            "   - Select specific algorithm from the dropdown\n\n" +
            "3. Parameters:\n" +
            "   - Use default or custom parameters\n" +
            "   - Click 'Setup' to validate configuration\n\n" +
            "4. Running Analysis:\n" +
            "   - Click 'Run' to execute the algorithm\n" +
            "   - Results will appear in the results panel\n\n" +
            "5. Saving Results:\n" +
            "   - Click 'Save Results' to export to file"
        );
        manualText.setEditable(false);
        dialog.add(new JScrollPane(manualText));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "SmartWeka\nVersion 1.0\n\n" +
            "A desktop application for data analysis using Weka library.\n" +
            "Supports classification and clustering algorithms.",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refresh() {
        // Reset algorithm selections
        algorithmTypeCombo.setSelectedIndex(0); // Reset to the first option
        specificAlgorithmCombo.removeAllItems(); // Clear specific algorithms
        parameterPanel.removeAll(); // Clear parameter panel
        parameterPanel.setVisible(false); // Hide the parameter panel
        resultsArea.setText(""); // Clear results area
        resultsArea.setEditable(false); // Make results area non-editable
        visualizationPanel.removeAll();
    }
  
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SplashScreen splashScreen = new SplashScreen(3000); // Duration: 3 seconds
        splashScreen.showSplash();
        
        SwingUtilities.invokeLater(() -> {
        	MainWindow app = new MainWindow();
            app.setVisible(true);
        });
    }
}