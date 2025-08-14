package UserInterface;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

    private MainPanel mainPanel;
    private boolean isFullscreen = false;
    
    public MainFrame() {
        setTitle("CS.410 Graph System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Start maximized instead of fixed size
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        
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
        runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem compileItem = new JMenuItem("Compile");
        compileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        
        
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
        
        // Add View menu
        createViewMenu(menuBar);
        
        setJMenuBar(menuBar);
    }
    
    private void createViewMenu(JMenuBar menuBar) {
        JMenu viewMenu = new JMenu("View");
        
        // Fullscreen toggle
        JMenuItem fullscreenItem = new JMenuItem("Toggle Fullscreen");
        fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        fullscreenItem.addActionListener(e -> toggleFullscreen());
        
        // Toggle sidebar
        JMenuItem toggleSidebarItem = new JMenuItem("Toggle Sidebar");
        toggleSidebarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, InputEvent.CTRL_DOWN_MASK));
        toggleSidebarItem.addActionListener(e -> toggleSidebar());
        
        // Tab navigation
        JMenuItem nextTabItem = new JMenuItem("Next Tab");
        nextTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK));
        nextTabItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.switchToNextTab();
        });
        
        JMenuItem prevTabItem = new JMenuItem("Previous Tab");
        prevTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        prevTabItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.switchToPreviousTab();
        });
        
        viewMenu.add(fullscreenItem);
        viewMenu.addSeparator();
        viewMenu.add(toggleSidebarItem);
        viewMenu.addSeparator();
        viewMenu.add(nextTabItem);
        viewMenu.add(prevTabItem);
        
        menuBar.add(viewMenu);
    }
    
    private void toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen - return to maximized windowed mode
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            isFullscreen = false;
        } else {
            // Enter fullscreen - use MAXIMIZED_BOTH for now
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            isFullscreen = true;
        }
        repaint();
    }
    
    private void toggleSidebar() {
        if (mainPanel != null) {
            mainPanel.toggleSidebar();
        }
    }
    

    public static void main(String[] args) {
                SwingUtilities.invokeLater(MainFrame::new);
            }
        }
