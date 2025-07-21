package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ChatDto;
import com.example.fitnessWeb.dto.ChatMessageDto;
import com.example.fitnessWeb.dto.ChatResponse;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.exception.ChatNotFoundException;
import com.example.fitnessWeb.exception.UnauthorizedAccessException;
import com.example.fitnessWeb.exception.UserNotFoundException;
import com.example.fitnessWeb.repository.ChatMessageRepository;
import com.example.fitnessWeb.repository.ChatRepository;
import com.example.fitnessWeb.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
        String prompt = userMessage + "\n\n- 자연스럽고 간결하게 대답해줘.\n- userMessage에 ** 같은 기호는 빼고, 가독성을 위해 기호는 · 또는 - 만 사용해줘.\n- 내용이 표로 보여주기 편하면 표로 보여주고, 아니면 텍스트로 가독성 좋게 정리해줘.\n- 말투는 헬스 트레이너 스타일로.";
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

        String prompt = userMessage +
                "\n\n자연스럽고 간결한 말투로 답변해줘. 말투는 헬스 트레이너처럼 친근하고 명확하게 해줘." +
                "텍스트는 보기 편하게 문단 단위로 정리하고, 마크다운 기호는 사용하지 말아줘. 예를 들면 별표(*), 하이픈(-), 밑줄(_) 같은 기호나 표 형식도 사용하지 말아줘." +
                "일반 텍스트 형식으로 정리해주면 돼." +
                "예를 들어 이런 식으로 작성해줘:\n" +
                "\n운동 효과\n" +
                "이 운동은 가슴 근육과 삼두근을 집중적으로 자극해. 상체 근육을 키우고 싶은 사람에게 특히 좋아.\n" +
                "\n운동 방법\n" +
                "양손은 어깨너비보다 약간 넓게 잡고, 팔꿈치를 90도로 내렸다가 밀어올리는 동작을 반복해. 자세가 흐트러지지 않도록 복근에 힘을 주는 것도 중요해.\n" +
                "\n주의 사항\n" +
                "초보자라면 너무 무리하지 말고 횟수는 적당히 조절해. 어깨 통증이 있다면 다른 운동으로 대체하는 게 좋아.";
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
        String prompt = userMessage + "\n\n- 이 메시지를 대표하는 간결한 채팅 제목을 10자 이내로 만들어줘.";
        return sendToGemini(prompt);
    }


    @Transactional
    public void deleteChat(Long chatId) {
        // 현재 로그인된 사용자 확인
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else {
            throw new UserNotFoundException();
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("채팅이 존재하지 않습니다."));

        if (!chat.getUser().equals(user)) {
            throw new UnauthorizedAccessException("본인의 채팅만 삭제할 수 있습니다.");
        }

        // Chat 및 관련 메시지 소프트 삭제
        chat.setIsDeleted(true);
        chat.setDeletedAt(LocalDateTime.now());

        chat.getMessages().forEach(message -> {
            message.setIsDeleted(true);
            message.setDeletedAt(LocalDateTime.now());
        });

        chatRepository.save(chat); // cascade에 의해 message도 반영됨
    }
}