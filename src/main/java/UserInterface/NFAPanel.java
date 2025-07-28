package UserInterface;

import common.Automaton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.*;
import java.util.List;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

public class NFAPanel extends JPanel {

    JButton saveButton, warningButton, compileButton, runButton, closeButton;
    JPanel textEditorPanel, graphPanel, buttonPanel, warningPanel;
    JTextArea textArea;
    JScrollPane scrollPane;
    JTextField warningField;
    private File file;
    private MainPanel mainPanel;
    private Automaton automaton;

    public NFAPanel(JTabbedPane tabbedPane, MainPanel mainPanel, Automaton automaton) {
        this.mainPanel = mainPanel;
        this.automaton = automaton;
        this.setLayout(new BorderLayout());
        this.setSize(600, 400);


        textEditorPanel = new JPanel();
        textEditorPanel.setLayout(new BorderLayout());
        textEditorPanel.setPreferredSize(new Dimension(500, 300));
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

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setPreferredSize(new Dimension(300, 100));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        saveButton = new JButton("Save");
        saveButton.setBackground(Color.orange);
        warningButton = new JButton("Warnings");
        warningButton.setBackground(Color.lightGray);
        runButton = new JButton("Run");
        runButton.setBackground(Color.green);
        compileButton = new JButton("Compile");
        compileButton.setBackground(Color.red);
        warningField = new JTextField("Warnings will be displayed here after clicking the Warning Button");

        buttonPanel.add(runButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(compileButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(warningButton);
        buttonPanel.add(Box.createVerticalStrut(5));

        buttonPanel.add(warningField);
        JScrollPane scrollPane1 = new JScrollPane(warningField);
        buttonPanel.add(scrollPane1, BorderLayout.NORTH);

        this.add(buttonPanel, BorderLayout.EAST);

        runButton.addActionListener(new ActionListener() { // WARNİNGLERİ GÖSTER + RENDERLA
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = textArea.getText();
                JLabel imageLabel = automaton.toGraphviz(inputText);
                graphPanel.removeAll();
                graphPanel.add(imageLabel);
                graphPanel.revalidate();
                graphPanel.repaint();
            }
        });

        compileButton.addActionListener(new ActionListener() { // WARNİNGLERİ GÖSTER
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile(file);
                if (file != null) {
                    mainPanel.savedPageList.add(file);
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    tabbedPane.setTitleAt(selectedIndex, file.getName());
                    JButton button = new JButton(file.getName());
                    mainPanel.savedPages.add(Box.createVerticalStrut(5));
                    mainPanel.savedPages.add(button);

                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            NFAPanel panel = new NFAPanel(tabbedPane, mainPanel, automaton);
                            panel.loadFile(file);
                            tabbedPane.addTab(file.getName(), panel);
                            tabbedPane.setSelectedComponent(panel);
                        }
                    });
                }
            }
        });


        warningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text =  getWarningText();
                warningField.setText(text);

            }
        });


        closeButton = new JButton("Close Tab");
        closeButton.addActionListener(e -> {
            tabbedPane.remove(this);
        });

        add(closeButton, BorderLayout.NORTH);
    }

    private String getWarningText() {
        String inputText = textArea.getText();
        automaton.setInputText(inputText);
        
        List<Automaton.ValidationMessage> messages = automaton.validate();
        if (messages.isEmpty()) {
            return "No warnings or errors found!";
        }
        
        StringBuilder result = new StringBuilder();
        for (Automaton.ValidationMessage msg : messages) {
            result.append(msg.toString()).append("\n");
        }
        return result.toString();
    }


    private void saveFile(File file) {
        if (file == null) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                this.file = fileChooser.getSelectedFile();
            } else {
                return;
            }
        }
        String text = textArea.getText();
        try (FileWriter writer = new FileWriter(this.file)) {
            writer.write(text);
            JOptionPane.showMessageDialog(this, "File saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    private void loadFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            textArea.read(reader, null);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }
}
