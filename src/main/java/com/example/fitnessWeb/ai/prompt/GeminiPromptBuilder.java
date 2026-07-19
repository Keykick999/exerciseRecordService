package com.example.fitnessWeb.ai.prompt;

import org.springframework.stereotype.Component;

@Component
public class GeminiPromptBuilder implements PromptBuilder {
    /**
     * 새로운 채팅을 시작할 때 사용하는 시스템 프롬프트.
     * 첫 응답에서는 표나 구조화된 답변을 허용한다.
     */
    private static final String CHAT_START_PROMPT = """
        자연스럽고 간결하게 대답해줘.
        userMessage에 ** 같은 기호는 빼고, 가독성을 위해 기호는 · 또는 - 만 사용해줘.
        내용이 표로 보여주기 편하면 표로 보여주고, 아니면 텍스트로 가독성 좋게 정리해줘.
        말투는 헬스 트레이너 스타일로.
        """;

    /**
     * 기존 채팅을 이어갈 때 사용하는 시스템 프롬프트.
     * 대화의 흐름을 위해 일반 텍스트 위주로 응답하도록 유도한다.
     */
    private static final String CHAT_CONTINUE_PROMPT = """
        자연스럽고 간결한 말투로 답변해줘.
        말투는 헬스 트레이너처럼 친근하고 명확하게 해줘.
        텍스트는 보기 편하게 문단 단위로 정리하고, 마크다운 기호는 사용하지 말아줘.
        예를 들어 별표(*), 하이픈(-), 밑줄(_) 같은 기호나 표 형식도 사용하지 말아줘.
        """;

    private static final String TITLE_PROMPT = """
        - 이 메시지를 대표하는 간결한 채팅 제목을 10자 이내로 만들어줘.
        """;

    @Override
    public String buildChatStartPrompt(String userMessage) {
        return userMessage + CHAT_START_PROMPT;
    }

    @Override
    public String buildChatContinuePrompt(String userMessage) {
        return userMessage + CHAT_CONTINUE_PROMPT;
    }

    @Override
    public String buildTitlePrompt(String userMessage) {
        return userMessage + TITLE_PROMPT;
    }
}
