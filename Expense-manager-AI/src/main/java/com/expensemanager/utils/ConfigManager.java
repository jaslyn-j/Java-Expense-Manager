
package com.expensemanager.utils;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    // Always look for config.properties in the Expense-manager-AI directory
    private static final String CONFIG_FILE = getConfigFilePath();
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }

    private static String getConfigFilePath() {
        // Try to find the Expense-manager-AI directory in the current path or parent paths
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir);
        while (dir != null) {
            File candidate = new File(dir, "Expense-manager-AI/config.properties");
            if (candidate.exists()) {
                return candidate.getAbsolutePath();
            }
            dir = dir.getParentFile();
        }
        // Fallback: use config.properties in current directory
        return "config.properties";
    }
    
    private static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            try {
                // Create default config file if it doesn't exist
                configFile.createNewFile();
                properties.setProperty("deepseek.api.key", "");
                saveConfig("Initial configuration created");
            } catch (IOException e) {
                System.err.println("Error creating config file: " + e.getMessage());
            }
        } else {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
            }
        }
    }
    
    private static void saveConfig(String comment) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, comment);
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
    
    public static String getDeepseekAIKey() {
        // First try environment variable
        String key = System.getenv("DEEPSEEK_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key.trim();
        }
        
        // Then try properties file
        key = properties.getProperty("deepseek.api.key");
        return key != null ? key.trim() : "";
    }
    
    public static void setDeepseekAIKey(String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("API key cannot be null");
        }
        properties.setProperty("deepseek.api.key", apiKey.trim());
        saveConfig("Updated Deepseek AI API key");
    }
    

}
