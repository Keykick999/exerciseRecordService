package com.example.fitnessWeb.ai.client;

import com.example.fitnessWeb.common.exception.AiApiException;
import com.example.fitnessWeb.common.exception.AiResponseParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class GeminiAiClient implements AiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Retryable(
            retryFor = AiApiException.class,   // 파싱 실패는 제외하고 api 요청 실패에 대해서만 재시도 하도록 설정
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2
            )
    )
    public String generateResponse(String prompt) {
        GeminiRequest request = new GeminiRequest(
                List.of(
                        new Content(
                                List.of(
                                        new Part(prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
        String fullUrl = apiUrl + "?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
            return parseAiResponse(response.getBody());
        } catch (RestClientException e) {
            throw new AiApiException("Gemini API 호출에 실패했습니다.", e);
        }
        catch (JsonProcessingException e) {
            throw new AiResponseParseException("Gemini 응답 파싱에 실패했습니다.", e);
        }
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

    // ===== Gemini Request DTO =====

    private record GeminiRequest(
            List<Content> contents
    ) {
    }

    private record Content(
            List<Part> parts
    ) {
    }

    private record Part(
            String text
    ) {
    }
}
