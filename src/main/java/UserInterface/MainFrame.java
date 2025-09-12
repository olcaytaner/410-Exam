package UserInterface;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

    private MainPanel mainPanel;
    private boolean isFullscreen = false;
    private JMenu recentFilesMenu;
    private int menuShortcutKeyMask;
    
    public MainFrame() {
        setTitle("CS.410 Graph System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Save currently open tabs before closing
                if (mainPanel != null) {
                    mainPanel.saveCurrentSession();
                }
                System.exit(0);
            }
        });
        
        // Start maximized instead of fixed size
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        createMenuBar();

        mainPanel = new MainPanel();
        mainPanel.setParentFrame(this);
        add(mainPanel);

        setVisible(true);
    }
    
    private void createMenuBar() {
        menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
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
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuShortcutKeyMask));
        
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
        
        // Create Recent Files submenu
        createRecentFilesMenu(fileMenu);
        
        menuBar.add(fileMenu);
        
        JMenu actionsMenu = new JMenu("Actions");
        
        JMenuItem compileWithFigureItem = new JMenuItem("Compile with Figure");
        compileWithFigureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, menuShortcutKeyMask));
        
        JMenuItem runItem = new JMenuItem("Run");
        runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, menuShortcutKeyMask));
        
        JMenuItem runWithFileItem = new JMenuItem("Run with File");
        runWithFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, menuShortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKeyMask));
        
        // Add action listeners that delegate to the current active panel
        compileWithFigureItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.runCurrentAutomaton();
        });
        
        runItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.testCurrentAutomaton();
        });
        
        runWithFileItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.testCurrentAutomatonWithFile();
        });
        
        saveItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.saveCurrentAutomaton();
        });
        
        actionsMenu.add(compileWithFigureItem);
        actionsMenu.add(runItem);
        actionsMenu.add(runWithFileItem);
        actionsMenu.addSeparator();
        actionsMenu.add(saveItem);
        
        menuBar.add(actionsMenu);
        
        // Add View menu
        createViewMenu(menuBar, menuShortcutKeyMask);
        
        // Add Help menu
        createHelpMenu(menuBar);
        
        setJMenuBar(menuBar);
    }
    
    private void createViewMenu(JMenuBar menuBar, int menuShortcutKeyMask) {
        JMenu viewMenu = new JMenu("View");
        
        // Fullscreen toggle
        JMenuItem fullscreenItem = new JMenuItem("Toggle Fullscreen");
        fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        fullscreenItem.addActionListener(e -> toggleFullscreen());
        
        // Toggle sidebar
        JMenuItem toggleSidebarItem = new JMenuItem("Toggle Sidebar");
        toggleSidebarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, menuShortcutKeyMask));
        toggleSidebarItem.addActionListener(e -> toggleSidebar());
        
        // Tab navigation
        JMenuItem nextTabItem = new JMenuItem("Next Tab");
        nextTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, menuShortcutKeyMask));
        nextTabItem.addActionListener(e -> {
            if (mainPanel != null) mainPanel.switchToNextTab();
        });
        
        JMenuItem prevTabItem = new JMenuItem("Previous Tab");
        prevTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, menuShortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
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
    
    private void createRecentFilesMenu(JMenu fileMenu) {
        recentFilesMenu = new JMenu("Recent Files");
        fileMenu.add(recentFilesMenu);
        
        // Initial population of recent files
        updateRecentFilesMenu();
    }
    
    /**
     * Updates the Recent Files menu with current recent files
     */
    public void updateRecentFilesMenu() {
        if (recentFilesMenu == null || mainPanel == null) return;
        
        recentFilesMenu.removeAll();
        
        List<String> recentFiles = mainPanel.getRecentFiles();
        
        if (recentFiles.isEmpty()) {
            JMenuItem noFilesItem = new JMenuItem("No recent files");
            noFilesItem.setEnabled(false);
            recentFilesMenu.add(noFilesItem);
        } else {
            // Add recent files with keyboard shortcuts Ctrl+1, Ctrl+2, etc.
            for (int i = 0; i < Math.min(recentFiles.size(), 9); i++) {
                String filePath = recentFiles.get(i);
                String fileName = new java.io.File(filePath).getName();
                JMenuItem fileItem = new JMenuItem((i + 1) + " " + fileName);
                
                // Add keyboard shortcut Ctrl+(i+1)
                fileItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_1 + i, menuShortcutKeyMask));
                
                final String finalFilePath = filePath;
                fileItem.addActionListener(e -> mainPanel.openRecentFile(finalFilePath));
                
                recentFilesMenu.add(fileItem);
            }
            
            // Add remaining files (up to 10 total) without shortcuts
            for (int i = 9; i < recentFiles.size(); i++) {
                String filePath = recentFiles.get(i);
                String fileName = new java.io.File(filePath).getName();
                JMenuItem fileItem = new JMenuItem(fileName);
                
                final String finalFilePath = filePath;
                fileItem.addActionListener(e -> mainPanel.openRecentFile(finalFilePath));
                
                recentFilesMenu.add(fileItem);
            }
            
            // Add separator and clear option
            recentFilesMenu.addSeparator();
            JMenuItem clearItem = new JMenuItem("Clear Recent Files");
            clearItem.addActionListener(e -> {
                mainPanel.clearRecentFiles();
                updateRecentFilesMenu();
            });
            recentFilesMenu.add(clearItem);
        }
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
    
    private void createHelpMenu(JMenuBar menuBar) {
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
    }
    
    private String getVersionFromManifest() {
        String version = getClass().getPackage().getImplementationVersion();
        return version != null ? version : "1.0.0"; // Fallback version for development
    }
    
    private void showAboutDialog() {
        String aboutText = 
            "<html><center>" +
            "<h2>CS.410 Graph System</h2>" +
            "<p>Version " + getVersionFromManifest() + "</p><br>" +
            "<p>An educational tool for teaching and learning:<br>" +
            "DFA, NFA, PDA, Turing Machines, CFG, and Regular Expressions</p><br>" +
            "<p><b>Development Team:</b><br>" +
            "Ege Yenen • Bora Baran • Berre Delikara<br>" +
            "Eren Yemşen • Berra Eğcin • Hakan Akbıyık<br>" +
            "Hakan Çildaş • Selim Özyılmaz • Olcay Taner Yıldız</p><br>" +
            "<p>© 2024 CS.410 Graph System</p>" +
            "</center></html>";
        
        javax.swing.JOptionPane.showMessageDialog(this, aboutText, "About", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SplashScreen::new);
    }
}
