
package com.expensemanager.utils;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = getConfigFilePath();
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }

    private static String getConfigFilePath() {
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir);
        while (dir != null) {
            File candidate = new File(dir, "Expense-manager-AI/config.properties");
            if (candidate.exists()) {
                return candidate.getAbsolutePath();
            }
            dir = dir.getParentFile();
        }
        return "config.properties";
    }
    
    private static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            try {
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
        String key = properties.getProperty("deepseek.api.key");
        return key != null ? key.trim() : "";
    }
    

}
