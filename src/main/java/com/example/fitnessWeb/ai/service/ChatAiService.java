package com.example.fitnessWeb.ai.service;

import com.example.fitnessWeb.ai.model.AiModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAiService {

    private final AiModel aiModel;  // 이제 인터페이스에만 의존

    public String ask(String prompt) {
        return aiModel.ask(prompt);
    }

    public String generateTitle(String userMessage) {
        return aiModel.generateTitle(userMessage);
    }

    public String generatePrompt(String userMessage) {
        return aiModel.generatePrompt(userMessage);
    }

    public String generateContinuePrompt(String userMessage) {
        return aiModel.generateContinuePrompt(userMessage);
    }
}
