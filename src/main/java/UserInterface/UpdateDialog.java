package UserInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;

/**
 * Dialog that displays update notification when a newer version is available.
 * Styled to match the application's dark theme.
 */
public class UpdateDialog extends JDialog {

    // Theme colors (matching SplashScreen)
    private static final Color BACKGROUND_COLOR = new Color(43, 43, 43);
    private static final Color TEXT_COLOR = new Color(187, 187, 187);
    private static final Color ACCENT_COLOR = new Color(75, 110, 175);
    private static final Color SECONDARY_TEXT_COLOR = new Color(150, 150, 150);
    private static final Color BORDER_COLOR = new Color(70, 70, 70);

    public UpdateDialog(Frame parent, VersionChecker.VersionInfo versionInfo) {
        super(parent, "Update Available", true);
        initComponents(versionInfo);
        setLocationRelativeTo(parent);
    }

    private void initComponents(VersionChecker.VersionInfo versionInfo) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("A new version is available!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createUpdateIcon());

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Version info panel
        JPanel versionPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        versionPanel.setBackground(BACKGROUND_COLOR);
        versionPanel.setBorder(new EmptyBorder(10, 0, 15, 0));

        JLabel currentLabel = new JLabel("Current version:");
        currentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        currentLabel.setForeground(SECONDARY_TEXT_COLOR);

        JLabel currentVersionLabel = new JLabel(versionInfo.currentVersion);
        currentVersionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        currentVersionLabel.setForeground(TEXT_COLOR);

        JLabel latestLabel = new JLabel("Latest version:");
        latestLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        latestLabel.setForeground(SECONDARY_TEXT_COLOR);

        JLabel latestVersionLabel = new JLabel(versionInfo.latestVersion);
        latestVersionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        latestVersionLabel.setForeground(ACCENT_COLOR);

        versionPanel.add(currentLabel);
        versionPanel.add(currentVersionLabel);
        versionPanel.add(latestLabel);
        versionPanel.add(latestVersionLabel);

        // Release notes panel
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBackground(BACKGROUND_COLOR);
        notesPanel.setBorder(new EmptyBorder(5, 0, 15, 0));

        JLabel notesTitle = new JLabel("Release Notes:");
        notesTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        notesTitle.setForeground(SECONDARY_TEXT_COLOR);
        notesTitle.setBorder(new EmptyBorder(0, 0, 5, 0));

        JTextArea notesArea = new JTextArea();
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(new Color(50, 50, 50));
        notesArea.setForeground(TEXT_COLOR);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        notesArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        notesArea.setCaretColor(TEXT_COLOR);

        String notes = versionInfo.releaseNotes;
        if (notes != null && !notes.isEmpty()) {
            // Clean up markdown formatting for display
            notes = cleanMarkdown(notes);
            notesArea.setText(notes);
        } else {
            notesArea.setText("No release notes available.");
        }

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(350, 120));
        notesScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        notesScroll.getVerticalScrollBar().setUnitIncrement(16);

        notesPanel.add(notesTitle, BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton downloadButton = createStyledButton("Download", true);
        JButton remindButton = createStyledButton("Remind Later", false);

        downloadButton.addActionListener((ActionEvent e) -> {
            openReleaseUrl(versionInfo.releaseUrl);
            dispose();
        });

        remindButton.addActionListener((ActionEvent e) -> dispose());

        buttonsPanel.add(remindButton);
        buttonsPanel.add(downloadButton);

        // Assemble main panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.add(versionPanel, BorderLayout.NORTH);
        contentPanel.add(notesPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();

        // Set minimum size
        setMinimumSize(new Dimension(420, 320));
    }

    /**
     * Creates a styled button matching the dark theme.
     */
    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isPrimary) {
            button.setBackground(ACCENT_COLOR);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
                new EmptyBorder(8, 20, 8, 20)
            ));
        } else {
            button.setBackground(new Color(60, 60, 60));
            button.setForeground(TEXT_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 20, 8, 20)
            ));
        }

        // Hover effect
        Color originalBg = button.getBackground();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    /**
     * Creates a simple update icon using Unicode character.
     */
    private Icon createUpdateIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ACCENT_COLOR);
                g2d.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
                g2d.drawString("\u2B06", x, y + 18); // Up arrow
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 24;
            }

            @Override
            public int getIconHeight() {
                return 24;
            }
        };
    }

    /**
     * Opens the release URL in the default browser.
     */
    private void openReleaseUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            // Show fallback dialog with URL
            JOptionPane.showMessageDialog(this,
                "Could not open browser. Please visit:\n" + url,
                "Open Browser",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Cleans up markdown formatting for display in plain text.
     */
    private String cleanMarkdown(String text) {
        if (text == null) return "";

        // Remove markdown headers
        text = text.replaceAll("#{1,6}\\s*", "");

        // Convert markdown links [text](url) to just text
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");

        // Convert **bold** to just text
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");

        // Convert *italic* to just text
        text = text.replaceAll("\\*([^*]+)\\*", "$1");

        // Convert markdown bullet points to unicode bullets
        text = text.replaceAll("(?m)^\\s*[-*]\\s+", "\u2022 ");

        // Clean up extra whitespace
        text = text.replaceAll("(?m)^\\s+$", "");
        text = text.replaceAll("\n{3,}", "\n\n");

        return text.trim();
    }
}
