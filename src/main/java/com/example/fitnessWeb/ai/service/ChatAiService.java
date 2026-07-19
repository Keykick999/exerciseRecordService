package com.example.fitnessWeb.ai.service;

import com.example.fitnessWeb.ai.client.AiClient;
import com.example.fitnessWeb.ai.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAiService {

    private final AiClient aiClient;
    private final PromptBuilder promptBuilder;

    /** 새로운 채팅 시작 시 AI 응답 생성 */
    public String generateStartResponse(String userMessage) {
        String prompt = promptBuilder.buildChatStartPrompt(userMessage);
        return aiClient.generateResponse(prompt);
    }

    /** 채팅 이어가기 */
    public String generateContinueResponse(String userMessage) {
        String prompt = promptBuilder.buildChatContinuePrompt(userMessage);
        return aiClient.generateResponse(prompt);
    }

    /** 채팅 제목 생성 */
    public String generateTitle(String userMessage) {
        String prompt = promptBuilder.buildTitlePrompt(userMessage);
        return aiClient.generateResponse(prompt);
    }
}