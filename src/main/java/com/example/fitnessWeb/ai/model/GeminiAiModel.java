package com.example.fitnessWeb.ai.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("geminiModel")
public class GeminiAiModel implements AiModel {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAiModel() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_PROMPT = """
        자연스럽고 간결하게 대답해줘.
        userMessage에 ** 같은 기호는 빼고, 가독성을 위해 기호는 · 또는 - 만 사용해줘.
        내용이 표로 보여주기 편하면 표로 보여주고, 아니면 텍스트로 가독성 좋게 정리해줘.
        말투는 헬스 트레이너 스타일로.
        """;

    private static final String CONTINUE_PROMPT = """
        자연스럽고 간결한 말투로 답변해줘.
        말투는 헬스 트레이너처럼 친근하고 명확하게 해줘.
        텍스트는 보기 편하게 문단 단위로 정리하고, 마크다운 기호는 사용하지 말아줘.
        예를 들어 별표(*), 하이픈(-), 밑줄(_) 같은 기호나 표 형식도 사용하지 말아줘.
        """;

    private static final String TITLE_PROMPT = """
        - 이 메시지를 대표하는 간결한 채팅 제목을 10자 이내로 만들어줘.
        """;

    @Override
    public String ask(String prompt) {
        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "%s" }
                      ]
                    }
                  ]
                }
                """.formatted(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String fullUrl = apiUrl + "?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
            return parseAiResponse(response.getBody());
        } catch (Exception e) {
            return "Gemini 응답 파싱 실패: " + e.getMessage();
        }
    }

    @Override
    public String generateTitle(String userMessage) {
        return ask(userMessage + TITLE_PROMPT);
    }

    @Override
    public String generatePrompt(String userMessage) {
        return userMessage + DEFAULT_PROMPT;
    }

    @Override
    public String generateContinuePrompt(String userMessage) {
        return userMessage + CONTINUE_PROMPT;
    }

    private String parseAiResponse(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
    }
}
