package UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.Timer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import common.Automaton;
import common.TestRunner;

/**
 * Abstract base class for all automaton panels to eliminate code duplication.
 * Contains all common UI components and functionality.
 */
public abstract class AbstractAutomatonPanel extends JPanel implements AutomatonPanel {

    protected JPanel textEditorPanel, graphPanel, topPanel;
    protected JTextArea textArea;
    protected JScrollPane scrollPane;
    protected JTextArea warningField;
    protected File file;
    protected MainPanel mainPanel;
    protected Automaton automaton;
    
    // Graph visualization caching and resizing
    private String cachedDotCode;
    private ImageIcon originalImage;
    private Timer resizeTimer;
    private static final int RESIZE_DELAY = 300; // milliseconds

    /**
     * Constructor that sets up the common UI layout
     */
    public AbstractAutomatonPanel(MainPanel mainPanel, Automaton automaton) {
        this.mainPanel = mainPanel;
        this.automaton = automaton;
        
        initializePanel();
        createTopPanel();
        createTextEditorPanel();
        createGraphPanel();
        createWarningPanel();
        assembleLayout();
    }

    /**
     * Abstract method to get the tab label text for this automaton type
     */
    protected abstract String getTabLabelText();

    /**
     * Initialize the main panel properties
     */
    private void initializePanel() {
        this.setLayout(new BorderLayout());
        this.setSize(600, 400);
    }

    /**
     * Creates the top panel with tab label and test button.
     */
    private void createTopPanel() {
        topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JLabel tabLabel = new JLabel(getTabLabelText());
        tabLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tabLabel.setForeground(new Color(102, 133, 102));
        
        JButton testButton = new JButton("Test");
        testButton.setPreferredSize(new Dimension(80, 30));
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runTests();
            }
        });
        
        topPanel.add(tabLabel, BorderLayout.WEST);
        topPanel.add(testButton, BorderLayout.EAST);
    }

    /**
     * Create the text editor panel with line numbers
     */
    private void createTextEditorPanel() {
        textEditorPanel = new JPanel();
        textEditorPanel.setLayout(new BorderLayout());
        textEditorPanel.setPreferredSize(new Dimension(300, 300));
        textEditorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        textArea = new JTextArea();
        scrollPane = new JScrollPane(textArea);

        TextLineNumber lineNumbering = new TextLineNumber(textArea);
        scrollPane.setRowHeaderView(lineNumbering);

        textEditorPanel.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Create the graph visualization panel
     */
    private void createGraphPanel() {
        graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        // Remove fixed size to allow dynamic resizing
        graphPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Graph Visualization"),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        graphPanel.setBackground(Color.WHITE);
        
        // Add component listener for dynamic resizing
        graphPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleGraphPanelResize();
            }
        });
        
        // Initialize resize timer
        resizeTimer = new Timer(RESIZE_DELAY, e -> regenerateGraphForCurrentSize());
        resizeTimer.setRepeats(false);
    }

    /**
     * Create the warning/messages panel
     */
    private void createWarningPanel() {
        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BorderLayout());
        warningPanel.setPreferredSize(new Dimension(300, 100));
        warningPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel warningLabel = new JLabel("Warnings and Messages:");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        warningField = new JTextArea("Warnings will be displayed here after using Compile or Run from the main Actions menu");
        warningField.setEditable(false);
        warningField.setBackground(new Color(255, 255, 255));
        warningField.setLineWrap(true);
        warningField.setWrapStyleWord(true);
        warningField.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JScrollPane warningScrollPane = new JScrollPane(warningField);
        warningScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        warningScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        warningPanel.add(warningLabel, BorderLayout.NORTH);
        warningPanel.add(warningScrollPane, BorderLayout.CENTER);
        
        this.add(warningPanel, BorderLayout.EAST);
    }

    /**
     * Assemble the final layout
     */
    private void assembleLayout() {
        this.add(textEditorPanel, BorderLayout.WEST);
        this.add(graphPanel, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);
    }

    // AutomatonPanel interface implementations
    @Override
    public void runAutomaton() {
        String inputText = textArea.getText();
        JLabel imageLabel = automaton.toGraphviz(inputText);
        
        if (imageLabel != null) {
            // Cache the original image and DOT code for resizing
            originalImage = (ImageIcon) imageLabel.getIcon();
            cachedDotCode = generateDotCodeForInput(inputText);
            
            // Use the new scaling method to fit current panel size
            int availableWidth = Math.max(graphPanel.getWidth() - 60, 500);
            int availableHeight = Math.max(graphPanel.getHeight() - 80, 400);
            
            ImageIcon scaledImage = scaleImageToFit(originalImage, availableWidth, availableHeight);
            updateGraphPanelWithImage(scaledImage);
        } else {
            // Clear cached data if generation failed
            originalImage = null;
            cachedDotCode = null;
            
            // Show error message if image generation failed
            JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>" +
                "<h3>Graph generation failed</h3>" +
                "<p>Check the warnings panel for details</p>" +
                "</body></html>");
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            errorLabel.setVerticalAlignment(JLabel.CENTER);
            errorLabel.setForeground(new Color(150, 50, 50));
            
            graphPanel.removeAll();
            graphPanel.add(errorLabel, BorderLayout.CENTER);
            graphPanel.revalidate();
            graphPanel.repaint();
        }
        
        updateWarningDisplay();
    }
    
    @Override
    public void compileAutomaton() {
        updateWarningDisplay();
    }

    /**
     * Run tests for the current automaton using a corresponding test file
     */
    protected void runTests() {
        // First compile/parse the current automaton
        String inputText = textArea.getText();
        automaton.setInputText(inputText);
        
        Automaton.ParseResult parseResult = automaton.parse(inputText);
        if (!parseResult.isSuccess()) {
            JOptionPane.showMessageDialog(this, 
                "Cannot run tests: Automaton has parsing errors. Check warnings panel.", 
                "Test Error", JOptionPane.ERROR_MESSAGE);
            updateWarningDisplay();
            return;
        }
        
        // Look for test file
        String testFilePath = findTestFile();
        if (testFilePath == null) {
            String message = "No test file found. Expected a .test file with the same name as your automaton file.\n\n";
            
            if (file != null) {
                String expectedTestFile = file.getName().replaceFirst("\\.[^.]*$", ".test");
                message += "Current file: " + file.getName() + "\n";
                message += "Expected test file: " + expectedTestFile + "\n\n";
            } else {
                message += "Please save your automaton file first, then try testing.\n\n";
            }
            
            message += "Example: if your file is q1.nfa, create q1.test with test cases.";
            
            JOptionPane.showMessageDialog(this, message, 
                "Test File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Run tests
        try {
            Automaton testAutomaton = parseResult.getAutomaton();
            TestRunner.TestResult result = TestRunner.runTests(testAutomaton, testFilePath);
            
            // Display results
            showTestResults(result);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error running tests: " + e.getMessage(), 
                "Test Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Find the corresponding test file for the current automaton
     */
    private String findTestFile() {
        if (file != null) {
            // Get the base name without extension
            String fileName = file.getName();
            String baseName;
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                baseName = fileName.substring(0, lastDot);
            } else {
                baseName = fileName;
            }
            
            // Look for .test file in the same directory
            File testFile = new File(file.getParent(), baseName + ".test");
            if (testFile.exists()) {
                return testFile.getAbsolutePath();
            }
        }
        
        return null;
    }

    /**
     * Display test results in a dialog
     */
    private void showTestResults(TestRunner.TestResult result) {
        StringBuilder message = new StringBuilder();
        message.append("Test Results:\n");
        message.append(String.format("Passed: %d/%d tests\n\n", 
                                    result.getPassedTests(), result.getTotalTests()));
        
        // Show detailed results
        for (TestRunner.TestCaseResult testResult : result.getDetailedResults()) {
            message.append(testResult.toString()).append("\n");
        }
        
        // Show failures if any
        if (!result.getFailures().isEmpty()) {
            message.append("\nFailure Details:\n");
            for (String failure : result.getFailures()) {
                message.append("• ").append(failure).append("\n");
            }
        }
        
        // Determine dialog type based on results
        int messageType;
        String title;
        if (result.getFailedTests() == 0) {
            messageType = JOptionPane.INFORMATION_MESSAGE;
            title = "All Tests Passed!";
        } else if (result.getPassedTests() > 0) {
            messageType = JOptionPane.WARNING_MESSAGE;
            title = "Some Tests Failed";
        } else {
            messageType = JOptionPane.ERROR_MESSAGE;
            title = "All Tests Failed";
        }
        
        // Create scrollable text area for long results
        JTextArea resultArea = new JTextArea(message.toString());
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, resultScrollPane, title, messageType);
    }
    
    @Override
    public void saveAutomaton() {
        File savedFile = mainPanel.fileManager.showSaveDialog(automaton, file != null ? file.getName() : null);
        if (savedFile != null) {
            saveFileContent(savedFile);
            file = savedFile;
            mainPanel.fileManager.addToRecentFiles(file);
            
            mainPanel.markCurrentTabAsSaved();
        }
    }
    
    @Override
    public String getTextAreaContent() {
        return textArea.getText();
    }
    
    @Override
    public Automaton getAutomaton() {
        return automaton;
    }
    
    @Override
    public File getCurrentFile() {
        return file;
    }
    
    @Override
    public void setCurrentFile(File file) {
        this.file = file;
    }
    
    @Override
    public void addTextChangeListener(Runnable onChange) {
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onChange.run();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onChange.run();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                onChange.run();
            }
        });
    }

    /**
     * Generate DOT code for the given input text
     */
    private String generateDotCodeForInput(String inputText) {
        try {
            automaton.setInputText(inputText);
            return automaton.toDotCode(inputText);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Updates the warning display with current validation messages
     */
    protected void updateWarningDisplay() {
        String inputText = textArea.getText();
        automaton.setInputText(inputText);
        
        List<Automaton.ValidationMessage> messages = automaton.validate();
        String warningText;
        if (messages.isEmpty()) {
            warningText = "No warnings or errors found!";
        } else {
            StringBuilder result = new StringBuilder();
            for (Automaton.ValidationMessage msg : messages) {
                result.append(msg.toString()).append("\n");
            }
            warningText = result.toString();
        }
        
        warningField.setText(warningText);
        warningField.setCaretPosition(0); 
    }

    /**
     * Saves the current content to a file
     */
    protected void saveFileContent(File file) {
        String text = textArea.getText();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
            JOptionPane.showMessageDialog(this, "File saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    /**
     * Loads content from a file into the text area
     */
    public void loadFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            textArea.read(reader, null);
            // Set the current file so test discovery works
            this.file = file;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }
    
    /**
     * Handle resize events with debouncing to prevent excessive regeneration
     */
    private void handleGraphPanelResize() {
        if (resizeTimer != null) {
            resizeTimer.restart();
        }
    }
    
    /**
     * Regenerate the graph visualization for the current panel size
     */
    private void regenerateGraphForCurrentSize() {
        if (originalImage != null && cachedDotCode != null && graphPanel.getWidth() > 0 && graphPanel.getHeight() > 0) {
            // Calculate available space (subtract border space)
            int availableWidth = graphPanel.getWidth() - 60; // account for borders and padding
            int availableHeight = graphPanel.getHeight() - 80; // account for title border and padding
            
            if (availableWidth > 50 && availableHeight > 50) {
                ImageIcon scaledImage = scaleImageToFit(originalImage, availableWidth, availableHeight);
                updateGraphPanelWithImage(scaledImage);
            }
        }
    }
    
    /**
     * Scale an image to fit within the given dimensions while maintaining aspect ratio
     */
    private ImageIcon scaleImageToFit(ImageIcon originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getIconWidth();
        int originalHeight = originalImage.getIconHeight();
        
        // Calculate scaling factors
        double widthScale = (double) maxWidth / originalWidth;
        double heightScale = (double) maxHeight / originalHeight;
        double scale = Math.min(widthScale, heightScale); // Maintain aspect ratio
        
        // For small graphs, allow upscaling up to a reasonable limit to better fill space
        // For large graphs, scale down as needed
        if (scale > 1.0) {
            // Upscaling: limit to maximum 3x for readability, but allow significant enlargement
            scale = Math.min(scale, 3.0);
        }
        
        // Apply minimum scale to maintain readability (but allow more aggressive scaling)
        scale = Math.max(scale, 0.05);
        
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        Image scaledImage = originalImage.getImage().getScaledInstance(
            scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        
        return new ImageIcon(scaledImage);
    }
    
    /**
     * Update the graph panel with a new image
     */
    private void updateGraphPanelWithImage(ImageIcon imageIcon) {
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        
        // Create a wrapper panel to center the image
        JPanel imageWrapper = new JPanel(new BorderLayout());
        imageWrapper.setBackground(Color.WHITE);
        imageWrapper.add(imageLabel, BorderLayout.CENTER);
        
        graphPanel.removeAll();
        graphPanel.add(imageWrapper, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }
}