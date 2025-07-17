package UserInterface;

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


        JLabel openSavedLabel = new JLabel("Open Saved Pages");
        savedPages.add(openSavedLabel);

        savedPages.add(Box.createVerticalStrut(10));

        JButton deneme = new JButton("deneme");
        savedPages.add(deneme);

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
                NFAPanel nfaPanel = new NFAPanel(tabbedPane, MainPanel.this);
                tabbedPane.addTab("NFA", nfaPanel);
                tabbedPane.setSelectedComponent(nfaPanel);
            }
        });



    }

}
