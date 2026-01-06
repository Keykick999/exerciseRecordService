package com.example.fitnessWeb.ai.model;

public interface AiModel {
    String ask(String prompt);
    String generateTitle(String userMessage);
    String generatePrompt(String userMessage);
    String generateContinuePrompt(String userMessage);
}
