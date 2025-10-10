package com.expensemanager.ui;

import java.math.RoundingMode;
import java.util.Calendar;
import com.expensemanager.models.User;
import com.expensemanager.ui.LoginFrame;
import com.expensemanager.models.Expense;
import com.expensemanager.dao.ExpenseDAO;
import com.expensemanager.utils.ConfigManager;
import com.expensemanager.utils.AIChatService;
import com.expensemanager.utils.DatabaseUtil;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import java.awt.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.awt.Desktop;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.text.SimpleDateFormat;
import javax.swing.JScrollBar;
import javax.swing.BoxLayout;
import javax.swing.SwingWorker;
import java.awt.FontMetrics;
import java.io.File;



public class DashboardFrame extends JFrame {
    private final User currentUser;
    private final ExpenseDAO expenseDAO;
    private AIChatService chatService;

    private boolean isDarkTheme = false;

    // UI Components
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout contentLayout;

    // Theme Colors - Light
    private static final Color PRIMARY_LIGHT = Color.WHITE;
    private static final Color SECONDARY_LIGHT = new Color(245, 245, 245);
    private static final Color ACCENT_LIGHT = Color.BLACK;
    private static final Color TEXT_LIGHT = Color.BLACK;

    // Theme Colors - Dark
    private static final Color PRIMARY_DARK = Color.BLACK;
    private static final Color SECONDARY_DARK = new Color(30, 30, 30);
    private static final Color ACCENT_DARK = Color.WHITE;
    private static final Color TEXT_DARK = Color.WHITE;

    // Border Colors
    private static final Color BORDER_LIGHT = new Color(220, 220, 220);
    private static final Color BORDER_DARK = new Color(50, 50, 50);

    // Success/Error Colors
    private static final Color SUCCESS_COLOR = new Color(40, 40, 40);
    private static final Color ERROR_COLOR = new Color(60, 60, 60);

    // Current Theme Colors
    private Color primaryColor = PRIMARY_LIGHT;
    private Color secondaryColor = SECONDARY_LIGHT;
    private Color accentColor = ACCENT_LIGHT;
    private Color textColor = TEXT_LIGHT;

    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Navigation Icons
    private static final String DASHBOARD_ICON = "📊";
    private static final String EXPENSES_ICON = "💰";
    private static final String ANALYTICS_ICON = "📈";
    private static final String SETTINGS_ICON = "⚙️";
    private static final String CHAT_ICON = "💬";
    private static final String ADD_ICON = "+";
    private static final String THEME_ICON_LIGHT = "☀️";
    private static final String THEME_ICON_DARK = "🌙";

    // Category Icons
    private static final String FOOD_ICON = "🍴";
    private static final String TRANSPORT_ICON = "🚗";
    private static final String SHOPPING_ICON = "🛒";
    private static final String ENTERTAINMENT_ICON = "🎮";
    private static final String BILLS_ICON = "📄";
    private static final String OTHERS_ICON = "📦";

    // Action Icons
    private static final String EDIT_ICON = "✎";
    private static final String DELETE_ICON = "×";
    private static final String SEND_ICON = "→";
    private static final String SAVE_ICON = "✓";
    private static final String CANCEL_ICON = "✕";
    private static final String HELP_ICON = "?";
    private static final String SUCCESS_ICON = "✓";
    private static final String ERROR_ICON = "!";
    private static final String WARNING_ICON = "⚠";

    // Icons using Unicode fallbacks
    private static final String[] ICONS = {
        "📊", "💰", "📈", "⚙️", "💬", "➕", "🌞", "🌙",  // Navigation
        "🍽️", "🚗", "🛍️", "🎮", "📝", "📦",            // Categories
        "✏️", "🗑️", "➤", "💾", "❌", "❓", "✅", "❌"    // Actions
    };

    // Icon indices for easy reference
    private static final int DASHBOARD = 0, EXPENSES = 1, ANALYTICS = 2, SETTINGS = 3,
                            CHAT = 4, ADD = 5, THEME_LIGHT = 6, THEME_DARK = 7,
                            FOOD = 8, TRANSPORT = 9, SHOPPING = 10, ENTERTAINMENT = 11,
                            BILLS = 12, OTHERS = 13,
                            EDIT = 14, DELETE = 15, SEND = 16, SAVE = 17,
                            CANCEL = 18, HELP = 19, SUCCESS = 20, ERROR = 21;

    static {
        // Set system property for UTF-8 encoding
        System.setProperty("file.encoding", "UTF-8");
        try {
            // Set default font that supports emojis
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
            Font fallbackFont = new Font("Dialog", Font.PLAIN, 14);
            
            UIManager.put("Button.font", emojiFont);
            UIManager.put("Label.font", emojiFont);
            
            // Set fallback font if emoji font is not available
            if (!emojiFont.canDisplay('\u2764')) { // Simple heart character
                UIManager.put("Button.font", fallbackFont);
                UIManager.put("Label.font", fallbackFont);
            }
        } catch (Exception e) {
            System.err.println("Error setting fonts: " + e.getMessage());
        }
    }

    public DashboardFrame(User user) {
        this.currentUser = user;
        this.expenseDAO = new ExpenseDAO();

        
        // Set system property for UTF-8 encoding
        System.setProperty("file.encoding", "UTF-8");
        
        setupFrame();
        initializeComponents();
        setupAnimations();
        setVisible(true);
    }

    private Font getIconFont() {
        Font[] fonts = {
            new Font("Segoe UI Emoji", Font.PLAIN, 14),
            new Font("Apple Color Emoji", Font.PLAIN, 14),
            new Font("Noto Color Emoji", Font.PLAIN, 14),
            new Font("Segoe UI Symbol", Font.PLAIN, 14),
            new Font("Dialog", Font.PLAIN, 14)
        };
        
        for (Font font : fonts) {
            if (font.canDisplay('$') && font.canDisplay('✓')) {
                return font;
            }
        }
        return fonts[fonts.length - 1];
    }

    private void setupFrame() {
        setTitle("Modern Expense Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setBackground(primaryColor);
        
        try {
            // Set FlatLaf theme with custom defaults
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Set custom colors
            UIManager.put("Panel.background", primaryColor);
            UIManager.put("Button.background", accentColor);
            UIManager.put("Button.foreground", isDarkTheme ? PRIMARY_DARK : PRIMARY_LIGHT);
            UIManager.put("TextField.background", isDarkTheme ? SECONDARY_DARK : SECONDARY_LIGHT);
            UIManager.put("TextField.foreground", textColor);
            UIManager.put("ComboBox.background", isDarkTheme ? SECONDARY_DARK : SECONDARY_LIGHT);
            UIManager.put("ComboBox.foreground", textColor);
            UIManager.put("Label.foreground", textColor);
            UIManager.put("ScrollPane.background", primaryColor);
            UIManager.put("TableHeader.background", accentColor);
            UIManager.put("TableHeader.foreground", isDarkTheme ? PRIMARY_DARK : PRIMARY_LIGHT);
            
            // Set rounded corners
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            
            // Update UI
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        // Main Panel with Border Layout
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(primaryColor);

        // Create Content Panel with Card Layout
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(secondaryColor);

        // Create Different Views
        createDashboardView();
        createExpensesView();
        createBudgetsView();
        createAnalyticsView();
        createSettingsView();
        createChatView();

        // Create Sidebar
        createSidebar();

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Show dashboard by default
        contentLayout.show(contentPanel, "dashboard");

        add(mainPanel);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBackground(primaryColor);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // Logo and Welcome Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(primaryColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Logo
        JLabel logoLabel = new JLabel("Expense Manager");
        logoLabel.setFont(TITLE_FONT);
        logoLabel.setForeground(accentColor);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(REGULAR_FONT);
        welcomeLabel.setForeground(new Color(128, 128, 128));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        topPanel.add(logoLabel);
        topPanel.add(welcomeLabel);

        // Navigation Panel
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(primaryColor);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addNavButton("dashboard", navPanel);
        addNavButton("expenses", navPanel);
        addNavButton("budgets", navPanel);
        addNavButton("analytics", navPanel);
        addNavButton("chat", navPanel);
        addNavButton("settings", navPanel);

        // Theme Toggle and Sign Out Panel (at the bottom)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(primaryColor);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 20, 0));


        // Theme Toggle Panel
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        themePanel.setBackground(primaryColor);
        createThemeToggle(themePanel);

        JButton signOutButton = createSignOutButton();
        signOutButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel.add(themePanel);
        bottomPanel.add(signOutButton);

        sidebarPanel.add(topPanel, BorderLayout.NORTH);
        sidebarPanel.add(navPanel, BorderLayout.CENTER);
        sidebarPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addNavButton(String command, JPanel container) {
        String icon;
        switch (command) {
            case "dashboard": icon = getIcon(DASHBOARD); break;
            case "expenses": icon = getIcon(EXPENSES); break;
            case "analytics": icon = getIcon(ANALYTICS); break;
            case "budgets": icon = "💰"; break;
            case "chat": icon = getIcon(CHAT); break;
            case "settings": icon = getIcon(SETTINGS); break;
            default: icon = getIcon(ADD); break;
        }
        String text = command.substring(0, 1).toUpperCase() + command.substring(1);
        JButton button = createIconButton(icon + " " + text);
        button.setForeground(textColor);
        button.setBackground(primaryColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(secondaryColor);
                button.setForeground(accentColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primaryColor);
                button.setForeground(textColor);
            }
        });

        button.addActionListener(e -> contentLayout.show(contentPanel, command));
        container.add(button);
        container.add(Box.createVerticalStrut(5));
    }

    private String getIcon(int index) {
        try {
            return ICONS[index];
        } catch (Exception e) {
            switch (index) {
                case DASHBOARD: return "[D]";
                case EXPENSES: return "[$]";
                case ANALYTICS: return "[A]";
                case SETTINGS: return "[S]";
                case CHAT: return "[C]";
                case ADD: return "[+]";
                case EDIT: return "[E]";
                case DELETE: return "[X]";
                default: return "[*]";
            }
        }
    }

    private void createDashboardView() {
        JPanel dashboardPanel = new JPanel(new BorderLayout(20, 20));
        dashboardPanel.setBackground(secondaryColor);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = createHeaderPanel("Dashboard Overview");

        // Quick Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(secondaryColor);

        try {
            BigDecimal totalExpenses = expenseDAO.getTotalExpenses(currentUser.getId());
            BigDecimal monthlyAverage = expenseDAO.getMonthlyAverage(currentUser.getId());
            String topCategory = expenseDAO.getTopCategory(currentUser.getId());

            addStatCard("Total Expenses", String.format("$%.2f", totalExpenses), "📈", statsPanel);
            addStatCard("Monthly Average", String.format("$%.2f", monthlyAverage), "📊", statsPanel);
            addStatCard("Top Category", topCategory, "🍽", statsPanel);

        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback to show error or default values
            addStatCard("Total Expenses", "Error loading", "❌", statsPanel);
            addStatCard("Monthly Average", "Error loading", "❌", statsPanel);
            addStatCard("Top Category", "Error loading", "❌", statsPanel);
        }
        // Charts Panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(secondaryColor);
        addExpenseChart(chartsPanel);
        addCategoryChart(chartsPanel);

        // Recent Transactions
        JPanel recentPanel = createRecentTransactionsPanel();

        // Layout
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(secondaryColor);
        mainContent.add(statsPanel, BorderLayout.NORTH);
        mainContent.add(chartsPanel, BorderLayout.CENTER);
        mainContent.add(recentPanel, BorderLayout.SOUTH);

        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        dashboardPanel.add(mainContent, BorderLayout.CENTER);

        contentPanel.add(dashboardPanel, "dashboard");
    }

    private JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);

        JButton addButton = new JButton(ADD_ICON + " Add Expense");
        addButton.setFont(REGULAR_FONT);
        addButton.setBackground(accentColor);
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddExpenseDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void addStatCard(String title, String value, String trend, JPanel container) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(isDarkTheme ? SECONDARY_DARK : SECONDARY_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, isDarkTheme ? BORDER_DARK : BORDER_LIGHT),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(REGULAR_FONT);
        titleLabel.setForeground(textColor);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(TITLE_FONT);
        valueLabel.setForeground(accentColor);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        valuePanel.setBackground(isDarkTheme ? SECONDARY_DARK : SECONDARY_LIGHT);
        valuePanel.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);

        container.add(card);
    }

    private void addExpenseChart(JPanel container) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        try {
            // Get real expense data
            Map<String, BigDecimal> categoryTotals = expenseDAO.getExpensesByCategory(currentUser.getId());
            categoryTotals.forEach((category, amount) -> 
                dataset.setValue(category, amount.doubleValue())
            );
        } catch (SQLException e) {
            e.printStackTrace();
            // Add sample data as fallback
            dataset.setValue("Food", 35);
            dataset.setValue("Transport", 25);
            dataset.setValue("Shopping", 20);
            dataset.setValue("Others", 20);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Expense Distribution",
            dataset,
            true,
            true,
            false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(primaryColor);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(primaryColor);
        wrapper.setBorder(new RoundedBorder(10, new Color(230, 230, 230)));
        wrapper.add(chartPanel);
        
        container.add(wrapper);
    }

    private void addCategoryChart(JPanel container) {
        // Similar to addExpenseChart but with different chart type
        // Implementation omitted for brevity
    }

    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(primaryColor);
        panel.setBorder(new RoundedBorder(10, new Color(230, 230, 230)));

        JLabel titleLabel = new JLabel("Recent Transactions");
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setBorder(new EmptyBorder(20, 20, 10, 20));

        // Create table with real data
        String[] columns = {"Date", "Category", "Description", "Amount", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };

        try {
            List<Map<String, Object>> recentExpenses = expenseDAO.getRecentExpenses(currentUser.getId(), 10);
            for (Map<String, Object> expense : recentExpenses) {
                model.addRow(new Object[]{
                    expense.get("date"),
                    expense.get("category") + " " + getCategoryIcon((String)expense.get("category")),
                    expense.get("description"),
                    String.format("$%.2f", expense.get("amount")),
                    expense.get("id") // Store ID for actions
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error loading recent transactions: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setBackground(primaryColor);
        table.getTableHeader().setFont(REGULAR_FONT.deriveFont(Font.BOLD));

        // Set custom renderer for the actions column
        table.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

                JButton editButton = new JButton("Edit");
                JButton deleteButton = new JButton("Delete");
                styleActionButton(editButton);
                styleActionButton(deleteButton);

                panel.add(editButton);
                panel.add(deleteButton);
                return panel;
            }
        });

        // Set custom editor for the actions column
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private Object cellValue;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                this.cellValue = value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                panel.setBackground(table.getSelectionBackground());

                JButton editButton = new JButton("Edit");
                JButton deleteButton = new JButton("Delete");
                styleActionButton(editButton);
                styleActionButton(deleteButton);

                editButton.addActionListener(e -> {
                    int expenseId = (Integer) cellValue;
                    editExpense(createExpenseFromRow(table, row, expenseId));
                    fireEditingStopped();
                });

                deleteButton.addActionListener(e -> {
                    int expenseId = (Integer) cellValue;
                    deleteExpense(createExpenseFromRow(table, row, expenseId));
                    fireEditingStopped();
                });

                panel.add(editButton);
                panel.add(deleteButton);
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return cellValue;
            }
        });

        // Adjust column widths
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(primaryColor);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(0, 0, 0, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
    }

    private Expense createExpenseFromRow(JTable table, int row, int id) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setUserId(currentUser.getId());
        expense.setDate(LocalDate.parse(table.getValueAt(row, 0).toString()));
        String category = table.getValueAt(row, 1).toString().split(" ")[0]; // Remove icon
        expense.setCategoryName(category);
        expense.setDescription(table.getValueAt(row, 2).toString());
        String amountStr = table.getValueAt(row, 3).toString().replace("$", "");
        expense.setAmount(new BigDecimal(amountStr));
        return expense;
    }

    private String getCategoryIcon(String category) {
        String lowerCategory = category.toLowerCase();
        switch (lowerCategory) {
            case "food": return getIcon(FOOD);
            case "transport": return getIcon(TRANSPORT);
            case "shopping": return getIcon(SHOPPING);
            case "entertainment": return getIcon(ENTERTAINMENT);
            case "bills": return getIcon(BILLS);
            default: return getIcon(OTHERS);
        }
    }

    private void createExpensesView() {
        JPanel expensesPanel = new JPanel(new BorderLayout(20, 20));
        expensesPanel.setBackground(secondaryColor);
        expensesPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header with search controls
        JPanel headerPanel = createExpensesHeaderPanel();

        // Create main content panel
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(secondaryColor);

        // Filter controls panel
        JPanel filterPanel = createFilterPanel();

        // Scrollable panel for month sections
        JPanel monthsContainer = new JPanel();
        monthsContainer.setLayout(new BoxLayout(monthsContainer, BoxLayout.Y_AXIS));
        monthsContainer.setBackground(secondaryColor);

        JScrollPane scrollPane = new JScrollPane(monthsContainer);
        scrollPane.setBorder(new RoundedBorder(10, new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(secondaryColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Store references for filtering
        this.monthsContainer = monthsContainer;
        this.expensesScrollPane = scrollPane;

        // Load and display expenses by month
        loadExpensesByMonth(monthsContainer, null); // null = show all categories

        mainContent.add(filterPanel, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        expensesPanel.add(headerPanel, BorderLayout.NORTH);
        expensesPanel.add(mainContent, BorderLayout.CENTER);

        contentPanel.add(expensesPanel, "expenses");
    }

    // Add these as instance variables at the top of your class
    private JPanel monthsContainer;
    private JScrollPane expensesScrollPane;
    private JComboBox<String> categoryFilter;

    private JPanel createExpensesHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("All Expenses");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);

        JButton addButton = new JButton("+ Add Expense");
        addButton.setFont(REGULAR_FONT);
        addButton.setBackground(accentColor);
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddExpenseDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(secondaryColor);
        filterPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Category filter
        JLabel filterLabel = new JLabel("Filter by Category:");
        filterLabel.setFont(REGULAR_FONT);
        filterLabel.setForeground(textColor);

        String[] categories = {"All Categories", "Food", "Transport", "Shopping",
                "Entertainment", "Bills", "Healthcare", "Education",
                "Housing", "Travel", "Others"};
        categoryFilter = new JComboBox<>(categories);
        categoryFilter.setFont(REGULAR_FONT);
        categoryFilter.setBackground(primaryColor);
        categoryFilter.setForeground(textColor);
        categoryFilter.addActionListener(e -> {
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            String filterCategory = "All Categories".equals(selectedCategory) ? null : selectedCategory;
            loadExpensesByMonth(monthsContainer, filterCategory);
        });

        // Search field
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(REGULAR_FONT);
        searchLabel.setForeground(textColor);

        JTextField searchField = new JTextField(15);
        searchField.setFont(REGULAR_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(5, new Color(230, 230, 230)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // Add search functionality with delay
        Timer searchTimer = new Timer(500, null);
        searchTimer.setRepeats(false);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchTimer.stop();
                searchTimer.addActionListener(evt -> {
                    String searchText = searchField.getText().trim();
                    String selectedCategory = (String) categoryFilter.getSelectedItem();
                    String filterCategory = "All Categories".equals(selectedCategory) ? null : selectedCategory;
                    loadExpensesByMonth(monthsContainer, filterCategory, searchText);
                });
                searchTimer.start();
            }
        });

        filterPanel.add(filterLabel);
        filterPanel.add(categoryFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(searchLabel);
        filterPanel.add(searchField);

        return filterPanel;
    }

    private void loadExpensesByMonth(JPanel container, String categoryFilter) {
        loadExpensesByMonth(container, categoryFilter, "");
    }

    private void loadExpensesByMonth(JPanel container, String categoryFilter, String searchText) {
        container.removeAll();

        try {
            // Get expenses with optional filtering
            List<Map<String, Object>> expenses = getFilteredExpenses(categoryFilter, searchText);

            // Group expenses by month-year
            Map<String, List<Map<String, Object>>> expensesByMonth = new LinkedHashMap<>();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");

            for (Map<String, Object> expense : expenses) {
                LocalDate date = (LocalDate) expense.get("date");
                String monthKey = monthFormat.format(Date.from(date.atStartOfDay()
                        .atZone(ZoneId.systemDefault()).toInstant()));

                expensesByMonth.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(expense);
            }

            if (expensesByMonth.isEmpty()) {
                JLabel noDataLabel = new JLabel("No expenses found");
                noDataLabel.setFont(REGULAR_FONT);
                noDataLabel.setForeground(new Color(128, 128, 128));
                noDataLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                container.add(noDataLabel);
            } else {
                // Create sections for each month
                boolean isFirst = true;
                for (Map.Entry<String, List<Map<String, Object>>> monthEntry : expensesByMonth.entrySet()) {
                    if (!isFirst) {
                        container.add(Box.createVerticalStrut(30));
                    }
                    container.add(createMonthSection(monthEntry.getKey(), monthEntry.getValue()));
                    isFirst = false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error loading expenses: " + e.getMessage());
        }

        container.revalidate();
        container.repaint();
    }

    private List<Map<String, Object>> getFilteredExpenses(String categoryFilter, String searchText) throws SQLException {
        String sql = "SELECT e.id, e.amount, c.name as category, e.date, e.description " +
                "FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id " +
                "WHERE e.user_id = ?";

        List<Object> parameters = new ArrayList<>();
        parameters.add(currentUser.getId());

        if (categoryFilter != null && !categoryFilter.trim().isEmpty()) {
            sql += " AND c.name = ?";
            parameters.add(categoryFilter);
        }

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql += " AND (e.description LIKE ? OR c.name LIKE ?)";
            String searchPattern = "%" + searchText + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        sql += " ORDER BY e.date DESC, e.id DESC";

        List<Map<String, Object>> expenses = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> expense = new HashMap<>();
                    expense.put("id", rs.getInt("id"));
                    expense.put("amount", rs.getBigDecimal("amount"));
                    expense.put("category", rs.getString("category"));
                    expense.put("date", rs.getDate("date").toLocalDate());
                    expense.put("description", rs.getString("description"));
                    expenses.add(expense);
                }
            }
        }

        return expenses;
    }

    private JPanel createMonthSection(String monthYear, List<Map<String, Object>> expenses) {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 15));
        sectionPanel.setBackground(secondaryColor);
        sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Month header with total
        BigDecimal monthTotal = expenses.stream()
                .map(e -> (BigDecimal) e.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, new Color(230, 230, 230)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel monthLabel = new JLabel(monthYear);
        monthLabel.setFont(SUBTITLE_FONT);
        monthLabel.setForeground(textColor);

        JLabel totalLabel = new JLabel(String.format("Total: $%.2f (%d expenses)",
                monthTotal, expenses.size()));
        totalLabel.setFont(REGULAR_FONT);
        totalLabel.setForeground(new Color(128, 128, 128));

        headerPanel.add(monthLabel, BorderLayout.WEST);
        headerPanel.add(totalLabel, BorderLayout.EAST);

        // Expenses table for this month
        String[] columns = {"Date", "Category", "Description", "Amount", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        for (Map<String, Object> expense : expenses) {
            model.addRow(new Object[]{
                    expense.get("date"),
                    expense.get("category") + " " + getCategoryIcon((String)expense.get("category")),
                    expense.get("description"),
                    String.format("$%.2f", expense.get("amount")),
                    expense.get("id")
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setBackground(primaryColor);
        table.getTableHeader().setFont(REGULAR_FONT.deriveFont(Font.BOLD));

        // Setup actions column
        setupActionsColumn(table);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new RoundedBorder(8, new Color(230, 230, 230)));
        tableScroll.getViewport().setBackground(primaryColor);
        tableScroll.setPreferredSize(new Dimension(0, Math.min(300, (expenses.size() + 1) * 35 + 20)));

        sectionPanel.add(headerPanel, BorderLayout.NORTH);
        sectionPanel.add(tableScroll, BorderLayout.CENTER);

        return sectionPanel;
    }

    private void setupActionsColumn(JTable table) {
        // Copy the actions column setup from your createRecentTransactionsPanel() method
        table.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

                JButton editButton = new JButton("Edit");
                JButton deleteButton = new JButton("Delete");
                styleActionButton(editButton);
                styleActionButton(deleteButton);

                panel.add(editButton);
                panel.add(deleteButton);
                return panel;
            }
        });

        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private Object cellValue;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                this.cellValue = value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                panel.setBackground(table.getSelectionBackground());

                JButton editButton = new JButton("Edit");
                JButton deleteButton = new JButton("Delete");
                styleActionButton(editButton);
                styleActionButton(deleteButton);

                editButton.addActionListener(e -> {
                    int expenseId = (Integer) cellValue;
                    editExpense(createExpenseFromRow(table, row, expenseId));
                    fireEditingStopped();
                });

                deleteButton.addActionListener(e -> {
                    int expenseId = (Integer) cellValue;
                    deleteExpense(createExpenseFromRow(table, row, expenseId));
                    fireEditingStopped();
                });

                panel.add(editButton);
                panel.add(deleteButton);
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return cellValue;
            }
        });

        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);
    }

    private void createAnalyticsView() {
        JPanel analyticsPanel = new JPanel(new BorderLayout(20, 20));
        analyticsPanel.setBackground(secondaryColor);
        analyticsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = createAnalyticsHeaderPanel();

        // Controls Panel
        JPanel controlsPanel = createAnalyticsControlsPanel();

        // Charts Container
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsContainer.setBackground(secondaryColor);

        // Store reference for updating charts
        this.analyticsChartsContainer = chartsContainer;

        // Load initial charts for current month
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        updateAnalyticsCharts(currentMonth);

        analyticsPanel.add(headerPanel, BorderLayout.NORTH);
        analyticsPanel.add(controlsPanel, BorderLayout.CENTER);
        analyticsPanel.add(chartsContainer, BorderLayout.SOUTH);

        contentPanel.add(analyticsPanel, "analytics");
    }

    // Add this as instance variable
    private JPanel analyticsChartsContainer;
    private JComboBox<String> monthYearCombo;

    private JPanel createAnalyticsHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Expense Analytics");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createAnalyticsControlsPanel() {
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.setBackground(secondaryColor);
        controlsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(REGULAR_FONT);
        monthLabel.setForeground(textColor);

        // Generate month-year options for the last 12 months
        String[] monthOptions = generateMonthOptions();
        monthYearCombo = new JComboBox<>(monthOptions);
        monthYearCombo.setFont(REGULAR_FONT);
        monthYearCombo.setBackground(primaryColor);
        monthYearCombo.setForeground(textColor);

        monthYearCombo.addActionListener(e -> {
            String selectedMonth = (String) monthYearCombo.getSelectedItem();
            if (selectedMonth != null) {
                LocalDate monthDate = parseMonthYear(selectedMonth);
                updateAnalyticsCharts(monthDate);
            }
        });

        controlsPanel.add(monthLabel);
        controlsPanel.add(monthYearCombo);

        return controlsPanel;
    }

    private String[] generateMonthOptions() {
        List<String> options = new ArrayList<>();
        LocalDate current = LocalDate.now().withDayOfMonth(1);
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");

        // Add current month and 11 previous months
        for (int i = 0; i < 12; i++) {
            LocalDate month = current.minusMonths(i);
            String monthStr = formatter.format(Date.from(month.atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            options.add(monthStr);
        }

        return options.toArray(new String[0]);
    }

    private LocalDate parseMonthYear(String monthYear) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
            Date date = formatter.parse(monthYear);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
        } catch (Exception e) {
            return LocalDate.now().withDayOfMonth(1);
        }
    }

    private void updateAnalyticsCharts(LocalDate selectedMonth) {
        analyticsChartsContainer.removeAll();

        try {
            // Get expense data for the selected month
            LocalDate startDate = selectedMonth.withDayOfMonth(1);
            LocalDate endDate = selectedMonth.withDayOfMonth(selectedMonth.lengthOfMonth());

            Map<String, BigDecimal> categoryData = expenseDAO.getExpensesByDateRange(
                    currentUser.getId(), startDate, endDate);

            if (categoryData.isEmpty()) {
                // Show "No data" message
                JPanel noDataPanel = new JPanel(new BorderLayout());
                noDataPanel.setBackground(primaryColor);
                noDataPanel.setBorder(new RoundedBorder(10, new Color(230, 230, 230)));

                JLabel noDataLabel = new JLabel("No expenses found for " +
                        selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                noDataLabel.setFont(REGULAR_FONT);
                noDataLabel.setForeground(new Color(128, 128, 128));
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);

                noDataPanel.add(noDataLabel, BorderLayout.CENTER);
                analyticsChartsContainer.add(noDataPanel);
                analyticsChartsContainer.add(new JPanel()); // Empty panel for grid layout
            } else {
                // Create pie chart
                JPanel pieChartPanel = createCategoryPieChart(categoryData, selectedMonth);

                // Create bar chart
                JPanel barChartPanel = createCategoryBarChart(categoryData, selectedMonth);

                analyticsChartsContainer.add(pieChartPanel);
                analyticsChartsContainer.add(barChartPanel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error loading analytics data: " + e.getMessage());
        }

        analyticsChartsContainer.revalidate();
        analyticsChartsContainer.repaint();
    }

    private JPanel createCategoryPieChart(Map<String, BigDecimal> categoryData, LocalDate month) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        categoryData.forEach((category, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue(category, amount.doubleValue());
            }
        });

        String title = "Expense Distribution - " + month.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        JFreeChart chart = ChartFactory.createPieChart("", dataset, false, true, false);

        // Create modern styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(20, 20, 20, 20));

        PiePlot plot = (PiePlot) chart.getPlot();

        // Create donut effect
        plot.setCircular(true);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}",
                NumberFormat.getNumberInstance(), new DecimalFormat("0.0%")));
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelPaint(Color.BLACK);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(Color.LIGHT_GRAY);
        plot.setLabelShadowPaint(null);

        // Modern color palette
        Color[] colors = {
                new Color(231, 76, 60),    // Red
                new Color(52, 152, 219),   // Blue
                new Color(46, 204, 113),   // Green
                new Color(155, 89, 182),   // Purple
                new Color(241, 196, 15),   // Yellow
                new Color(230, 126, 34),   // Orange
                new Color(149, 165, 166),  // Gray
                new Color(26, 188, 156),   // Turquoise
                new Color(231, 76, 60),    // Pink
                new Color(52, 73, 94)      // Dark Blue
        };

        int colorIndex = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) key, colors[colorIndex % colors.length]);
            plot.setSectionOutlinePaint((Comparable) key, Color.WHITE);
            plot.setSectionOutlineStroke((Comparable) key, new BasicStroke(2.0f));
            colorIndex++;
        }

        // Remove default styling
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);

        // Add title manually with better positioning
        TextTitle chartTitle = new TextTitle(title);
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chartTitle.setPaint(new Color(44, 62, 80));
        chartTitle.setPosition(RectangleEdge.TOP);
        chartTitle.setPadding(0, 0, 20, 0);
        chart.setTitle(chartTitle);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(400, 350));
        chartPanel.setBorder(null);

        // Create wrapper with shadow effect
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createCategoryBarChart(Map<String, BigDecimal> categoryData, LocalDate month) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        categoryData.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEach(entry -> {
                    if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                        dataset.addValue(entry.getValue().doubleValue(), "Amount", entry.getKey());
                    }
                });

        String title = "Category Comparison - " + month.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        JFreeChart chart = ChartFactory.createBarChart(
                "",  // Empty title, we'll add it manually
                "",  // Empty category axis label
                "Amount ($)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Modern chart styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(20, 20, 20, 20));

        CategoryPlot plot = chart.getCategoryPlot();

        // Clean background
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setOutlineVisible(false);

        // Modern bar renderer
        BarRenderer renderer = new BarRenderer();

        // Gradient colors for bars
        GradientPaint[] gradients = {
                new GradientPaint(0, 0, new Color(231, 76, 60), 0, 300, new Color(192, 57, 43)),
                new GradientPaint(0, 0, new Color(52, 152, 219), 0, 300, new Color(41, 128, 185)),
                new GradientPaint(0, 0, new Color(46, 204, 113), 0, 300, new Color(39, 174, 96)),
                new GradientPaint(0, 0, new Color(155, 89, 182), 0, 300, new Color(142, 68, 173)),
                new GradientPaint(0, 0, new Color(241, 196, 15), 0, 300, new Color(243, 156, 18)),
                new GradientPaint(0, 0, new Color(230, 126, 34), 0, 300, new Color(211, 84, 0))
        };

        for (int i = 0; i < dataset.getColumnCount(); i++) {
            renderer.setSeriesPaint(0, gradients[i % gradients.length]);
        }

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);

        plot.setRenderer(renderer);

        // Style axes
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setLabelPaint(new Color(44, 62, 80));
        domainAxis.setTickLabelPaint(new Color(127, 140, 141));
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setLabelPaint(new Color(44, 62, 80));
        rangeAxis.setTickLabelPaint(new Color(127, 140, 141));
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(new DecimalFormat("$#,##0"));

        // Add title
        TextTitle chartTitle = new TextTitle(title);
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chartTitle.setPaint(new Color(44, 62, 80));
        chartTitle.setPosition(RectangleEdge.TOP);
        chartTitle.setPadding(0, 0, 20, 0);
        chart.setTitle(chartTitle);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(400, 350));
        chartPanel.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private void createBudgetsView() {
        JPanel budgetsPanel = new JPanel(new BorderLayout(20, 20));
        budgetsPanel.setBackground(secondaryColor);
        budgetsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = createBudgetsHeaderPanel();

        // Main content
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(secondaryColor);

        // Budgets container
        JPanel budgetsContainer = new JPanel();
        budgetsContainer.setLayout(new BoxLayout(budgetsContainer, BoxLayout.Y_AXIS));
        budgetsContainer.setBackground(secondaryColor);

        JScrollPane scrollPane = new JScrollPane(budgetsContainer);
        scrollPane.setBorder(new RoundedBorder(10, isDarkTheme ? new Color(50, 50, 50) : new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(secondaryColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Load budgets
        loadBudgets(budgetsContainer);

        mainContent.add(scrollPane, BorderLayout.CENTER);
        budgetsPanel.add(headerPanel, BorderLayout.NORTH);
        budgetsPanel.add(mainContent, BorderLayout.CENTER);

        contentPanel.add(budgetsPanel, "budgets");
    }

    private JPanel createBudgetsHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);

        JButton addButton = new JButton("+ Create Budget");
        addButton.setFont(REGULAR_FONT);
        addButton.setBackground(accentColor);
        addButton.setForeground(isDarkTheme ? primaryColor : Color.WHITE);
        addButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddBudgetDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void loadBudgets(JPanel container) {
        container.removeAll();

        try {
            List<Map<String, Object>> budgets = expenseDAO.getBudgetsWithSpending(currentUser.getId());

            if (budgets.isEmpty()) {
                JLabel noDataLabel = new JLabel("No budgets set. Create your first budget to start tracking!");
                noDataLabel.setFont(REGULAR_FONT);
                noDataLabel.setForeground(new Color(128, 128, 128));
                noDataLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                container.add(noDataLabel);
            } else {
                for (Map<String, Object> budget : budgets) {
                    container.add(createBudgetCard(budget));
                    container.add(Box.createVerticalStrut(15));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error loading budgets: " + e.getMessage());
        }

        container.revalidate();
        container.repaint();
    }

    private JPanel createBudgetCard(Map<String, Object> budget) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(primaryColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, isDarkTheme ? new Color(50, 50, 50) : new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Left side - Category info
        JPanel leftPanel = new JPanel(new BorderLayout(10, 5));
        leftPanel.setBackground(primaryColor);

        String categoryName = (String) budget.get("category_name");
        JLabel categoryLabel = new JLabel(categoryName + " " + getCategoryIcon(categoryName));
        categoryLabel.setFont(SUBTITLE_FONT);
        categoryLabel.setForeground(textColor);

        LocalDate startDate = (LocalDate) budget.get("start_date");
        LocalDate endDate = (LocalDate) budget.get("end_date");
        JLabel dateLabel = new JLabel(String.format("%s - %s",
                startDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        dateLabel.setFont(REGULAR_FONT);
        dateLabel.setForeground(new Color(128, 128, 128));

        leftPanel.add(categoryLabel, BorderLayout.NORTH);
        leftPanel.add(dateLabel, BorderLayout.CENTER);

        // Center - Progress bar and amounts
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(primaryColor);

        BigDecimal budgetAmount = (BigDecimal) budget.get("budget_amount");
        BigDecimal spentAmount = (BigDecimal) budget.get("spent_amount");
        BigDecimal remaining = budgetAmount.subtract(spentAmount);
        double percentage = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                ? spentAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue()
                : 0;

        // Amounts panel
        JPanel amountsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        amountsPanel.setBackground(primaryColor);

        amountsPanel.add(createAmountLabel("Budget", String.format("$%.2f", budgetAmount)));
        amountsPanel.add(createAmountLabel("Spent", String.format("$%.2f", spentAmount)));
        amountsPanel.add(createAmountLabel("Remaining", String.format("$%.2f", remaining)));

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", percentage));
        progressBar.setPreferredSize(new Dimension(0, 25));

        // Color code the progress bar
        if (percentage >= 100) {
            progressBar.setForeground(new Color(231, 76, 60)); // Red - over budget
        } else if (percentage >= 80) {
            progressBar.setForeground(new Color(241, 196, 15)); // Yellow - warning
        } else {
            progressBar.setForeground(new Color(46, 204, 113)); // Green - good
        }

        centerPanel.add(amountsPanel, BorderLayout.NORTH);
        centerPanel.add(progressBar, BorderLayout.CENTER);

        // Right side - Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(REGULAR_FONT);
        deleteButton.setForeground(new Color(231, 76, 60));
        deleteButton.setBackground(primaryColor);
        deleteButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> {
            int budgetId = (int) budget.get("id");
            deleteBudget(budgetId);
        });

        card.add(leftPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(deleteButton, BorderLayout.EAST);

        return card;
    }

    private JPanel createAmountLabel(String label, String amount) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setBackground(primaryColor);

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelText.setForeground(new Color(128, 128, 128));

        JLabel amountText = new JLabel(amount);
        amountText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        amountText.setForeground(textColor);

        panel.add(labelText);
        panel.add(amountText);

        return panel;
    }

    private void showAddBudgetDialog() {
        JDialog dialog = new JDialog(this, "Create Budget", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(primaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Category Field
        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills",
                "Healthcare", "Education", "Housing", "Travel", "Others"};
        JComboBox<String> categoryBox = new JComboBox<>(categories);
        addFormField(formPanel, categoryLabel, categoryBox, gbc, 0);

        // Amount Field
        JLabel amountLabel = new JLabel("Budget Amount:");
        JTextField amountField = new JTextField();
        addFormField(formPanel, amountLabel, amountField, gbc, 1);

        // Start Date
        JLabel startLabel = new JLabel("Start Date:");
        JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd");
        startSpinner.setEditor(startEditor);
        addFormField(formPanel, startLabel, startSpinner, gbc, 2);

        // End Date
        JLabel endLabel = new JLabel("End Date:");
        JSpinner endSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd");
        endSpinner.setEditor(endEditor);
        // Set default end date to 30 days from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        endSpinner.setValue(cal.getTime());
        addFormField(formPanel, endLabel, endSpinner, gbc, 3);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(primaryColor);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(200, 200, 200));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = new JButton("Create Budget");
        styleButton(saveButton, accentColor);
        saveButton.addActionListener(e -> {
            try {
                String category = (String) categoryBox.getSelectedItem();
                BigDecimal amount = new BigDecimal(amountField.getText());
                LocalDate startDate = ((Date) startSpinner.getValue()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate endDate = ((Date) endSpinner.getValue()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                if (endDate.isBefore(startDate)) {
                    showErrorMessage("End date must be after start date");
                    return;
                }

                expenseDAO.createBudget(currentUser.getId(), category, amount, startDate, endDate);
                dialog.dispose();

                // Refresh budgets view
                refreshBudgetsView();
                showSuccessMessage("Budget created successfully!");
            } catch (NumberFormatException ex) {
                showErrorMessage("Please enter a valid amount");
            } catch (Exception ex) {
                showErrorMessage("Error creating budget: " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteBudget(int budgetId) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this budget?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                expenseDAO.deleteBudget(budgetId);
                refreshBudgetsView();
                showSuccessMessage("Budget deleted successfully!");
            } catch (Exception e) {
                showErrorMessage("Error deleting budget: " + e.getMessage());
            }
        }
    }

    private void refreshBudgetsView() {
        contentPanel.removeAll();
        createDashboardView();
        createExpensesView();
        createBudgetsView();  // Add this line
        createAnalyticsView();
        createSettingsView();
        createChatView();
        contentLayout.show(contentPanel, "budgets");
        revalidate();
        repaint();
    }

    private JButton createSignOutButton() {
        JButton signOutButton = new JButton("Sign Out");
        signOutButton.setFont(REGULAR_FONT);
        signOutButton.setForeground(new Color(231, 76, 60)); // Red color for sign out
        signOutButton.setBackground(primaryColor);
        signOutButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        signOutButton.setHorizontalAlignment(SwingConstants.LEFT);
        signOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signOutButton.setFocusPainted(false);
        signOutButton.setContentAreaFilled(false);

        signOutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signOutButton.setBackground(new Color(231, 76, 60, 30));
                signOutButton.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signOutButton.setOpaque(false);
                signOutButton.setBackground(primaryColor);
            }
        });

        signOutButton.addActionListener(e -> handleSignOut());

        return signOutButton;
    }

    private void handleSignOut() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Close current window
            this.dispose();

            // Open login window
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }

    private void createSettingsView() {
        JPanel settingsPanel = new JPanel(new BorderLayout(20, 20));
        settingsPanel.setBackground(secondaryColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = createHeaderPanel("Settings");

        // Settings Content
        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 0, 20));
        contentPanel.setBackground(secondaryColor);

        // Theme Settings
        JPanel themeSection = createSettingsSection("Theme",
            "Choose between light and dark theme",
            createThemeTogglePanel());


        // Export Settings
        JPanel exportSection = createSettingsSection("Export Data",
            "Export your expense data",
            createExportPanel());

        // Add sections to content
        contentPanel.add(themeSection);
        contentPanel.add(exportSection);

        // Add scroll support
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(secondaryColor);
        scrollPane.getViewport().setBackground(secondaryColor);

        settingsPanel.add(headerPanel, BorderLayout.NORTH);
        settingsPanel.add(scrollPane, BorderLayout.CENTER);

        this.contentPanel.add(settingsPanel, "settings");
    }

    private JPanel createSettingsSection(String title, String description, JComponent control) {
        JPanel section = new JPanel(new BorderLayout(10, 5));
        section.setBackground(primaryColor);
        section.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, new Color(230, 230, 230)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Title and description
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        textPanel.setBackground(primaryColor);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setForeground(textColor);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(REGULAR_FONT);
        descLabel.setForeground(new Color(128, 128, 128));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        section.add(textPanel, BorderLayout.CENTER);
        section.add(control, BorderLayout.EAST);

        return section;
    }

    private JPanel createThemeTogglePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(primaryColor);
        createThemeToggle(panel);
        return panel;
    }



    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(primaryColor);

    JButton exportButton = new JButton("Export as PDF");
        styleButton(exportButton, accentColor);
        exportButton.addActionListener(e -> exportData());

        panel.add(exportButton);
        return panel;
    }

    private void exportData() {
        try {
            java.util.List<com.expensemanager.models.Expense> expenses = expenseDAO.getAllExpensesWithCategory(currentUser.getId());
            org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            document.addPage(page);
            org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);

            // Title
            contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 770);
            contentStream.showText("Expense Report for " + currentUser.getUsername());
            contentStream.endText();

            // Export date
            String exportDate = java.time.LocalDate.now().toString();
            contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 755);
            contentStream.showText("Exported on: " + exportDate);
            contentStream.endText();

            // Table header
            int y = 730;
            int rowHeight = 20;
            int startX = 50;
            int[] colWidths = {90, 90, 70, 220}; // Date, Category, Amount, Description
            String[] headers = {"Date", "Category", "Amount", "Description"};

            contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            int x = startX;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(headers[i]);
                contentStream.endText();
                x += colWidths[i];
            }

            // Draw table rows
            y -= rowHeight;
            contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 11);
            for (com.expensemanager.models.Expense exp : expenses) {
                x = startX;
                String[] row = {
                    exp.getDate() != null ? exp.getDate().toString() : "",
                    exp.getCategoryName() != null ? exp.getCategoryName() : "-",
                    exp.getAmount() != null ? exp.getAmount().toString() : "",
                    exp.getDescription() != null ? exp.getDescription() : ""
                };
                for (int i = 0; i < row.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(row[i]);
                    contentStream.endText();
                    x += colWidths[i];
                }
                y -= rowHeight;
                if (y < 60) {
                    contentStream.close();
                    page = new org.apache.pdfbox.pdmodel.PDPage();
                    document.addPage(page);
                    contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
                    y = 770;
                }
            }
            contentStream.close();

            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Save PDF Report");
            fileChooser.setSelectedFile(new java.io.File("expenses.pdf"));
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                document.save(fileToSave);
                showSuccessMessage("PDF exported successfully!");
            }
            document.close();
        } catch (Exception e) {
            showErrorMessage("Error exporting PDF: " + e.getMessage());
        }
    }



    private void createChatView() {
        JPanel chatPanel = new JPanel(new BorderLayout(20, 20));
        chatPanel.setBackground(secondaryColor);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = createHeaderPanel("AI Financial Assistant");

        JButton clearButton = new JButton("Clear Chat");
        clearButton.setFont(REGULAR_FONT);
        clearButton.setBackground(new Color(200, 200, 200));
        clearButton.setForeground(textColor);
        clearButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final JPanel[] messagesPanelRef = new JPanel[1];

        clearButton.addActionListener(e -> {
            if (messagesPanelRef[0] != null) {
                messagesPanelRef[0].removeAll();
                addMessageBubble(messagesPanelRef[0],
                        "Hello! I'm your AI financial assistant. How can I help you today?", false);
                messagesPanelRef[0].revalidate();
                messagesPanelRef[0].repaint();

                if (chatService != null) {
                    chatService.clearConversation();
                }
            }
        });

        headerPanel.add(clearButton, BorderLayout.EAST);

        // Chat messages panel
        JPanel messagesPanel = new JPanel();
        messagesPanelRef[0] = messagesPanel;
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(primaryColor);
        messagesPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(new RoundedBorder(10, new Color(230, 230, 230)));
        scrollPane.setBackground(primaryColor);
        scrollPane.getViewport().setBackground(primaryColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Welcome message
        addMessageBubble(messagesPanel, "Hello! I'm your AI financial assistant. How can I help you today?", false);

        // Input panel with modern design
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(secondaryColor);
        inputPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JTextField inputField = new JTextField();
        inputField.setFont(REGULAR_FONT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(25, new Color(230, 230, 230)),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JButton sendButton = new JButton(new String("Send"));
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sendButton.setBackground(accentColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(50, 50));


        // Action for sending messages
        ActionListener sendAction = e -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                addMessageBubble(messagesPanel, message, true);
                inputField.setText("");
                scrollToBottom(scrollPane);

                // Show typing indicator
                JPanel typingIndicator = createTypingIndicator();
                messagesPanel.add(typingIndicator);
                messagesPanel.revalidate();
                messagesPanel.repaint();
                scrollToBottom(scrollPane);

                // Process AI response
                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        String apiKey = ConfigManager.getDeepseekAIKey();
                        if (apiKey == null || apiKey.trim().isEmpty()) {
                            return "ERROR: Please set up your DeepSeek API key in Settings first.";
                        }

                        try {
                            AIChatService chatService = new AIChatService(apiKey);

                            // Gather comprehensive dashboard data
                            Map<String, Object> dashboardData = new HashMap<>();
                            dashboardData.put("totalExpenses", expenseDAO.getTotalExpenses(currentUser.getId()));
                            dashboardData.put("monthlyAverage", expenseDAO.getMonthlyAverage(currentUser.getId()));
                            dashboardData.put("topCategory", expenseDAO.getTopCategory(currentUser.getId()));
                            dashboardData.put("expensesByCategory", expenseDAO.getExpensesByCategory(currentUser.getId()));

                            // Get recent expenses for context
                            List<Map<String, Object>> recentExpenses = expenseDAO.getRecentExpenses(currentUser.getId(), 5);
                            dashboardData.put("recentExpenses", recentExpenses);

                            // Get current month data
                            LocalDate now = LocalDate.now();
                            LocalDate startOfMonth = now.withDayOfMonth(1);
                            Map<String, BigDecimal> monthlyData = expenseDAO.getExpensesByDateRange(
                                    currentUser.getId(), startOfMonth, now);
                            dashboardData.put("currentMonthExpenses", monthlyData);

                            return chatService.processQuestion(message, dashboardData);

                        } catch (Exception ex) {
                            return "ERROR: " + ex.getMessage();
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            // Remove typing indicator
                            messagesPanel.remove(typingIndicator);

                            String response = get();
                            if (response.startsWith("ERROR: ")) {
                                addMessageBubble(messagesPanel, response.substring(7), false);
                            } else {
                                addMessageBubble(messagesPanel, response, false);
                            }
                            scrollToBottom(scrollPane);
                        } catch (Exception ex) {
                            messagesPanel.remove(typingIndicator);
                            addMessageBubble(messagesPanel,
                                    "Sorry, I encountered an error: " + ex.getMessage(), false);
                            scrollToBottom(scrollPane);
                        }
                    }
                };
                worker.execute();
            }


        };



        // Add action listeners
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        // Add hover effect to send button
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(accentColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(accentColor);
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(headerPanel, BorderLayout.NORTH);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        this.contentPanel.add(chatPanel, "chat");
    }

    private void addMessageBubble(JPanel container, String message, boolean isUser) {
        JPanel outerPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.LEFT : FlowLayout.RIGHT, 0, 0));
        outerPanel.setOpaque(false);

        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setBackground(isUser ? new Color(220, 225, 255) : new Color(245, 245, 245));
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, isUser ? new Color(79, 93, 247, 50) : new Color(200, 200, 200, 50)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        bubblePanel.setAlignmentX(isUser ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        JTextArea textArea = new JTextArea(message);
        textArea.setFont(REGULAR_FONT);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setForeground(textColor);
        textArea.setBorder(null);

        int maxWidth = 400;
        Dimension prefSize = getTextAreaPreferredSize(textArea, maxWidth);
        textArea.setPreferredSize(prefSize);
        textArea.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
        textArea.setMinimumSize(new Dimension(60, prefSize.height));

        bubblePanel.add(textArea);

        // Add timestamp below the message
        JLabel timeLabel = new JLabel(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(128, 128, 128));
        timeLabel.setAlignmentX(isUser ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);
        bubblePanel.add(Box.createVerticalStrut(4));
        bubblePanel.add(timeLabel);

        outerPanel.add(bubblePanel);
        container.add(outerPanel);
        container.revalidate();
        container.repaint();
    }

    // Helper to calculate preferred size for JTextArea based on text and max width
    private Dimension getTextAreaPreferredSize(JTextArea textArea, int maxWidth) {
        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int maxLineWidth = 0;
        int lines = 0;
        for (String line : textArea.getText().split("\n")) {
            int lineWidth = fm.stringWidth(line);
            int lineCount = Math.max(1, (int) Math.ceil((double) lineWidth / maxWidth));
            lines += lineCount;
            maxLineWidth = Math.max(maxLineWidth, Math.min(lineWidth, maxWidth));
        }
        int width = Math.max(60, Math.min(maxLineWidth + 20, maxWidth));
        int height = fm.getHeight() * lines + 20;
        return new Dimension(width, height);
    }

    private JPanel createTypingIndicator() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(primaryColor);
        panel.setBorder(new EmptyBorder(5, 10, 5, 50));

        JLabel label = new JLabel("AI is thinking...");
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(new Color(128, 128, 128));

        panel.add(label, BorderLayout.WEST);
        return panel;
    }

    private void scrollToBottom(JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void createThemeToggle(JPanel container) {
        JPanel toggleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toggleWrapper.setBackground(primaryColor);

        JLabel themeLabel = new JLabel(isDarkTheme ? THEME_ICON_DARK : THEME_ICON_LIGHT);
        themeLabel.setFont(REGULAR_FONT);

        JPanel toggleButton = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2d.setColor(isDarkTheme ? accentColor : new Color(200, 200, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                // Draw toggle circle
                g2d.setColor(Color.WHITE);
                int diameter = getHeight() - 4;
                int x = isDarkTheme ? getWidth() - diameter - 2 : 2;
                g2d.fillOval(x, 2, diameter, diameter);

                g2d.dispose();
            }
        };

        toggleButton.setPreferredSize(new Dimension(50, 24));
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleTheme();
                themeLabel.setText(isDarkTheme ? THEME_ICON_DARK : THEME_ICON_LIGHT);
                toggleButton.repaint();
            }
        });

        container.add(themeLabel);
        container.add(toggleButton);
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;

        // Update colors
        if (isDarkTheme) {
            primaryColor = new Color(18, 18, 18);
            secondaryColor = new Color(30, 30, 30);
            accentColor = new Color(200, 200, 200);
            textColor = new Color(255, 255, 255);
        } else {
            primaryColor = new Color(255, 255, 255);
            secondaryColor = new Color(245, 245, 245);
            accentColor = new Color(50, 50, 50);
            textColor = new Color(33, 33, 33);
        }

        // Update UI with new colors
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(isDarkTheme ? new FlatDarkLaf() : new FlatLightLaf());

                // Set custom colors
                UIManager.put("Panel.background", primaryColor);
                UIManager.put("Button.background", accentColor);
                UIManager.put("Button.foreground", isDarkTheme ? primaryColor : Color.WHITE);
                UIManager.put("TextField.background", secondaryColor);
                UIManager.put("TextField.foreground", textColor);
                UIManager.put("ComboBox.background", secondaryColor);
                UIManager.put("ComboBox.foreground", textColor);
                UIManager.put("Label.foreground", textColor);
                UIManager.put("Table.background", primaryColor);
                UIManager.put("Table.foreground", textColor);
                UIManager.put("TableHeader.background", accentColor);
                UIManager.put("TableHeader.foreground", isDarkTheme ? primaryColor : Color.WHITE);

                // Recreate all views with new colors
                contentPanel.removeAll();
                createDashboardView();
                createExpensesView();
                createAnalyticsView();
                createSettingsView();
                createChatView();

                // Recreate sidebar
                mainPanel.remove(sidebarPanel);
                createSidebar();
                mainPanel.add(sidebarPanel, BorderLayout.WEST);

                // Show current view
                contentLayout.first(contentPanel);

                SwingUtilities.updateComponentTreeUI(this);
                refreshUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshUI() {
        // Update main components
        mainPanel.setBackground(primaryColor);
        sidebarPanel.setBackground(primaryColor);
        contentPanel.setBackground(secondaryColor);

        // Update borders
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                isDarkTheme ? new Color(50, 50, 50) : new Color(220, 220, 220)));

        // Update all child components recursively
        updateComponentTreeColors(mainPanel);

        // Force complete repaint
        revalidate();
        repaint();
    }

    // Add this helper method
    private void updateComponentTreeColors(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(primaryColor);
            } else if (comp instanceof JLabel) {
                comp.setForeground(textColor);
            } else if (comp instanceof JButton) {
                comp.setForeground(textColor);
            }

            if (comp instanceof Container) {
                updateComponentTreeColors((Container) comp);
            }
        }
    }

    private void setupAnimations() {
        javax.swing.Timer fadeTimer = new javax.swing.Timer(50, null);
        fadeTimer.addActionListener(e -> {
            // Add fade animations for panel transitions
            contentPanel.repaint();
        });
    }

    private void showAddExpenseDialog() {
        ExpenseDialogWithAI dialog = new ExpenseDialogWithAI(this);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                // Create new expense from dialog data
                Expense expense = new Expense();
                expense.setUserId(currentUser.getId());
                expense.setAmount(new BigDecimal(String.valueOf(dialog.getAmount())));
                expense.setDate(LocalDate.now());
                expense.setDescription(dialog.getDescription());
                expense.setCategoryName(dialog.getCategory());

                // Save to database
                expenseDAO.createExpense(expense);

                // Refresh the entire dashboard view
                SwingUtilities.invokeLater(() -> {
                    contentPanel.remove(contentPanel.getComponent(0));
                    createDashboardView();
                    contentLayout.show(contentPanel, "dashboard");
                    revalidate();
                    repaint();
                });

                showSuccessMessage("Expense added successfully!");
            } catch (Exception ex) {
                showErrorMessage("Error adding expense: " + ex.getMessage());
            }
        }
    }

    private void addFormField(JPanel panel, JLabel label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(REGULAR_FONT);
        button.setBackground(bgColor);
        button.setForeground(isDarkTheme ? PRIMARY_DARK : PRIMARY_LIGHT);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }

    private void showSuccessMessage(String message) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(SUCCESS_COLOR);
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel label = new JLabel(message);
        label.setFont(REGULAR_FONT);
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        Timer timer = new Timer(2000, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
    }

    private void showErrorMessage(String message) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(ERROR_COLOR);
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel label = new JLabel(message);
        label.setFont(REGULAR_FONT);
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        Timer timer = new Timer(2000, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
    }

    private void refreshDashboard() {
        try {
            // Refresh stats
            BigDecimal totalExpenses = expenseDAO.getTotalExpenses(currentUser.getId());
            Map<String, BigDecimal> categoryTotals = expenseDAO.getExpensesByCategory(currentUser.getId());
            
            // Update UI components
            SwingUtilities.invokeLater(() -> {
                // Refresh all panels
                createDashboardView();
                contentLayout.show(contentPanel, "dashboard");
                revalidate();
                repaint();
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error refreshing dashboard: " + e.getMessage());
        }
    }

    // Add this method to handle expense editing
    private void editExpense(Expense expense) {
        JDialog dialog = new JDialog(this, "Edit Expense", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(primaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Date Field
        JLabel dateLabel = new JLabel(FOOD_ICON + " Date:");
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.sql.Date.valueOf(expense.getDate()));
        addFormField(formPanel, dateLabel, dateSpinner, gbc, 0);

        // Amount Field
        JLabel amountLabel = new JLabel(EXPENSES_ICON + " Amount:");
        JTextField amountField = new JTextField(expense.getAmount().toString());
        addFormField(formPanel, amountLabel, amountField, gbc, 1);

        // Category Field
        JLabel categoryLabel = new JLabel("🏷️ Category:");
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Others"};
        JComboBox<String> categoryBox = new JComboBox<>(categories);
        categoryBox.setSelectedItem(expense.getCategoryName());
        addFormField(formPanel, categoryLabel, categoryBox, gbc, 2);

        // Description Field
        JLabel descLabel = new JLabel("📝 Description:");
        JTextField descField = new JTextField(expense.getDescription());
        addFormField(formPanel, descLabel, descField, gbc, 3);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(primaryColor);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(200, 200, 200));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = new JButton("Save");
        styleButton(saveButton, accentColor);
        saveButton.addActionListener(e -> {
            try {
                // Update expense object
                expense.setAmount(new BigDecimal(amountField.getText()));
                expense.setDate(((Date) dateSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
                expense.setDescription(descField.getText());
                expense.setCategoryName(categoryBox.getSelectedItem().toString());

                // Update in database
                expenseDAO.updateExpense(expense);

                // Close dialog
                dialog.dispose();
                
                // Refresh the dashboard
                refreshDashboard();
                showSuccessMessage("Expense updated successfully!");
            } catch (Exception ex) {
                showErrorMessage("Error updating expense: " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Add this method to handle expense deletion
    private void deleteExpense(Expense expense) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this expense?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Delete from database
                expenseDAO.deleteExpense(expense.getId());
                refreshDashboard();
                showSuccessMessage("Expense deleted successfully!");
            } catch (Exception e) {
                showErrorMessage("Error deleting expense: " + e.getMessage());
            }
        }
    }

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
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
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

    private JButton createIconButton(String text) {
        JButton button = new JButton(text);
        Font buttonFont = getIconFont();
        button.setFont(buttonFont);
        return button;
    }
} 