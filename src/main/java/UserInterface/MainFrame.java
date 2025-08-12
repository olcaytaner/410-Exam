package UserInterface;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

    private MainPanel mainPanel;
    
    public MainFrame() {
        setTitle("CS.410 Graph System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createMenuBar();

        mainPanel = new MainPanel();
        add(mainPanel);

        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        
        JMenu newMenu = new JMenu("New");
        
        JMenuItem nfaItem = new JMenuItem("NFA (Nondeterministic)");
        JMenuItem dfaItem = new JMenuItem("DFA (Deterministic)");
        
        JMenuItem pdaItem = new JMenuItem("PDA (Push-down)");
        JMenuItem tmItem = new JMenuItem("TM (Turing Machine)");
        
        JMenuItem cfgItem = new JMenuItem("CFG (Context-Free Grammar)");
        JMenuItem rexItem = new JMenuItem("REX (Regular Expression)");
        
        newMenu.add(nfaItem);
        newMenu.add(dfaItem);
        newMenu.addSeparator();
        newMenu.add(pdaItem);
        newMenu.add(tmItem);
        newMenu.addSeparator();
        newMenu.add(cfgItem);
        newMenu.add(rexItem);
        
        JMenuItem openItem = new JMenuItem("Open From Computer");
        
        nfaItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.createNewAutomaton("NFA");
        });
        dfaItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.createNewAutomaton("DFA");
        });
        pdaItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.createNewAutomaton("PDA");
        });
        tmItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.createNewAutomaton("TM");
        });
        cfgItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.createNewAutomaton("CFG");
        });
        rexItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.createNewAutomaton("REX");
        });
        openItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.fileManager.showOpenDialog();
        });
        
        fileMenu.add(newMenu);
        fileMenu.add(openItem);
        
        menuBar.add(fileMenu);
        
        JMenu actionsMenu = new JMenu("Actions");
        
        JMenuItem runItem = new JMenuItem("Run");
        JMenuItem compileItem = new JMenuItem("Compile");
        JMenuItem saveItem = new JMenuItem("Save");
        
        runItem.setBackground(new java.awt.Color(166, 255, 166));
        compileItem.setBackground(new java.awt.Color(255, 166, 166));
        saveItem.setBackground(new java.awt.Color(242, 255, 166));
        
        // Add action listeners that delegate to the current active panel
        runItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.runCurrentAutomaton();
        });
        
        compileItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.compileCurrentAutomaton();
        });
        
        saveItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.saveCurrentAutomaton();
        });
        
        actionsMenu.add(runItem);
        actionsMenu.addSeparator();
        actionsMenu.add(compileItem);
        actionsMenu.addSeparator();
        actionsMenu.add(saveItem);
        
        menuBar.add(actionsMenu);
        
        setJMenuBar(menuBar);
    }

            public static void main(String[] args) {
                SwingUtilities.invokeLater(MainFrame::new);
            }
        }
