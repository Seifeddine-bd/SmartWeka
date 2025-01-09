
package main.utils;

import javax.swing.*;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import weka.gui.hierarchyvisualizer.HierarchyVisualizer;
import weka.gui.treevisualizer.NodePlace;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import main.algorithms.clustering.CAHClusterer;

public class ResultVisualizer {
	 private JPanel panel;

	    public ResultVisualizer() {
	        panel = new JPanel();
	        panel.setLayout(new BorderLayout());
	    }

	    public JPanel getPanel() {
	        return panel;
	    }

	    

	    
	 // Method to visualize decision tree using Weka's J48 classifier
	    public void visualizeDecisionTree(Instances data) {
	        try {
	            panel.removeAll(); // Clear previous content

	            // Create and train the J48 classifier
	            J48 cls = new J48();
	            cls.buildClassifier(data); // Build the classifier

	            // Create the tree visualizer
	            TreeVisualizer treeVisualizer = new TreeVisualizer(null, cls.graph(), new PlaceNode2());
	            panel.add(treeVisualizer, BorderLayout.CENTER);

	            treeVisualizer.fitToScreen();
	            // Add controls for zooming and fitting
	            JPanel controls = new JPanel();
	            JButton zoomIn = new JButton("+");
	            JButton zoomOut = new JButton("-");
	            JButton fit = new JButton("Fit");

	            zoomIn.addActionListener(e -> {
	                Dimension size = treeVisualizer.getSize();
	                treeVisualizer.setSize((int) (size.width * 1.2), (int) (size.height * 1.2));
	                treeVisualizer.revalidate();
	                treeVisualizer.repaint();
	            });

	            zoomOut.addActionListener(e -> {
	                Dimension size = treeVisualizer.getSize();
	                treeVisualizer.setSize((int) (size.width * 0.8), (int) (size.height * 0.8));
	                treeVisualizer.revalidate();
	                treeVisualizer.repaint();
	            });

	            fit.addActionListener(e -> treeVisualizer.fitToScreen());

	            controls.add(zoomIn);
	            controls.add(zoomOut);
	            controls.add(fit);
	            panel.add(controls, BorderLayout.SOUTH);

	            // Refresh panel
	            panel.revalidate();
	            panel.repaint();

	            treeVisualizer.fitToScreen(); // Fit the visualizer to the screen
	        } catch (Exception e) {
	            System.err.println("Error visualizing decision tree: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }

	    public static void visualizeCAHDendrogram(Instances data, CAHClusterer cahClusterer, JPanel targetPanel) {
	        try {
	            // Build the clusterer with the provided dataset
	            cahClusterer.buildClusterer(data);

	            // Retrieve the Newick tree
	            String newickTree = cahClusterer.getDendrogramNewick();
	            System.out.println("Newick Tree:\n" + newickTree);

	            // Create the HierarchyVisualizer and add it to the target panel
	            HierarchyVisualizer visualizer = new HierarchyVisualizer(newickTree);
	            targetPanel.add(visualizer, BorderLayout.CENTER);

	        } catch (Exception e) {
	            System.err.println("Error during dendrogram visualization: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }

	    public static void visualizeCAHDendrogram(Instances data, CAHClusterer cahClusterer) {
	        try {
	            // Step 1: Build the clusterer with the provided dataset
	            cahClusterer.buildClusterer(data);

	            // Step 2: Retrieve the Newick tree
	            String newickTree = cahClusterer.getDendrogramNewick();
	            System.out.println("Newick Tree:\n" + newickTree);

	            // Step 3: Visualize the dendrogram using HierarchyVisualizer
	           // JFrame frame = new JFrame("CAH Dendrogram Visualization");
	            HierarchyVisualizer visualizer = new HierarchyVisualizer(newickTree);

	
	        } catch (Exception e) {
	            System.err.println("Error during dendrogram visualization: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	    
	    


	    private void addControlButtons(JPanel controls, TreeVisualizer visualizer) {
	        JButton zoomIn = new JButton("+");
	        JButton zoomOut = new JButton("-");
	        JButton fit = new JButton("Fit");
	        
	        zoomIn.addActionListener(e -> {
	            Dimension size = visualizer.getSize();
	            visualizer.setSize((int)(size.width * 1.2), (int)(size.height * 1.2));
	            visualizer.revalidate();
	            visualizer.repaint();
	        });
	        
	        zoomOut.addActionListener(e -> {
	            Dimension size = visualizer.getSize();
	            visualizer.setSize((int)(size.width * 0.8), (int)(size.height * 0.8));
	            visualizer.revalidate();
	            visualizer.repaint();
	        });
	        
	        fit.addActionListener(e -> visualizer.fitToScreen());
	        
	        controls.add(zoomIn);
	        controls.add(zoomOut);
	        controls.add(fit);
	    } 
	    
	    

}
