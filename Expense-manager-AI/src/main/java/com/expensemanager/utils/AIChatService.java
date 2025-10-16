package com.expensemanager.utils;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AIChatService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "You are a helpful financial assistant. Analyze the expense data and provide clear, concise summaries and insights.";
    private final String apiKey;
    private final OkHttpClient client;
    private final List<JSONObject> conversationHistory;
    private final MediaType JSON = MediaType.get("application/json");
    private static final int MAX_HISTORY_SIZE = 20;

    public AIChatService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("OpenRouter API key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.conversationHistory = new ArrayList<>();
        initializeConversation();
    }

    private void initializeConversation() {
        conversationHistory.clear();
        conversationHistory.add(new JSONObject()
                .put("role", "system")
                .put("content", SYSTEM_PROMPT));
    }

    public String processQuestion(String question, Map<String, Object> dashboardData) throws IOException {
        int maxRetries = 3;
        int baseDelayMs = 5000;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    Thread.sleep(baseDelayMs * (long)Math.pow(2, attempt - 1));
                }
                return makeAPIRequest(question, dashboardData);
            } catch (IOException e) {
                if (e.getMessage().contains("429") && attempt < maxRetries - 1) {
                    continue;
                }
                throw new IOException("API error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
        throw new IOException("Max retries exceeded");
    }

    private String makeAPIRequest(String question, Map<String, Object> dashboardData) throws IOException {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }

        JSONObject userMessage = new JSONObject()
                .put("role", "user")
                .put("content", formatQuestionWithData(question, dashboardData));
        addToHistory(userMessage);

        JSONObject requestBody = new JSONObject()
                .put("model", "deepseek/deepseek-chat")
                .put("messages", new JSONArray(conversationHistory))
                .put("temperature", 0.7)
                .put("max_tokens", 1000);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://expense-manager-app")
                .addHeader("X-Title", "Expense Manager")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;

            if (!response.isSuccessful()) {
                String errorMessage = "API error (" + response.code() + ")";
                if (responseBody != null) {
                    try {
                        JSONObject errorJson = new JSONObject(responseBody);
                        if (errorJson.has("error")) {
                            JSONObject error = errorJson.getJSONObject("error");
                            errorMessage += ": " + error.optString("message", "Unknown error");
                        }
                    } catch (Exception e) {
                        errorMessage += ": " + responseBody;
                    }
                }


                throw new IOException(errorMessage);
            }

            try {
                JSONObject jsonResponse = new JSONObject(responseBody);
                String assistantResponse = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                addToHistory(new JSONObject()
                        .put("role", "assistant")
                        .put("content", assistantResponse));

                return assistantResponse;
            } catch (Exception e) {
                throw new IOException("Failed to parse API response: " + e.getMessage());
            }
        }
    }

    private String formatQuestionWithData(String question, Map<String, Object> data) {
        StringBuilder context = new StringBuilder("Current Dashboard Data:\n");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            context.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return context.toString() + "\n\nUser Question: " + question;
    }

    public void clearConversation() {
        initializeConversation();
    }

    private void addToHistory(JSONObject message) {
        conversationHistory.add(message);
        if (conversationHistory.size() > MAX_HISTORY_SIZE) {
            conversationHistory.remove(1);
        }
    }

    public String suggestCategory(String description, double amount) throws IOException {
        String prompt = String.format(
                "Categorize this expense into ONE of these categories ONLY: " +
                        "Food, Transport, Entertainment, Bills, Shopping, Healthcare, Education, Travel, Other.\n\n" +
                        "Expense Description: %s\n" +
                        "Amount: $%.2f\n\n" +
                        "Respond with ONLY the category name, nothing else.",
                description, amount
        );

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are an expense categorization assistant. Always respond with only the category name."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));

        JSONObject requestBody = new JSONObject()
                .put("model", "deepseek/deepseek-chat")
                .put("messages", messages)
                .put("temperature", 0.3)
                .put("max_tokens", 50);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://expense-manager-app")
                .addHeader("X-Title", "Expense Manager")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;

            if (!response.isSuccessful()) {
                throw new IOException("Category suggestion failed: " + response.code());
            }

            JSONObject jsonResponse = new JSONObject(responseBody);
            String category = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

            return category;
        }
    }
}