package main.utils;
/*
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import weka.core.Instances;
import java.util.ArrayList;
import java.util.List;

public class ResultVisualizer extends JPanel {
    private Instances data;
    private int[] assignments;
    private List<Point2D> centroids;
    private static final Color[] COLORS = {
        new Color(31, 119, 180),   // Bleu
        new Color(255, 127, 14),   // Orange
        new Color(44, 160, 44),    // Vert
        new Color(214, 39, 40),    // Rouge
        new Color(148, 103, 189),  // Violet
        new Color(140, 86, 75),    // Marron
        new Color(227, 119, 194),  // Rose
        new Color(127, 127, 127)   // Gris
    };
    
    public ResultVisualizer(Instances data, int[] assignments) {
        this.data = data;
        this.assignments = assignments;
        this.centroids = new ArrayList<>();
        setPreferredSize(new Dimension(600, 400));
    }
    
    public void setCentroids(List<Point2D> centroids) {
        this.centroids = centroids;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculer les limites des données
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        for (int i = 0; i < data.numInstances(); i++) {
            double x = data.instance(i).value(0);
            double y = data.instance(i).value(1);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        
        // Ajouter une marge
        double marginX = (maxX - minX) * 0.1;
        double marginY = (maxY - minY) * 0.1;
        minX -= marginX;
        maxX += marginX;
        minY -= marginY;
        maxY += marginY;
        
        // Fonction de mise à l'échelle
        double scaleX = getWidth() / (maxX - minX);
        double scaleY = getHeight() / (maxY - minY);
        
        // Dessiner les points
        int pointSize = 10;
        for (int i = 0; i < data.numInstances(); i++) {
            double x = data.instance(i).value(0);
            double y = data.instance(i).value(1);
            
            int screenX = (int) ((x - minX) * scaleX);
            int screenY = (int) (getHeight() - (y - minY) * scaleY);
            
            g2d.setColor(COLORS[assignments[i] % COLORS.length]);
            g2d.fillOval(screenX - pointSize/2, screenY - pointSize/2, pointSize, pointSize);
        }
        
        // Dessiner les centroïdes
        if (!centroids.isEmpty()) {
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i < centroids.size(); i++) {
                Point2D centroid = centroids.get(i);
                int screenX = (int) ((centroid.getX() - minX) * scaleX);
                int screenY = (int) (getHeight() - (centroid.getY() - minY) * scaleY);
                
                g2d.setColor(Color.BLACK);
                g2d.drawRect(screenX - 5, screenY - 5, 10, 10);
                g2d.setColor(COLORS[i % COLORS.length]);
                g2d.fillRect(screenX - 4, screenY - 4, 8, 8);
            }
        }
    }
}*/


import javax.swing.*;

import main.algorithms.clustering.CAHClusterer;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Instances;
import weka.gui.treevisualizer.NodePlace;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ResultVisualizer {

    private JPanel panel;

    // Constructor
    public ResultVisualizer() {
        panel = new JPanel(); // Initialize the JPanel
        panel.setLayout(new BorderLayout());
    }

    public JPanel getPanel() {
        return panel; // Provide a method to access the panel
    }

    public void visualizeDendrogram(CAHClusterer clusterer) throws Exception {
        // Assuming CAHClusterer has a method to get the cluster assignments or dendrogram representation
        String newickFormat = clusterer.getDendrogramNewick(); // Replace with the correct method if available

        // Save the Newick format to a file (optional)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("dendrogram.nwk"))) {
            writer.write(newickFormat);
            System.out.println("Dendrogram saved as 'dendrogram.nwk'");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display the Newick format
        System.out.println("Dendrogram (Newick format):\n" + newickFormat);
        
        // Optionally, visualize with a specialized library if you have one
        // Example: Some libraries like JTreeViewer can be used to visualize Newick trees
    }

    public void visualizeDecisionTree(String treeString) {
        JFrame frame = new JFrame("Decision Tree Visualization");
        TreeVisualizer treeVisualizer = new TreeVisualizer(null, treeString, new NodePlace(800, 600));

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(treeVisualizer);
        frame.pack();
        frame.setVisible(true);
        
        treeVisualizer.fitToScreen();
    }
    
    public void visualize2DScatterPlot(Instances data) {
        VisualizePanel vp = new VisualizePanel();
        vp.setName("Scatter Plot");
        
        PlotData2D plotData = new PlotData2D(data);
        plotData.setPlotName("2D Scatter Plot");
        
        try {
            vp.addPlot(plotData);
            panel.add(vp); // Add VisualizePanel to the main panel
            panel.revalidate(); // Refresh the panel
            panel.repaint(); // Repaint the panel
            
            JFrame frame = new JFrame("2D Scatter Plot");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setVisible(true);
            vp.fitToScreen();
        } catch (Exception e) {
            System.err.println("Error visualizing 2D scatter plot: " + e.getMessage());
        }
    }
}
