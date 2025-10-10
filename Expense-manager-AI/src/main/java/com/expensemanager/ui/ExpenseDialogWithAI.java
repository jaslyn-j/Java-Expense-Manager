package com.expensemanager.ui;

import com.expensemanager.utils.AIChatService;
import com.expensemanager.utils.ConfigManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ExpenseDialogWithAI extends JDialog {
    private JTextField descriptionField;
    private JTextField amountField;
    private JComboBox<String> categoryCombo;
    private JButton suggestButton;
    private JButton saveButton;
    private JButton cancelButton;
    private AIChatService aiService;
    private boolean confirmed = false;

    private static final String[] CATEGORIES = {
            "Food", "Transport", "Entertainment", "Bills",
            "Shopping", "Healthcare", "Education", "Travel", "Other"
    };

    public ExpenseDialogWithAI(Frame parent) {
        super(parent, "Add Expense", true);

        // Initialize AI service
        try {
            String apiKey = ConfigManager.getDeepseekAIKey();
            if (apiKey != null && !apiKey.isEmpty()) {
                aiService = new AIChatService(apiKey);
            }
        } catch (Exception e) {
            System.err.println("AI service unavailable: " + e.getMessage());
        }

        initComponents();
        setupLayout();
        setSize(450, 300);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        descriptionField = new JTextField(20);
        amountField = new JTextField(10);
        categoryCombo = new JComboBox<>(CATEGORIES);

        suggestButton = new JButton("🤖 AI Suggest");
        suggestButton.setToolTipText("Use AI to suggest a category");
        suggestButton.setEnabled(aiService != null);
        suggestButton.addActionListener(e -> suggestCategory());

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        mainPanel.add(descriptionField, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        mainPanel.add(amountField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        mainPanel.add(categoryCombo, gbc);
        gbc.gridx = 2;
        mainPanel.add(suggestButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // AI status indicator
        if (aiService == null) {
            JLabel statusLabel = new JLabel("⚠️ AI features disabled. Configure API key in settings.");
            statusLabel.setForeground(Color.ORANGE);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            add(statusLabel, BorderLayout.NORTH);
        }
    }

    private void suggestCategory() {
        String description = descriptionField.getText().trim();
        String amountText = amountField.getText().trim();

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a description first!",
                    "Missing Description",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount = 0;
        try {
            if (!amountText.isEmpty()) {
                amount = Double.parseDouble(amountText);
            }
        } catch (NumberFormatException e) {
            // Use 0 if amount is invalid
        }

        // Show loading state
        suggestButton.setEnabled(false);
        suggestButton.setText("Thinking...");

        final double finalAmount = amount;

        // Run AI request in background thread
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return aiService.suggestCategory(description, finalAmount);
            }

            @Override
            protected void done() {
                try {
                    String suggestedCategory = get();

                    // Find and select the suggested category
                    boolean found = false;
                    for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                        if (categoryCombo.getItemAt(i).equalsIgnoreCase(suggestedCategory)) {
                            categoryCombo.setSelectedIndex(i);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        // If exact match not found, default to "Other"
                        categoryCombo.setSelectedItem("Other");
                    }

                    JOptionPane.showMessageDialog(ExpenseDialogWithAI.this,
                            "AI suggests: " + suggestedCategory,
                            "Category Suggestion",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ExpenseDialogWithAI.this,
                            "Failed to get AI suggestion: " + e.getMessage(),
                            "AI Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    suggestButton.setEnabled(true);
                    suggestButton.setText("🤖 AI Suggest");
                }
            }
        };

        worker.execute();
    }

    private boolean validateInput() {
        if (descriptionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a description!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Amount must be greater than 0!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid amount!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // Getters
    public boolean isConfirmed() {
        return confirmed;
    }

    public String getDescription() {
        return descriptionField.getText().trim();
    }

    public double getAmount() {
        return Double.parseDouble(amountField.getText().trim());
    }

    public String getCategory() {
        return (String) categoryCombo.getSelectedItem();
    }

    // Test the dialog
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ExpenseDialogWithAI dialog = new ExpenseDialogWithAI(frame);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                System.out.println("Description: " + dialog.getDescription());
                System.out.println("Amount: " + dialog.getAmount());
                System.out.println("Category: " + dialog.getCategory());
            }
        });
    }
}