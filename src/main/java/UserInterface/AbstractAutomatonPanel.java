package UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.Timer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;

import common.Automaton;
import common.TestRunner;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * Abstract base class for all automaton panels to eliminate code duplication.
 * Contains all common UI components and functionality.
 */
public abstract class AbstractAutomatonPanel extends JPanel implements AutomatonPanel {

    // Configuration flag for inline testing feature
    private static final boolean ENABLE_INLINE_TESTING = true; // Set to false for production
    
    protected JPanel textEditorPanel, graphPanel, topPanel;
    protected JTextArea textArea;
    protected JScrollPane scrollPane;
    protected JTextArea warningField;
    protected File file;
    protected MainPanel mainPanel;
    protected Automaton automaton;
    protected UndoManager undoManager;
    
    // Inline testing components
    protected JPanel inlineTestPanel;
    protected JTextField inlineTestInput;
    protected JLabel inlineTestResult;
    protected JButton inlineTestButton;
    
    // Graph visualization caching and resizing
    private String cachedDotCode;
    private Timer resizeTimer;
    private static final int RESIZE_DELAY = 300; // milliseconds
    private String svgText;
    
    // Loading indicator components
    private JPanel loadingPanel;
    private JLabel loadingSpinner;
    private JLabel loadingText;
    private Timer spinnerTimer;
    private int spinnerAngle = 0;
    
    /**
     * Result class to pass data from background thread to UI thread
     */
    private static class GraphGenerationResult {
        public final Automaton.ParseResult parseResult;
        public final JLabel imageLabel;
        public final String inputText;
        
        public GraphGenerationResult(Automaton.ParseResult parseResult, JLabel imageLabel, String inputText) {
            this.parseResult = parseResult;
            this.imageLabel = imageLabel;
            this.inputText = inputText;
        }
    }

    /**
     * Constructor that sets up the common UI layout
     */
    public AbstractAutomatonPanel(MainPanel mainPanel, Automaton automaton) {
        this.mainPanel = mainPanel;
        this.automaton = automaton;
        
        initializePanel();
        createTopPanel();
        if (ENABLE_INLINE_TESTING) {
            createInlineTestPanel();
        }
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
        
        JButton runButton = new JButton("Run");
        runButton.setPreferredSize(new Dimension(80, 30));
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runTestFile();
            }
        });
        
        topPanel.add(tabLabel, BorderLayout.WEST);
        topPanel.add(runButton, BorderLayout.EAST);
    }

    /**
     * Create the inline test panel for quick testing
     */
    private void createInlineTestPanel() {
        inlineTestPanel = new JPanel();
        inlineTestPanel.setLayout(new BoxLayout(inlineTestPanel, BoxLayout.X_AXIS));
        inlineTestPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        inlineTestPanel.setBackground(new Color(245, 245, 245));
        
        // Create components
        JLabel testLabel = new JLabel("Quick Test: ");
        testLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        inlineTestInput = new JTextField();
        inlineTestInput.setMaximumSize(new Dimension(300, 30));
        inlineTestInput.setPreferredSize(new Dimension(200, 30));
        inlineTestInput.setToolTipText("Enter input string to test (empty for epsilon)");
        
        inlineTestButton = new JButton("Test");
        inlineTestButton.setPreferredSize(new Dimension(70, 30));
        
        inlineTestResult = new JLabel("");
        inlineTestResult.setFont(new Font("Arial", Font.BOLD, 12));
        inlineTestResult.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        // Add action listener for test button
        inlineTestButton.addActionListener(e -> runInlineTest());
        
        // Add enter key listener for input field
        inlineTestInput.addActionListener(e -> runInlineTest());
        
        // Assemble the panel
        inlineTestPanel.add(testLabel);
        inlineTestPanel.add(Box.createHorizontalStrut(10));
        inlineTestPanel.add(inlineTestInput);
        inlineTestPanel.add(Box.createHorizontalStrut(10));
        inlineTestPanel.add(inlineTestButton);
        inlineTestPanel.add(Box.createHorizontalStrut(15));
        inlineTestPanel.add(inlineTestResult);
        inlineTestPanel.add(Box.createHorizontalGlue());
    }
    
    /**
     * Run inline test for the entered input
     */
    private void runInlineTest() {
        String input = inlineTestInput.getText();
        String automatonText = textArea.getText();
        
        // Parse the automaton
        Automaton.ParseResult parseResult = automaton.parse(automatonText);
        
        if (!parseResult.isSuccess()) {
            inlineTestResult.setText("⚠ Parse Error");
            inlineTestResult.setForeground(new Color(200, 100, 0));
            updateWarningDisplayWithParseResult(parseResult, automatonText);
            return;
        }
        
        // Execute the test
        try {
            Automaton parsedAutomaton = parseResult.getAutomaton();
            Automaton.ExecutionResult execResult = parsedAutomaton.execute(input);
            
            boolean accepted = execResult.isAccepted();
            String displayInput = input.isEmpty() ? "ε" : "\"" + input + "\"";
            
            if (accepted) {
                inlineTestResult.setText("✓ " + displayInput + " → ACCEPT");
                inlineTestResult.setForeground(new Color(0, 150, 0));
            } else {
                inlineTestResult.setText("✗ " + displayInput + " → REJECT");
                inlineTestResult.setForeground(new Color(200, 0, 0));
            }
            
            // Update warning field with execution trace
            String traceInfo = "Quick Test Result:\n";
            traceInfo += "Input: " + displayInput + "\n";
            traceInfo += "Result: " + (accepted ? "ACCEPTED" : "REJECTED") + "\n";
            if (execResult.getTrace() != null && !execResult.getTrace().isEmpty()) {
                traceInfo += "\nExecution Trace:\n" + execResult.getTrace();
            }
            warningField.setText(traceInfo);
            warningField.setCaretPosition(0);
            
        } catch (Exception e) {
            inlineTestResult.setText("⚠ Execution Error");
            inlineTestResult.setForeground(new Color(200, 100, 0));
            warningField.setText("Execution Error: " + e.getMessage());
        }
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

        // Set up undo/redo functionality
        setupUndoRedo();
    }

    /**
     * Set up undo/redo functionality for the text area
     */
    private void setupUndoRedo() {
        // Create and configure UndoManager
        undoManager = new UndoManager();
        undoManager.setLimit(1000); // Limit undo history to prevent memory issues

        // Add UndoManager directly as the listener (it implements UndoableEditListener)
        textArea.getDocument().addUndoableEditListener(undoManager);

        // Get the platform-specific menu shortcut key mask (Command on Mac, Control on Windows/Linux)
        int menuShortcutKeyMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // Define unique action key names to avoid conflicts with parent component action maps
        final String UNDO_ACTION_KEY = "cs410-text-undo";
        final String REDO_ACTION_KEY = "cs410-text-redo";
        final String REDO_ALT_ACTION_KEY = "cs410-text-redo-alt";

        // Get both WHEN_FOCUSED and WHEN_ANCESTOR_OF_FOCUSED_COMPONENT input maps
        // We register in both to handle all focus scenarios (direct focus and wrapped in JScrollPane)
        InputMap inputMapFocused = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        InputMap inputMapAncestor = textArea.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = textArea.getActionMap();

        // Remove any existing conflicting actions from ActionMap
        actionMap.remove("undo");
        actionMap.remove("redo");
        actionMap.remove("redo-alt");

        // Create KeyStroke objects (reuse for consistency)
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, menuShortcutKeyMask);
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, menuShortcutKeyMask);
        KeyStroke redoAltKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
            menuShortcutKeyMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);

        // Remove any existing bindings for these keystrokes in both InputMaps
        inputMapFocused.remove(undoKeyStroke);
        inputMapFocused.remove(redoKeyStroke);
        inputMapFocused.remove(redoAltKeyStroke);
        inputMapAncestor.remove(undoKeyStroke);
        inputMapAncestor.remove(redoKeyStroke);
        inputMapAncestor.remove(redoAltKeyStroke);

        // Install new bindings in BOTH InputMaps for redundancy
        // WHEN_FOCUSED - when textArea has direct focus
        inputMapFocused.put(undoKeyStroke, UNDO_ACTION_KEY);
        inputMapFocused.put(redoKeyStroke, REDO_ACTION_KEY);
        inputMapFocused.put(redoAltKeyStroke, REDO_ALT_ACTION_KEY);

        // WHEN_ANCESTOR_OF_FOCUSED_COMPONENT - when wrapped in JScrollPane
        inputMapAncestor.put(undoKeyStroke, UNDO_ACTION_KEY);
        inputMapAncestor.put(redoKeyStroke, REDO_ACTION_KEY);
        inputMapAncestor.put(redoAltKeyStroke, REDO_ALT_ACTION_KEY);

        // Undo: Ctrl+Z (or Cmd+Z on Mac)
        actionMap.put(UNDO_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    try {
                        undoManager.undo();
                    } catch (Exception ex) {
                        // Silently handle undo errors to prevent disrupting user workflow
                        System.err.println("Error during undo: " + ex.getMessage());
                    }
                }
            }
        });

        // Redo: Ctrl+Y (or Cmd+Y on Mac)
        actionMap.put(REDO_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    try {
                        undoManager.redo();
                    } catch (Exception ex) {
                        // Silently handle redo errors to prevent disrupting user workflow
                        System.err.println("Error during redo: " + ex.getMessage());
                    }
                }
            }
        });

        // Alternative redo: Ctrl+Shift+Z (or Cmd+Shift+Z on Mac)
        actionMap.put(REDO_ALT_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    try {
                        undoManager.redo();
                    } catch (Exception ex) {
                        // Silently handle redo errors to prevent disrupting user workflow
                        System.err.println("Error during redo: " + ex.getMessage());
                    }
                }
            }
        });

        // Clear any initialization edits from the undo stack
        undoManager.discardAllEdits();
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
        resizeTimer = new Timer(RESIZE_DELAY, e -> {
            try {
                regenerateGraphForCurrentSize();
            } catch (Exception ex) {
                ex.printStackTrace();

                JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>" +
                        "<h3>Error Generating Figure</h3>" +
                        "<p>Error: " + ex.getMessage() + "</p>" +
                        "</body></html>");

                errorLabel.setHorizontalAlignment(JLabel.CENTER);
                errorLabel.setVerticalAlignment(JLabel.CENTER);
                errorLabel.setForeground(new Color(150, 50, 50));

                graphPanel.removeAll();
                graphPanel.add(errorLabel, BorderLayout.CENTER);
                graphPanel.revalidate();
                graphPanel.repaint();

                updateWarningDisplay();
            }
        });
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
        
        warningField = new JTextArea("Warnings will be displayed here after using 'Compile with Figure' or 'Run' from the main Actions menu");
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
        // Create a north panel that contains both top panel and inline test panel
        if (ENABLE_INLINE_TESTING && inlineTestPanel != null) {
            JPanel northContainer = new JPanel();
            northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
            northContainer.add(topPanel);
            northContainer.add(inlineTestPanel);
            this.add(northContainer, BorderLayout.NORTH);
        } else {
            this.add(topPanel, BorderLayout.NORTH);
        }
        
        this.add(textEditorPanel, BorderLayout.WEST);
        this.add(graphPanel, BorderLayout.CENTER);
    }

    // AutomatonPanel interface implementations
    @Override
    public void compileWithFigure() {
        final String inputText = textArea.getText();
        
        // Show loading indicator immediately
        showLoadingIndicator();
        
        // Create SwingWorker to handle parsing and GraphViz processing in background
        SwingWorker<GraphGenerationResult, Void> worker = new SwingWorker<GraphGenerationResult, Void>() {
            @Override
            protected GraphGenerationResult doInBackground() throws Exception {
                // This runs on background thread - First parse, then generate if successful
                Automaton.ParseResult parseResult = automaton.parse(inputText);
                
                JLabel imageLabel = null;
                if (parseResult.isSuccess()) {
                    // Only generate image if parsing succeeded
                    imageLabel = automaton.toGraphviz(inputText);
                }
                
                return new GraphGenerationResult(parseResult, imageLabel, inputText);
            }
            
            @Override
            protected void done() {
                // This runs on EDT when background work is complete
                hideLoadingIndicator();
                
                try {
                    GraphGenerationResult result = get(); // Get result from doInBackground()
                    
                    // Always update warnings first to show parsing errors
                    updateWarningDisplayWithParseResult(result.parseResult, result.inputText);
                    
                    if (result.parseResult.isSuccess()
                            && result.imageLabel != null && !result.imageLabel.getText().isEmpty()) {

                        // Parsing succeeded and we have a valid image
                        svgText = result.imageLabel.getText();
                        cachedDotCode = generateDotCodeForInput(result.inputText);
                        
                        // Use the new scaling method to fit current panel size
                        int availableWidth = Math.max(graphPanel.getWidth() - 60, 500);
                        int availableHeight = Math.max(graphPanel.getHeight() - 80, 400);

                        ImageIcon svgImage = svgStringToIcon(svgText, availableWidth, availableHeight);

                        updateGraphPanelWithImage(svgImage);
                    } else {
                        // Parsing failed or no image generated - clear cached data and show error
                        cachedDotCode = null;
                        
                        String errorMessage;
                        if (!result.parseResult.isSuccess()) {
                            errorMessage = "<h3>Parsing Failed</h3><p>Check the warnings panel for syntax errors</p>";
                        } else {
                            errorMessage = "<h3>Graph generation failed</h3><p>Check the warnings panel for details</p>";
                        }
                        
                        JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>" + errorMessage + "</body></html>");
                        errorLabel.setHorizontalAlignment(JLabel.CENTER);
                        errorLabel.setVerticalAlignment(JLabel.CENTER);
                        errorLabel.setForeground(new Color(150, 50, 50));
                        
                        graphPanel.removeAll();
                        graphPanel.add(errorLabel, BorderLayout.CENTER);
                        graphPanel.revalidate();
                        graphPanel.repaint();
                    }
                } catch (Exception e) {
                    // Handle any exceptions that occurred during processing
                    e.printStackTrace();
                    hideLoadingIndicator();
                    
                    // Clear cached data
                    cachedDotCode = null;
                    
                    // Show error message
                    JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>" +
                        "<h3>Unexpected Error</h3>" +
                        "<p>Error: " + e.getMessage() + "</p>" +
                        "</body></html>");
                    errorLabel.setHorizontalAlignment(JLabel.CENTER);
                    errorLabel.setVerticalAlignment(JLabel.CENTER);
                    errorLabel.setForeground(new Color(150, 50, 50));
                    
                    graphPanel.removeAll();
                    graphPanel.add(errorLabel, BorderLayout.CENTER);
                    graphPanel.revalidate();
                    graphPanel.repaint();
                    
                    // Update warnings to show the exception - fallback to basic validation
                    updateWarningDisplay();
                }
            }
        };
        
        // Start the background work
        worker.execute();
    }

    private ImageIcon svgStringToIcon(String svg, int width, int height) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
        TranscoderInput input = new TranscoderInput(bais);

        BufferedImageTranscoder t = new BufferedImageTranscoder();
        t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

        t.transcode(input, null);

        return new ImageIcon(t.getBufferedImage());
    }

    //To write svg output to memory
    static class BufferedImageTranscoder extends PNGTranscoder {
        BufferedImage image;

        @Override
        public void writeImage(BufferedImage image, TranscoderOutput output) {
            this.image = image;
        }

        public BufferedImage getBufferedImage() {
            return image;
        }
    }
    
    // Deprecated compileAutomaton - no longer needed as validation happens in compileWithFigure

    /**
     * Run tests for the current automaton using a corresponding test file
     */
    protected void runTestFile() {
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
        
        // Run tests in background
        runTestsInBackground(parseResult.getAutomaton(), testFilePath);
    }

    /**
     * Runs tests from the matching .test file (interface implementation)
     */
    @Override
    public void run() {
        runTestFile();
    }

    /**
     * Runs tests from a user-selected test file (interface implementation)
     */
    @Override
    public void runWithFile() {
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
        
        // Show file chooser for test file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Test File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Test Files (*.test)", "test"));
        
        // Set current directory to same as automaton file if available
        if (file != null && file.getParent() != null) {
            fileChooser.setCurrentDirectory(new File(file.getParent()));
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedTestFile = fileChooser.getSelectedFile();
            
            // Run tests with selected file in background
            runTestsInBackground(parseResult.getAutomaton(), selectedTestFile.getAbsolutePath());
        }
    }

    /**
     * Run tests in background with progress dialog
     */
    private void runTestsInBackground(Automaton testAutomaton, String testFilePath) {
        // Create progress dialog
        javax.swing.JDialog progressDialog = new javax.swing.JDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this), 
            "Running Tests...", 
            javax.swing.JDialog.ModalityType.APPLICATION_MODAL
        );
        
        javax.swing.JPanel progressPanel = new javax.swing.JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        javax.swing.JLabel statusLabel = new javax.swing.JLabel("Initializing...");
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false); // Remove text from progress bar itself
        progressBar.setAlignmentX(CENTER_ALIGNMENT);
        
        javax.swing.JLabel progressLabel = new javax.swing.JLabel("0% - Preparing tests...");
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        javax.swing.JLabel timeoutLabel = new javax.swing.JLabel("Total timeout: " + (TestRunner.DEFAULT_TIMEOUT_MS / 1000) + " seconds for all tests");
        timeoutLabel.setAlignmentX(CENTER_ALIGNMENT);
        timeoutLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        javax.swing.JButton cancelButton = new javax.swing.JButton("Cancel");
        cancelButton.setAlignmentX(CENTER_ALIGNMENT);
        
        progressPanel.add(statusLabel);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(5));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(timeoutLabel);
        progressPanel.add(Box.createVerticalStrut(15));
        progressPanel.add(cancelButton);
        
        progressDialog.setContentPane(progressPanel);
        progressDialog.setSize(350, 180);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        
        // Create SwingWorker for background execution
        SwingWorker<TestRunner.TestResult, TestRunner.TestProgress> worker = new SwingWorker<TestRunner.TestResult, TestRunner.TestProgress>() {
            @Override
            protected TestRunner.TestResult doInBackground() throws Exception {
                // Create progress callback that publishes updates
                TestRunner.TestProgressCallback progressCallback = new TestRunner.TestProgressCallback() {
                    @Override
                    public void onTestStarted(int currentTest, int totalTests, String input) {
                        publish(TestRunner.TestProgress.started(currentTest, totalTests, input));
                    }
                    
                    @Override
                    public void onTestCompleted(int currentTest, int totalTests, String input, boolean passed) {
                        publish(TestRunner.TestProgress.completed(currentTest, totalTests, input, passed));
                    }
                };
                
                // Run tests with timeout and progress callback
                return TestRunner.runTests(testAutomaton, testFilePath, TestRunner.DEFAULT_TIMEOUT_MS, progressCallback);
            }
            
            @Override
            protected void process(java.util.List<TestRunner.TestProgress> chunks) {
                // Update UI with progress information
                if (!chunks.isEmpty()) {
                    TestRunner.TestProgress latest = chunks.get(chunks.size() - 1);
                    
                    // Update progress bar
                    int percentage = latest.getProgressPercentage();
                    progressBar.setValue(percentage);
                    
                    // Update status and progress bar string
                    String inputDisplay = latest.getCurrentInput().isEmpty() ? "ε" : latest.getCurrentInput();
                    if (inputDisplay.length() > 20) {
                        inputDisplay = inputDisplay.substring(0, 17) + "...";
                    }
                    
                    if (latest.isCompleted()) {
                        String result = latest.isPassed() ? "✓" : "✗";
                        statusLabel.setText(String.format("Test %d/%d: %s %s", 
                            latest.getCurrentTest(), latest.getTotalTests(), inputDisplay, result));
                        progressLabel.setText(String.format("%d%% - Test %d of %d completed", 
                            percentage, latest.getCurrentTest(), latest.getTotalTests()));
                    } else {
                        statusLabel.setText(String.format("Running test %d/%d: %s", 
                            latest.getCurrentTest(), latest.getTotalTests(), inputDisplay));
                        progressLabel.setText(String.format("%d%% - Running test %d of %d", 
                            percentage, latest.getCurrentTest(), latest.getTotalTests()));
                    }
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    TestRunner.TestResult result = get();
                    showTestResults(result);
                } catch (InterruptedException e) {
                    // Test was cancelled
                    JOptionPane.showMessageDialog(AbstractAutomatonPanel.this, 
                        "Test execution was cancelled.", 
                        "Test Cancelled", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AbstractAutomatonPanel.this, 
                        "Error running tests: " + e.getMessage(), 
                        "Test Execution Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        // Handle cancel button
        cancelButton.addActionListener(e -> {
            worker.cancel(true);
            progressDialog.dispose();
        });
        
        // Start the worker
        worker.execute();
        
        // Show progress dialog (blocks until disposed)
        progressDialog.setVisible(true);
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
        
        // Add timeout warning if any tests timed out
        if (result.getTimeoutCount() > 0) {
            message.append("⚠️ WARNING: Test suite timed out after ")
                   .append(TestRunner.DEFAULT_TIMEOUT_MS / 1000)
                   .append(" seconds total.\n")
                   .append("This may indicate infinite loops or very long computations.\n\n");
        }
        
        // Use the new classification-based detailed report
        message.append(result.getDetailedReport());
        
        // Determine dialog type based on results
        int messageType;
        String title;
        if (result.getTimeoutCount() > 0) {
            messageType = JOptionPane.WARNING_MESSAGE;
            title = "Tests Completed with Timeouts";
        } else if (result.getFailedTests() == 0) {
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
        resultScrollPane.setPreferredSize(new Dimension(500, 350));
        
        JOptionPane.showMessageDialog(this, resultScrollPane, title, messageType);
    }
    
    @Override
    public void saveAutomaton() {
        if (file != null) {
            // File already exists, just save directly (quick save)
            saveFileContent(file);
            mainPanel.markCurrentTabAsSaved();
        } else {
            // No file associated, show save dialog (Save As)
            saveAsAutomaton();
        }
    }
    
    /**
     * Save As - always shows save dialog
     */
    @Override
    public void saveAsAutomaton() {
        File savedFile = mainPanel.fileManager.showSaveDialog(automaton, file != null ? file.getName() : null);
        if (savedFile != null) {
            saveFileContent(savedFile);
            
            // Update file reference
            file = savedFile;
            
            // Add to recent files
            mainPanel.fileManager.addToRecentFiles(file);
            
            // Update tab file association and title
            mainPanel.updateCurrentTabFile(savedFile);
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
     * Sets the initial content of the text area (used for templates)
     */
    public void setInitialContent(String content) {
        if (content != null && textArea != null) {
            textArea.setText(content);
            textArea.setCaretPosition(0);
        }
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
     * Update warning display for a successfully parsed automaton
     */
    protected void updateWarningDisplayForParsedAutomaton(String inputText) {
        // Parse the input text to get a fresh automaton instance
        Automaton.ParseResult parseResult = automaton.parse(inputText);
        
        StringBuilder result = new StringBuilder();
        
        // Always show parsing messages first (includes syntax errors)
        List<Automaton.ValidationMessage> parseMessages = parseResult.getValidationMessages();
        if (parseMessages != null && !parseMessages.isEmpty()) {
            for (Automaton.ValidationMessage msg : parseMessages) {
                result.append(msg.toString()).append("\n");
            }
        }
        
        if (parseResult.isSuccess() && parseResult.getAutomaton() != null) {
            // Use the parsed automaton for validation
            Automaton parsedAutomaton = parseResult.getAutomaton();
            parsedAutomaton.setInputText(inputText);
            
            List<Automaton.ValidationMessage> validationMessages = parsedAutomaton.validate();
            if (validationMessages != null && !validationMessages.isEmpty()) {
                for (Automaton.ValidationMessage msg : validationMessages) {
                    result.append(msg.toString()).append("\n");
                }
            }
        }
        
        String warningText;
        if (result.length() == 0) {
            warningText = "No warnings or errors found!";
        } else {
            warningText = result.toString();
        }
        
        warningField.setText(warningText);
        warningField.setCaretPosition(0);
    }
    
    /**
     * Update warning display using an existing parse result
     */
    protected void updateWarningDisplayWithParseResult(Automaton.ParseResult parseResult, String inputText) {
        StringBuilder result = new StringBuilder();
        
        // Always show parsing messages first (includes syntax errors)
        List<Automaton.ValidationMessage> parseMessages = parseResult.getValidationMessages();
        if (parseMessages != null && !parseMessages.isEmpty()) {
            for (Automaton.ValidationMessage msg : parseMessages) {
                result.append(msg.toString()).append("\n");
            }
        }
        
        if (parseResult.isSuccess() && parseResult.getAutomaton() != null) {
            // Use the parsed automaton for validation
            Automaton parsedAutomaton = parseResult.getAutomaton();
            parsedAutomaton.setInputText(inputText);
            
            List<Automaton.ValidationMessage> validationMessages = parsedAutomaton.validate();
            if (validationMessages != null && !validationMessages.isEmpty()) {
                for (Automaton.ValidationMessage msg : validationMessages) {
                    result.append(msg.toString()).append("\n");
                }
            }
        }
        
        String warningText;
        if (result.length() == 0) {
            warningText = "No warnings or errors found!";
        } else {
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
        try {
            // Read file content as string
            // Using setText() instead of read() to preserve the document and UndoManager connection
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()),
                                      java.nio.charset.StandardCharsets.UTF_8);
            textArea.setText(content);

            // Clear the setText edit from undo history (we don't want to undo file loading)
            if (undoManager != null) {
                undoManager.discardAllEdits();
            }

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
    private void regenerateGraphForCurrentSize() throws Exception {
        if (svgText != null && cachedDotCode != null && graphPanel.getWidth() > 0 && graphPanel.getHeight() > 0) {
            // Calculate available space (subtract border space)
            int availableWidth = graphPanel.getWidth() - 60; // account for borders and padding
            int availableHeight = graphPanel.getHeight() - 80; // account for title border and padding
            
            if (availableWidth > 50 && availableHeight > 50) {
                ImageIcon svgImage = svgStringToIcon(svgText, availableWidth, availableHeight);
                updateGraphPanelWithImage(svgImage);
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
    
    /**
     * Initialize the loading indicator components
     */
    private void initializeLoadingComponents() {
        // Create loading panel
        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        
        // Create spinner with custom painting
        loadingSpinner = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = 20;
                
                // Draw spinning dots
                for (int i = 0; i < 8; i++) {
                    double angle = (spinnerAngle + i * 45) * Math.PI / 180;
                    int x = centerX + (int) (radius * Math.cos(angle));
                    int y = centerY + (int) (radius * Math.sin(angle));
                    
                    int alpha = 255 - (i * 30);
                    if (alpha < 50) alpha = 50;
                    
                    g2d.setColor(new Color(0, 100, 200, alpha));
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                }
                g2d.dispose();
            }
        };
        loadingSpinner.setPreferredSize(new Dimension(80, 80));
        loadingSpinner.setHorizontalAlignment(JLabel.CENTER);
        
        // Create loading text
        loadingText = new JLabel("Generating visualization...");
        loadingText.setHorizontalAlignment(JLabel.CENTER);
        loadingText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        loadingText.setForeground(new Color(60, 60, 60));
        
        // Create spinner animation timer
        spinnerTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spinnerAngle = (spinnerAngle + 45) % 360;
                loadingSpinner.repaint();
            }
        });
        
        // Assemble loading panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(loadingSpinner);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(loadingText);
        centerPanel.add(Box.createVerticalGlue());
        
        loadingPanel.add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Show the loading indicator
     */
    private void showLoadingIndicator() {
        if (loadingPanel == null) {
            initializeLoadingComponents();
        }
        
        graphPanel.removeAll();
        graphPanel.add(loadingPanel, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
        
        spinnerTimer.start();
    }
    
    /**
     * Hide the loading indicator
     */
    private void hideLoadingIndicator() {
        if (spinnerTimer != null) {
            spinnerTimer.stop();
        }
    }
}