package UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SplashScreen extends JWindow {
    
    private static final int LOADING_STEP_DELAY_MS = 500;
    private static final int FINAL_DISPLAY_DELAY_MS = 600;
    private static final int SMOOTH_UPDATE_DELAY_MS = 20; // Update every 20ms for smooth animation
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer timer;
    private Timer smoothTimer;
    private int progress = 0;
    private int targetProgress = 0;
    private String[] loadingSteps = {
        "Initializing CS.410 Graph System...",
        "Loading automata engines...",
        "Preparing DFA/NFA components...",
        "Setting up PDA simulator...",
        "Initializing Turing Machine...",
        "Loading CFG parser...",
        "Setting up GraphViz integration...",
        "Preparing workspace...",
        "Finalizing components...",
        "Ready to launch!"
    };
    
    public SplashScreen() {
        initComponents();
        setLocationRelativeTo(null);
        setVisible(true);
        startLoading();
    }
    
    private String getVersionFromManifest() {
        String version = getClass().getPackage().getImplementationVersion();
        return version != null ? version : "1.0.0"; // Fallback version for development
    }
    
    private void initComponents() {
        setSize(500, 350);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(43, 43, 43)); // Dark theme like IntelliJ
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 1));
        
        // Header panel with app info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(43, 43, 43));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // App title
        JLabel titleLabel = new JLabel("CS.410 Graph System", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // Version
        JLabel versionLabel = new JLabel("Version " + getVersionFromManifest(), JLabel.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(187, 187, 187));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(versionLabel, BorderLayout.SOUTH);
        
        // Contributors panel
        JPanel contributorsPanel = new JPanel();
        contributorsPanel.setLayout(new BoxLayout(contributorsPanel, BoxLayout.Y_AXIS));
        contributorsPanel.setBackground(new Color(43, 43, 43));
        contributorsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));
        
        JLabel contributorsTitle = new JLabel("Development Team:", JLabel.CENTER);
        contributorsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contributorsTitle.setForeground(new Color(187, 187, 187));
        contributorsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contributorsPanel.add(contributorsTitle);
        
        contributorsPanel.add(Box.createVerticalStrut(8));
        
        // Contributors in a more compact format
        String[] contributors = {
            "Ege Yenen • Bora Baran • Berre Delikara",
            "Eren Yemşen • Berra Eğcin • Hakan Akbıyık",
            "Hakan Çildaş • Selim Özyılmaz • Olcay Taner Yıldız"
        };
        
        for (String line : contributors) {
            JLabel contribLabel = new JLabel(line, JLabel.CENTER);
            contribLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            contribLabel.setForeground(new Color(150, 150, 150));
            contribLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contributorsPanel.add(contribLabel);
        }
        
        // Progress panel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(new Color(43, 43, 43));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Status label
        statusLabel = new JLabel("Starting application...", JLabel.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(187, 187, 187));
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(60, 60, 60));
        progressBar.setForeground(new Color(75, 110, 175)); // IntelliJ blue
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(460, 8));
        
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        
        // Copyright panel
        JPanel copyrightPanel = new JPanel();
        copyrightPanel.setBackground(new Color(43, 43, 43));
        copyrightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        JLabel copyrightLabel = new JLabel("© 2024 CS.410 Graph System", JLabel.CENTER);
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        copyrightLabel.setForeground(new Color(120, 120, 120));
        copyrightPanel.add(copyrightLabel);
        
        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contributorsPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(43, 43, 43));
        bottomPanel.add(progressPanel, BorderLayout.NORTH);
        bottomPanel.add(copyrightPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void startLoading() {
        // Start smooth progress bar animation
        smoothTimer = new Timer(SMOOTH_UPDATE_DELAY_MS, e -> {
            if (progress < targetProgress) {
                progress = Math.min(progress + 1, targetProgress);
                progressBar.setValue(progress);
            }
        });
        smoothTimer.start();
        
        // Timer for loading steps
        timer = new Timer(LOADING_STEP_DELAY_MS, new ActionListener() {
            private int stepIndex = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (stepIndex < loadingSteps.length) {
                    statusLabel.setText(loadingSteps[stepIndex]);
                    targetProgress = (stepIndex * 100) / (loadingSteps.length - 1);
                    stepIndex++;
                } else {
                    targetProgress = 100;
                    timer.stop();
                    
                    // Wait for smooth animation to complete, then add final delay
                    Timer checkCompleteTimer = new Timer(SMOOTH_UPDATE_DELAY_MS, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (progress >= 100) {
                                ((Timer)e.getSource()).stop();
                                smoothTimer.stop();
                                
                                Timer finalTimer = new Timer(FINAL_DISPLAY_DELAY_MS, evt -> {
                                    ((Timer)evt.getSource()).stop();
                                    closeSplash();
                                });
                                finalTimer.setRepeats(false);
                                finalTimer.start();
                            }
                        }
                    });
                    checkCompleteTimer.start();
                }
            }
        });
        timer.start();
    }
    
    private void closeSplash() {
        setVisible(false);
        dispose();
        // Launch main application
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}