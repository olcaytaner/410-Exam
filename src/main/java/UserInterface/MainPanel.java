package UserInterface;

import javax.swing.filechooser.FileNameExtensionFilter;

import NondeterministicFiniteAutomaton.NFA;
import DeterministicFiniteAutomaton.DFA;
import TuringMachine.TM;
import PushDownAutomaton.PDA;
import common.Automaton;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import javax.swing.*;

public class MainPanel extends JPanel {
    public LinkedList<File> savedPageList;
    public JPanel recentFilesPanel;

    private JPanel objectPanel; 

    public class FileManager {
        public String getExtensionForAutomaton(Automaton automaton) {
            if (automaton instanceof NFA) return ".nfa";
            if (automaton instanceof DFA) return ".dfa"; 
            if (automaton instanceof PDA) return ".pda";
            if (automaton instanceof TM) return ".tm";
                // TODO: Add CFG and REX;
            return ".txt";
        }
        
     
        public Color getColorForExtension(String extension) {
            switch(extension.toLowerCase()) {
                case ".nfa":
                case ".pda":
                    return new Color(230, 250, 230);
                case ".dfa": 
                case ".tm":
                    return new Color(250, 240, 230);
                case ".cfg":
                case ".rex":
                    return new Color(240, 230, 250);
                default:
                    return Color.LIGHT_GRAY;
            }
        }
        
       
        public JPanel createPanelForFile(File file) throws Exception {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            String extension = getFileExtension(file);
            Automaton automaton = null;
            JPanel panel = null;
            
            switch(extension) {
                case ".nfa":
                    automaton = new NFA();
                    automaton.setInputText(content);
                    panel = new NFAPanel(MainPanel.this, automaton);
                    ((NFAPanel)panel).loadFile(file);
                    break;
                case ".pda":
                    automaton = new PDA();
                    automaton.setInputText(content);
                    panel = new PDAPanel(MainPanel.this, automaton);
                    ((PDAPanel)panel).loadFile(file);
                    break;
                // TODO: Add other
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + extension);
            }
            
            return panel;
        }
        
        /**
         * Adds a file to recent files if not already present
         */
        public void addToRecentFiles(File file) {
            if (!savedPageList.contains(file)) {
                savedPageList.add(file);
                addRecentFileButton(file);
                refreshRecentFilesUI();
            }
        }
        
        /**
         * Opens a file and displays it in the object panel
         */
        public void openFile(File file) {
            try {
                JPanel panel = createPanelForFile(file);
                addToRecentFiles(file);
                
                objectPanel.removeAll();
                objectPanel.add(panel);
                objectPanel.revalidate();
                objectPanel.repaint();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(MainPanel.this,
                    "Error opening file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * Shows file chooser to select and open a file
         */
        public void showOpenDialog() {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Automaton Files", "nfa", "pda", "tm", "dfa", "rex", "cfg", "txt");
            fileChooser.setFileFilter(filter);
            
            int option = fileChooser.showOpenDialog(MainPanel.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                openFile(file);
            }
        }
        
        /**
         * Shows save dialog and returns the selected file with proper extension
         */
        public File showSaveDialog(Automaton automaton, String currentFileName) {
            JFileChooser fileChooser = new JFileChooser();
            String extension = getExtensionForAutomaton(automaton);
            String filterName = extension.substring(1).toUpperCase() + " Files (*" + extension + ")";
            fileChooser.setFileFilter(new FileNameExtensionFilter(filterName, extension.substring(1)));
            
            if (currentFileName != null) {
                fileChooser.setSelectedFile(new File(currentFileName));
            }
            
            int option = fileChooser.showSaveDialog(MainPanel.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(extension)) {
                    file = new File(file.toString() + extension);
                }
                return file;
            }
            return null;
        }
        
        private String getFileExtension(File file) {
            String name = file.getName();
            int lastDot = name.lastIndexOf(".");
            return lastDot > 0 ? name.substring(lastDot) : "";
        }
    }
    
    public FileManager fileManager;

    /**
     * Creates and configures a button for a recent file
     */
    private void addRecentFileButton(File file) {
        JButton fileButton = new JButton(file.getName());
        fileButton.setMaximumSize(new Dimension(280, 40));
        fileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        fileButton.setBackground(fileManager.getColorForExtension(extension));
        
        fileButton.addActionListener(e -> fileManager.openFile(file));
        recentFilesPanel.add(fileButton);
        recentFilesPanel.add(Box.createVerticalStrut(5));
    }
    
    /**
     * Refreshes the recent files UI
     */
    private void refreshRecentFilesUI() {
        recentFilesPanel.revalidate();
        recentFilesPanel.repaint();
    }
    
    /**
     * Loads recent files from savedPageList and creates buttons for them
     */
    private void loadRecentFilesButtons() {
        for(File file : savedPageList) {
            addRecentFileButton(file);
        }
    }
    
    /**
     * Creates the welcome panel with "New File" and "Open From Computer" buttons
     */
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Welcome to CS.410 Graph System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 0));
        
        JLabel subtitleLabel = new JLabel("Choose an option to get started");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton newFileButton = createStyledButton("Create New File", Color.darkGray);
        newFileButton.addActionListener(e -> showNewAutomatonMenu(newFileButton));
        
        JButton openFileButton = createStyledButton("Open From Computer", Color.darkGray);
        openFileButton.addActionListener(e -> fileManager.showOpenDialog());
        
        buttonsPanel.add(newFileButton);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(openFileButton);
        
        contentPanel.add(titleLabel);
        contentPanel.add(subtitleLabel);
        contentPanel.add(buttonsPanel);
        contentPanel.add(Box.createVerticalGlue());
        
        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        
        return welcomePanel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 45));
        button.setPreferredSize(new Dimension(250, 45));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private void showNewAutomatonMenu(JButton parentButton) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem nfaItem = new JMenuItem("NFA (Nondeterministic Finite Automaton)");
        JMenuItem dfaItem = new JMenuItem("DFA (Deterministic Finite Automaton)");
        
        JMenuItem pdaItem = new JMenuItem("PDA (Push-down Automaton)");
        JMenuItem tmItem = new JMenuItem("TM (Turing Machine)");
        
        JMenuItem cfgItem = new JMenuItem("CFG (Context-Free Grammar)");
        JMenuItem rexItem = new JMenuItem("REX (Regular Expression)");
        
        nfaItem.addActionListener(e -> createNewAutomaton("NFA"));
        dfaItem.addActionListener(e -> createNewAutomaton("DFA"));
        pdaItem.addActionListener(e -> createNewAutomaton("PDA"));
        tmItem.addActionListener(e -> createNewAutomaton("TM"));
        cfgItem.addActionListener(e -> createNewAutomaton("CFG"));
        rexItem.addActionListener(e -> createNewAutomaton("REX"));
        
        menu.add(nfaItem);
        menu.add(dfaItem);
        menu.addSeparator();
        menu.add(pdaItem);
        menu.add(tmItem);
        menu.addSeparator();
        menu.add(cfgItem);
        menu.add(rexItem);
        
        // Show menu below the button
        menu.show(parentButton, 0, parentButton.getHeight());
    }

    public MainPanel() {
        this.fileManager = new FileManager();
        this.setLayout(new BorderLayout());
        this.setSize(900, 400); 
        this.setBackground(new Color(248, 249, 250)); 

        /*
         * Recent files panel (left side).
         * This panel contains the recent files that the user has opened.
         */
        recentFilesPanel = new JPanel();
        recentFilesPanel.setLayout(new BoxLayout(recentFilesPanel, BoxLayout.Y_AXIS));
        recentFilesPanel.setPreferredSize(new Dimension(150, 400));
        recentFilesPanel.setBackground(Color.WHITE);
        recentFilesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        

        /*
         * Object panel (center).
         * This panel contains the current automaton being edited.
         */
        objectPanel = new JPanel(); 
        objectPanel.setLayout(new BorderLayout());
        objectPanel.setBackground(Color.WHITE);
        objectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
       
        JPanel welcomePanel = createWelcomePanel();
        objectPanel.add(welcomePanel, BorderLayout.CENTER);

        savedPageList = new LinkedList<>();

        // Recent Files header
        JLabel recentFilesLabel = new JLabel("Recent Files");
        recentFilesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        recentFilesPanel.add(recentFilesLabel);
        recentFilesPanel.add(Box.createVerticalStrut(10));

        // Load existing recent files
        loadRecentFilesButtons();

        this.add(recentFilesPanel, BorderLayout.WEST);
        this.add(objectPanel, BorderLayout.CENTER);

    }
    
    /**
     * Creates a new automaton of the specified type
     * Called from the menu bar
     */
    public void createNewAutomaton(String type) {
        Automaton automaton = null;
        JPanel panel = null;
        
        switch(type) {
            case "NFA":
                automaton = new NFA();
                panel = new NFAPanel(this, automaton);
                break;
            case "DFA":
                automaton = new DFA();
                panel = new NFAPanel(this, automaton);
                break;
            case "PDA":
                automaton = new PDA();
                panel = new PDAPanel(this, automaton);
                break;
            case "TM":
                automaton = new TM(); // TODO: Replace with TuringMachine when constructor is available
                panel = new PDAPanel(this, automaton);
                break;
            case "CFG":
                automaton = new NFA(); // TODO: Replace with CFG when it extends Automaton
                panel = new NFAPanel(this, automaton);
                break;
            case "REX":
                automaton = new NFA(); // TODO: Replace with REX when it extends Automaton
                panel = new NFAPanel(this, automaton);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown automaton type: " + type, "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
        
        if (panel != null) {
            objectPanel.removeAll();
            objectPanel.add(panel);
            objectPanel.revalidate();
            objectPanel.repaint();
        }
    }
}
