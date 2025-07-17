package UserInterface;

import javax.swing.*;

public class MainFrame extends JFrame {
    protected JTabbedPane tabbedPane;
            public MainFrame() {
                setTitle("CS.410 Graph System");
                setSize(800, 600);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



                tabbedPane = new JTabbedPane();
                MainPanel mainPanel = new MainPanel(tabbedPane);
                tabbedPane.addTab("Main Page", mainPanel);

                add(tabbedPane);

                setVisible(true);
            }

            public static void main(String[] args) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MainFrame();
                    }
                });
            }
        }
