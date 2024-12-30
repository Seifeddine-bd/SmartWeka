package main.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class SplashScreen extends JWindow {
    private final int duration;

    public SplashScreen(int duration) {
        this.duration = duration; // Splash screen duration in milliseconds
        initializeUI();
    }

    private void initializeUI() {
        // Set size and location
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Add custom splash panel
        add(new SplashPanel());
    }

    public void showSplash() {
        // Display the splash screen
        setVisible(true);

        // Close the splash screen after the specified duration
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            setVisible(false);
            dispose();
        }
    }

    // Custom splash panel for visuals
    static class SplashPanel extends JPanel {
        public SplashPanel() {
            setBackground(new Color(240, 248, 255)); // Light blue tone
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing for smoother visuals
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw gradient background
            GradientPaint gradient = new GradientPaint(0, 0, new Color(173, 216, 230), getWidth(), getHeight(), Color.WHITE);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw abstract visuals (interconnected nodes and lines)
            g2d.setColor(new Color(100, 149, 237));
            int[][] points = {{150, 200}, {300, 100}, {450, 150}, {350, 300}, {200, 300}};
            for (int i = 0; i < points.length; i++) {
                for (int j = i + 1; j < points.length; j++) {
                    g2d.drawLine(points[i][0], points[i][1], points[j][0], points[j][1]);
                }
            }

            for (int[] point : points) {
                Ellipse2D.Double circle = new Ellipse2D.Double(point[0] - 10, point[1] - 10, 20, 20);
                g2d.fill(circle);
            }

            // Draw title
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            g2d.setColor(new Color(25, 25, 112)); // Modern dark blue
            g2d.drawString("SmartWeka", 220, 50);

            // Draw a tagline
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2d.setColor(new Color(70, 130, 180));
            g2d.drawString("Transform your data into intelligent insights user-friendly machine learning ", 40, 80);
        }
    }
}
