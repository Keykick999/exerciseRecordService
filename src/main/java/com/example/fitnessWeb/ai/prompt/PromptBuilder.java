package com.example.fitnessWeb.ai.prompt;

public interface PromptBuilder {

    String buildChatStartPrompt(String userMessage);

    String buildChatContinuePrompt(String userMessage);

    String buildTitlePrompt(String userMessage);
}
