package UserInterface;

import common.Automaton;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.*;
import java.util.List;

public class NFAPanel extends JPanel {

    JPanel textEditorPanel, graphPanel, topPanel;
    JTextArea textArea;
    JScrollPane scrollPane;
    JTextArea warningField;
    JMenuBar menuBar;
    private File file;
    private MainPanel mainPanel;
    private Automaton automaton;

    public NFAPanel(MainPanel mainPanel, Automaton automaton) {
        this.mainPanel = mainPanel;
        this.automaton = automaton;
        this.setLayout(new BorderLayout());
        this.setSize(600, 400);

        createTopPanel();

        textEditorPanel = new JPanel();
        textEditorPanel.setLayout(new BorderLayout());
        textEditorPanel.setPreferredSize(new Dimension(300, 300));
        textEditorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        textArea = new JTextArea();
        scrollPane = new JScrollPane(textArea);

        TextLineNumber lineNumbering = new TextLineNumber(textArea);
        scrollPane.setRowHeaderView(lineNumbering);

        textEditorPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(textEditorPanel, BorderLayout.WEST);

        graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        graphPanel.setPreferredSize(new Dimension(400, 300));
        this.add(graphPanel, BorderLayout.CENTER);

        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BorderLayout());
        warningPanel.setPreferredSize(new Dimension(300, 100));
        warningPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel warningLabel = new JLabel("Warnings and Messages:");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        warningField = new JTextArea("Warnings will be displayed here after using Compile or Run from the Actions menu");
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

        // Top panel is now created in createTopPanel() method
        add(topPanel, BorderLayout.NORTH);
    }

    private void createTopPanel() {
        topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);
        
        // Create title label on the left (determine if it's NFA or DFA based on the automaton type)
        String title = automaton instanceof NondeterministicFiniteAutomaton.NFA ? "NFA Tab" : 
                      automaton instanceof DeterministicFiniteAutomaton.DFA ? "DFA Tab" : "FA Tab";
        JLabel tabLabel = new JLabel(title);
        tabLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tabLabel.setForeground(new Color(102, 133, 102));
        
        // Create menu bar for the right side
        createMenuBar();
        
        // Add components to top panel
        topPanel.add(tabLabel, BorderLayout.WEST);
        topPanel.add(menuBar, BorderLayout.EAST);
    }
    
    /**
     * Creates the menu bar with run, compile, and save options
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(Color.lightGray);
        
        // Actions menu
        JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Menu items
        JMenuItem runItem = new JMenuItem("Run");
        JMenuItem compileItem = new JMenuItem("Compile");
        JMenuItem saveItem = new JMenuItem("Save");
        
        // Set colors similar to original buttons
        runItem.setBackground(new Color(166, 255, 166));
        compileItem.setBackground(new Color(255, 166, 166));
        saveItem.setBackground(new Color(242, 255, 166));
        
        // Add action listeners (reusing the existing logic)
        runItem.addActionListener(e -> {
            String inputText = textArea.getText();
            JLabel imageLabel = automaton.toGraphviz(inputText);
            graphPanel.removeAll();
            graphPanel.add(imageLabel);
            graphPanel.revalidate();
            graphPanel.repaint();
            updateWarningDisplay();
        });
        
        compileItem.addActionListener(e -> updateWarningDisplay());
        
        saveItem.addActionListener(e -> {
            File savedFile = mainPanel.fileManager.showSaveDialog(automaton, file != null ? file.getName() : null);
            if (savedFile != null) {
                NFAPanel.this.saveFileContent(savedFile);
                file = savedFile;
                mainPanel.fileManager.addToRecentFiles(file);
                

            }
        });
        
        // Add items to menu
        actionsMenu.add(runItem);
        actionsMenu.addSeparator();
        actionsMenu.add(compileItem);
        actionsMenu.addSeparator();
        actionsMenu.add(saveItem);
        
        // Add menu to menu bar
        menuBar.add(actionsMenu);
    }
    

    
    /**
     * Updates the warning display with current validation messages
     */
    private void updateWarningDisplay() {
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
        warningField.setCaretPosition(0); // Scroll to top
    }

    protected void saveFileContent(File file) {
        String text = textArea.getText();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
            JOptionPane.showMessageDialog(this, "File saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    public void loadFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            textArea.read(reader, null);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }
}
