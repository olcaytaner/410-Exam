package UserInterface;

import javax.swing.filechooser.FileNameExtensionFilter;

import NondeterministicFiniteAutomaton.NFA;
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
    private JPanel objectPanel; 

    public MainPanel(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        this.setLayout(new BorderLayout());
        this.setSize(900, 400); 
        this.setBackground(new Color(248, 249, 250)); 

        JPanel defaultPages = new JPanel();
        defaultPages.setLayout(new BoxLayout(defaultPages, BoxLayout.Y_AXIS));
        defaultPages.setPreferredSize(new Dimension(250, 400));
        defaultPages.setBackground(Color.WHITE);
        defaultPages.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        objectPanel = new JPanel(); 
        objectPanel.setLayout(new BorderLayout());
        objectPanel.setBackground(Color.WHITE);
        objectPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel placeholderLabel = new JLabel("<html><div style='text-align: center;'>" +
            "<h2>Select an Automaton</h2>" +
            "<p>Choose from the options on the left to create and edit automata</p>" +
            "</div></html>");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setVerticalAlignment(SwingConstants.CENTER);
        objectPanel.add(placeholderLabel, BorderLayout.CENTER);

        savedPages = new JPanel();
        savedPages.setLayout(new BoxLayout(savedPages, BoxLayout.Y_AXIS));
        savedPages.setPreferredSize(new Dimension(250, 400));
        savedPages.setBackground(Color.WHITE);
        savedPages.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        savedPageList = new LinkedList<>();

        JLabel newLabel = new JLabel("New");
        newLabel.setFont(new Font("Arial", Font.BOLD, 14));
        defaultPages.add(newLabel);
        defaultPages.add(Box.createVerticalStrut(10));

        JLabel faLabel = new JLabel("Finite Automata:");
        faLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        defaultPages.add(faLabel);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton nfaButton = new JButton("⚫ NFA (Nondeterministic)");
        nfaButton.setBackground(new Color(230, 250, 230));
        defaultPages.add(nfaButton);
        defaultPages.add(Box.createVerticalStrut(3));

        JButton dfaButton = new JButton("⚫ DFA (Deterministic)");
        dfaButton.setBackground(new Color(230, 250, 230));
        defaultPages.add(dfaButton);
        defaultPages.add(Box.createVerticalStrut(8));

        JLabel advancedLabel = new JLabel("Advanced Automata:");
        advancedLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        defaultPages.add(advancedLabel);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton pdaButton = new JButton("⚫ PDA (Push-down)");
        pdaButton.setBackground(new Color(250, 240, 230));
        defaultPages.add(pdaButton);
        defaultPages.add(Box.createVerticalStrut(3));

        JButton tmButton = new JButton("⚫ TM (Turing Machine)");
        tmButton.setBackground(new Color(250, 240, 230));
        defaultPages.add(tmButton);
        defaultPages.add(Box.createVerticalStrut(8));

        JLabel grammarLabel = new JLabel("Grammar & Expressions:");
        grammarLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        defaultPages.add(grammarLabel);
        defaultPages.add(Box.createVerticalStrut(5));

        JButton cfgButton = new JButton("⚫ CFG (Context-Free Grammar)");
        cfgButton.setBackground(new Color(240, 230, 250));
        defaultPages.add(cfgButton);
        defaultPages.add(Box.createVerticalStrut(3));

        JButton rexButton = new JButton("⚫ REX (Regular Expression)");
        rexButton.setBackground(new Color(240, 230, 250));
        defaultPages.add(rexButton);
        defaultPages.add(Box.createVerticalStrut(10));

        JButton openFile = new JButton("📁 Open File");
        openFile.setBackground(Color.lightGray);
        savedPages.add(openFile);

        savedPages.add(Box.createVerticalStrut(10));

        JLabel openSavedLabel = new JLabel("Recent Files");
        openSavedLabel.setFont(new Font("Arial", Font.BOLD, 14));
        savedPages.add(openSavedLabel);

        for(File a : savedPageList ){
            String name = a.getName();
            JButton jbutton = new JButton(name); 
            savedPages.add(jbutton);
            savedPages.add(Box.createVerticalStrut(5));
        }

        this.add(defaultPages, BorderLayout.WEST);
        this.add(objectPanel, BorderLayout.CENTER);
        this.add(savedPages, BorderLayout.EAST);

        nfaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Automaton nfaMachine = new NFA();
                NFAPanel nfaPanel = new NFAPanel(tabbedPane, MainPanel.this, nfaMachine);
                objectPanel.removeAll();
                objectPanel.add(nfaPanel);
                objectPanel.revalidate();
                objectPanel.repaint();
            }
        });

        dfaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Automaton dfaMachine = new NFAMachine(); // TODO: Replace with actual DFAMachine when available
                NFAPanel dfaPanel = new NFAPanel(tabbedPane, MainPanel.this, dfaMachine);
                objectPanel.removeAll();
                objectPanel.add(dfaPanel);
                objectPanel.revalidate();
                objectPanel.repaint();
            }
        });

        pdaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Automaton pdaMachine = new PDA();           
                PDAPanel pdaPanel = new PDAPanel(tabbedPane, MainPanel.this, pdaMachine);
                objectPanel.removeAll();
                objectPanel.add(pdaPanel);
                objectPanel.revalidate();
                objectPanel.repaint();
            }
        });

        tmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Automaton tmMachine = new NFAMachine(); // TODO: Replace with actual TM creation logic
                PDAPanel tmPanel = new PDAPanel(tabbedPane, MainPanel.this, tmMachine);
                objectPanel.removeAll();
                objectPanel.add(tmPanel);
                objectPanel.revalidate();
                objectPanel.repaint();
            }
        });

        cfgButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Automaton cfgMachine = new NFAMachine(); // TODO: Replace with actual CFG machine
                NFAPanel cfgPanel = new NFAPanel(tabbedPane, MainPanel.this, cfgMachine);
                objectPanel.removeAll();
                objectPanel.add(cfgPanel);
                objectPanel.revalidate();
                objectPanel.repaint();
            }
        });

        rexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Automaton rexMachine = new NFAMachine(); // TODO: Replace with actual REX machine
                NFAPanel rexPanel = new NFAPanel(tabbedPane, MainPanel.this, rexMachine);
                objectPanel.removeAll();
                objectPanel.add(rexPanel);
                objectPanel.revalidate();
                objectPanel.repaint();
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
                                automaton = new NFA();
                                automaton.setInputText(content);
                                panel = new NFAPanel(tabbedPane, MainPanel.this, automaton);
                                break;
                            case ".pda":
                                automaton = new PDA();
                                automaton.setInputText(content);
                                panel = new PDAPanel(tabbedPane, MainPanel.this, automaton);
                                ((PDAPanel)panel).loadFile(file);
                                savedPageList.add(file);
                                break;
                        }

                        if (panel != null) {
                            JButton fileButton = new JButton(file.getName());
                            fileButton.setMaximumSize(new Dimension(280, 40));
                            fileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                            if(file.getName().substring(file.getName().lastIndexOf(".")).equals(".nfa") || file.getName().substring(file.getName().lastIndexOf(".") ).equals(".pda")) { fileButton.setBackground(new Color(230, 250, 230)); }
                            else if(file.getName().substring(file.getName().lastIndexOf(".")).equals(".dfa") || file.getName().substring(file.getName().lastIndexOf(".")).equals(".tm")) { fileButton.setBackground(new Color(250, 240, 230)); }
                            else if(file.getName().substring(file.getName().lastIndexOf(".")).equals(".cfg") || file.getName().substring(file.getName().lastIndexOf(".")).equals(".rex")) { fileButton.setBackground(new Color(240, 230, 250)); }
                            savedPages.add(fileButton);
                            savedPages.revalidate();
                            savedPages.repaint();

                            tabbedPane.addTab(file.getName(), panel);
                            tabbedPane.setSelectedComponent(panel);
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
