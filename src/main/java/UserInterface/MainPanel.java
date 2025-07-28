package UserInterface;

import javax.swing.filechooser.FileNameExtensionFilter;
import common.Automaton;
import PushDownAutomaton.PDA;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public class MainPanel extends JPanel {
    public LinkedList<File> savedPageList;
    public JPanel savedPages;
    private JTabbedPane tabbedPane;


    public MainPanel(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(600, 400);
        //this.setBackground(Color.lightGray);

        JPanel defaultPages = new JPanel();
        defaultPages.setLayout(new BoxLayout(defaultPages, BoxLayout.Y_AXIS));
        defaultPages.setPreferredSize(new Dimension(300, 300));
        defaultPages.setBackground(Color.white);
        defaultPages.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        savedPages = new JPanel();
        savedPages.setLayout(new BoxLayout(savedPages, BoxLayout.Y_AXIS));
        savedPages.setPreferredSize(new Dimension(300, 300));
        savedPages.setBackground(Color.white);
        savedPages.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        savedPageList = new LinkedList<>();

        JLabel openLabel= new JLabel("Open New Page");
        defaultPages.add(openLabel);

        defaultPages.add(Box.createVerticalStrut(10));

        JButton nfaButton = new JButton("NFA");
        defaultPages.add(nfaButton);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton dfaButton = new JButton("DFA");
        defaultPages.add(dfaButton);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton cfgButton = new JButton("CFG");
        defaultPages.add(cfgButton);
        defaultPages.add(Box.createVerticalStrut(5));


        JButton pdaButton = new JButton("PDA");
        defaultPages.add(pdaButton);
        defaultPages.add(Box.createVerticalStrut(5));


        JButton rexButton = new JButton("REX");
        defaultPages.add(rexButton);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton tmButton = new JButton("TM");
        defaultPages.add(tmButton);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton openFile = new JButton("Open File");
        openFile.setBackground(Color.lightGray);
        savedPages.add(openFile);

        savedPages.add(Box.createVerticalStrut(10));

        JLabel openSavedLabel = new JLabel("Recent Files");
        savedPages.add(openSavedLabel);

        for(File a : savedPageList ){
            String name = a.getName();
            JButton jbutton = new JButton(name); // farklı buttonlar oluşturuyor mu
            savedPages.add(jbutton);
            savedPages.add(Box.createVerticalStrut(5));
        }

        this.add(Box.createHorizontalGlue());
        this.add(defaultPages);
        this.add(Box.createHorizontalStrut(40)); // space between panels
        this.add(savedPages);
        this.add(Box.createHorizontalGlue());

        nfaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               
               Automaton nfaMachine = new NFAMachine();
                NFAPanel nfaPanel = new NFAPanel(tabbedPane, MainPanel.this, nfaMachine);
                tabbedPane.addTab("NFA", nfaPanel);
                tabbedPane.setSelectedComponent(nfaPanel);
            }
        });

        pdaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Automaton pdaMachine = new PDA();           
                PDAPanel pdaPanel = new PDAPanel(tabbedPane, MainPanel.this, pdaMachine);
                tabbedPane.addTab("PDA", pdaPanel);
                tabbedPane.setSelectedComponent(pdaPanel);
            }
        });

        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Automaton Files", "nfa", "pda", "tm", "dfa", "rex", "cfg","txt");
                fileChooser.setFileFilter(filter);
                int option = fileChooser.showOpenDialog(MainPanel.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    savedPageList.add(file);
                    String name = file.getName();
                    String extension = name.substring(name.lastIndexOf("."));
                    
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                        Automaton automaton = null;
                        JPanel panel = null;
                        
                        switch(extension) {
                            case ".nfa":
                                automaton = new NFAMachine();
                                automaton.setInputText(content);
                                panel = new NFAPanel(tabbedPane, MainPanel.this, automaton);
                                break;
                            case ".pda":
                                automaton = new PDA();
                                automaton.setInputText(content);
                                panel = new PDAPanel(tabbedPane, MainPanel.this, automaton);
                                ((PDAPanel)panel).loadFile(file);
                                savedPageList.add(file);
                                tabbedPane.addTab(file.getName(), panel);
                                tabbedPane.setSelectedComponent(panel);
                                break;
                            // Add other cases for different automaton types
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(MainPanel.this, 
                            "Error reading file: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

    
        });


    }

}
