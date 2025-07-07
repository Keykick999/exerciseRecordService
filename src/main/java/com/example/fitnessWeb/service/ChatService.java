package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ChatDto;
import com.example.fitnessWeb.dto.ChatMessageDto;
import com.example.fitnessWeb.dto.ChatResponse;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.exception.UserNotFoundException;
import com.example.fitnessWeb.repository.ChatMessageRepository;
import com.example.fitnessWeb.repository.ChatRepository;
import com.example.fitnessWeb.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatResponse askModel(String userMessage) {
        // 1. 인증된 사용자 정보에서 username 꺼내기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            throw new UserNotFoundException();
        }

        // 2. username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException());


        // 3. 채팅 제목 생성
        String generatedTitle = generateTitleFromGemini(userMessage);
        if (generatedTitle == null || generatedTitle.isBlank()) {
            generatedTitle = "새 채팅";  // fallback
        }

        // 4. Chat 생성 및 저장
        Chat chat = Chat.builder()
                .title(generatedTitle)
                .user(user)
                .isDeleted(false)
                .deletedAt(null)
                .lastAccessedAt(LocalDateTime.now())
                .build();
        chatRepository.save(chat);

        // 5. 사용자 메시지 저장
        ChatMessage userMsg = ChatMessage.builder()
                .chat(chat)
                .role(Role.USER)
                .content(userMessage)
                .orderNo(1)
                .isDeleted(false)
                .build();
        chatMessageRepository.save(userMsg);

        // 6. Gemini API 호출
        String prompt = userMessage + "\n\n- 자연스럽고 간결하게 대답해줘.\n- 마크다운 없이 정리된 요약 형식으로.";
        String gptResponse = sendToGemini(prompt);

        // 7. GPT 응답 저장
        ChatMessage gptMsg = ChatMessage.builder()
                .chat(chat)
                .role(Role.GPT)
                .content(gptResponse)
                .orderNo(2)
                .isDeleted(false)
                .build();
        chatMessageRepository.save(gptMsg);

        return new ChatResponse(chat.getId(), gptResponse);
    }

    private String sendToGemini(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(userMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String fullUrl = apiUrl + "?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "Gemini 응답 파싱 실패: " + e.getMessage();
        }
    }

    public Page<ChatDto> getChats(int page) {
        // 1. 현재 인증된 username 꺼내기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else {
            throw new UserNotFoundException();
        }

        // 2. User 엔티티 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException());
        // 3. 페이지 요청
        Pageable pg = PageRequest.of(page, 10);
        // 4. Projection 호출
        return chatRepository.findDtoByUserOrderByLastAccessedAtDesc(user, pg);
    }


    public Page<ChatMessageDto> getChatMessages(Long chatId, int page) {
        Pageable pg = PageRequest.of(page, 10);
        return chatMessageRepository.findDtoByChatId(chatId, pg);
    }


    public ChatResponse continueChat(Long chatId, String userMessage) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        ChatMessage userMsg = ChatMessage.builder()
                .chat(chat)
                .role(Role.USER)
                .content(userMessage)
                .orderNo(chatMessageRepository.countByChat(chat) + 1)
                .isDeleted(false)
                .build();
        chatMessageRepository.save(userMsg);

        String prompt = userMessage + "\n\n- 자연스럽고 간결하게 대답해줘.\n- 마크다운 없이 정리된 요약 형식으로.";
        String gptResponse = sendToGemini(prompt);

        ChatMessage gptMsg = ChatMessage.builder()
                .chat(chat)
                .role(Role.GPT)
                .content(gptResponse)
                .orderNo(chatMessageRepository.countByChat(chat) + 1)
                .isDeleted(false)
                .build();
        chatMessageRepository.save(gptMsg);

        return new ChatResponse(chat.getId(), gptResponse);
    }


    // 사용자 메시지 기반으로 채팅 제목 정하기
    private String generateTitleFromGemini(String userMessage) {
        // prompt 예시: "사용자가 보낸 메시지로 채팅 제목 한 줄로 간결하게 만들어줘."
        String prompt = userMessage + "\n\n- 이 메시지를 대표하는 간결한 채팅 제목을 한 줄로 만들어줘.";
        return sendToGemini(prompt);
    }
}