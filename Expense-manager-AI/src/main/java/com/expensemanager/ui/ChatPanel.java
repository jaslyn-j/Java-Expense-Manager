package com.expensemanager.ui;

import com.expensemanager.utils.AIChatService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChatPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);
    private static final Color USER_BUBBLE_COLOR = new Color(63, 81, 181);
    private static final Color AI_BUBBLE_COLOR = new Color(240, 240, 240);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final int BORDER_RADIUS = 10;

    private final ResourceBundle messages;
    private final AIChatService chatService;
    private Map<String, Object> dashboardData;
    private JTextField inputField;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;

    // Inner class for rounded borders
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public ChatPanel(ResourceBundle messages, String apiKey) {
        this.messages = messages;
        try {
            this.chatService = new AIChatService(apiKey);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Error initializing AI Chat: " + e.getMessage(),
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            throw e;
        }
        this.dashboardData = new HashMap<>();
        initializeUI();
    }

    public void updateDashboardData(Map<String, Object> data) {
        this.dashboardData = new HashMap<>(data);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        // Messages Panel
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BACKGROUND_COLOR);

        // Scroll Pane
        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(REGULAR_FONT);
        inputField.setBorder(new RoundedBorder(BORDER_RADIUS, ACCENT_COLOR));
        inputField.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Send");
        sendButton.setFont(REGULAR_FONT);
        sendButton.setBackground(ACCENT_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(new RoundedBorder(BORDER_RADIUS, ACCENT_COLOR));
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setPreferredSize(new Dimension(80, 30));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // Add user message
        addMessage(message, true);
        inputField.setText("");

        // Process with AI in background
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    return chatService.processQuestion(message, dashboardData);
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    addMessage(response, false);
                } catch (ExecutionException e) {
                    addMessage("Error processing request: " + e.getMessage(), false);
                } catch (Exception e) {
                    addMessage("Error processing request: " + e.getMessage(), false);
                }
            }
        };
        worker.execute();
    }

    private void addMessage(String message, boolean isUser) {
        // Outer container for alignment
        JPanel outerContainer = new JPanel();
        outerContainer.setLayout(new BoxLayout(outerContainer, BoxLayout.X_AXIS));
        outerContainer.setBackground(BACKGROUND_COLOR);
        outerContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        outerContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Text area (bubble content)
        JTextArea textArea = new JTextArea(message);
        textArea.setFont(REGULAR_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(isUser ? USER_BUBBLE_COLOR : AI_BUBBLE_COLOR);
        textArea.setForeground(isUser ? Color.WHITE : TEXT_PRIMARY);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        textArea.setOpaque(true);

        // Bubble panel (for rounded background)
        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS * 2, BORDER_RADIUS * 2);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBackground(isUser ? USER_BUBBLE_COLOR : AI_BUBBLE_COLOR);
        bubble.add(textArea, BorderLayout.CENTER);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 4, 4, 4),
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(220, 220, 220))
        ));

        // Align: user messages to right, AI to left
        if (isUser) {
            outerContainer.add(Box.createHorizontalGlue());
            outerContainer.add(Box.createHorizontalStrut(5));
            outerContainer.add(bubble);
            outerContainer.add(Box.createHorizontalStrut(10));
        } else {
            outerContainer.add(Box.createHorizontalStrut(10));
            outerContainer.add(bubble);
            outerContainer.add(Box.createHorizontalGlue());
            outerContainer.add(Box.createHorizontalStrut(5));
        }

        messagesPanel.add(outerContainer);
        messagesPanel.revalidate();
        messagesPanel.repaint();

        //  Dynamically adjust width after UI renders
        SwingUtilities.invokeLater(() -> {
            int maxWidth = scrollPane.getViewport().getWidth();
            if (maxWidth <= 0) maxWidth = 400; // fallback

            int bubbleWidth = (int) (maxWidth * (isUser ? 0.8 : 0.7));
            textArea.setSize(new Dimension(bubbleWidth, Short.MAX_VALUE));

            Dimension preferredSize = textArea.getPreferredSize();
            int finalWidth = Math.max(preferredSize.width, 50);

            textArea.setMaximumSize(new Dimension(finalWidth, Integer.MAX_VALUE));
            textArea.setPreferredSize(new Dimension(finalWidth, preferredSize.height));
            bubble.setMaximumSize(new Dimension(bubbleWidth, Integer.MAX_VALUE));

            messagesPanel.revalidate();
            messagesPanel.repaint();

            // Auto-scroll to latest message
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }


}