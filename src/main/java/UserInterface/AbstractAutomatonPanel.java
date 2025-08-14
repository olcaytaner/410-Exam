package UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import common.Automaton;

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
     * Create the top panel with tab label
     */
    private void createTopPanel() {
        topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JLabel tabLabel = new JLabel(getTabLabelText());
        tabLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tabLabel.setForeground(new Color(102, 133, 102));
        
        topPanel.add(tabLabel, BorderLayout.WEST);
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
        graphPanel.setPreferredSize(new Dimension(600, 500));
        graphPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Graph Visualization"),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        graphPanel.setBackground(Color.WHITE);
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
            // Center the image label
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
            
            // Create a wrapper panel to center the image
            JPanel imageWrapper = new JPanel(new BorderLayout());
            imageWrapper.setBackground(Color.WHITE);
            imageWrapper.add(imageLabel, BorderLayout.CENTER);
            
            graphPanel.removeAll();
            graphPanel.add(imageWrapper, BorderLayout.CENTER);
        } else {
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
        }
        
        graphPanel.revalidate();
        graphPanel.repaint();
        updateWarningDisplay();
    }
    
    @Override
    public void compileAutomaton() {
        updateWarningDisplay();
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
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }
}