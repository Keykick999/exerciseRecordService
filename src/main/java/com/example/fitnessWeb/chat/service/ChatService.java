package com.example.fitnessWeb.chat.service;

import com.example.fitnessWeb.chat.domain.Chat;
import com.example.fitnessWeb.chat.domain.ChatMessage;
import com.example.fitnessWeb.chat.enums.Role;
import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.chat.dto.ChatMessageDto;
import com.example.fitnessWeb.chat.dto.ChatResponse;
import com.example.fitnessWeb.common.exception.ChatNotFoundException;
import com.example.fitnessWeb.common.exception.UnAuthorizedAccessException;
import com.example.fitnessWeb.common.exception.UserNotFoundException;
import com.example.fitnessWeb.chat.repository.ChatMessageRepository;
import com.example.fitnessWeb.chat.repository.ChatRepository;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
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

    /** 새로운 채팅 시작 */
    public ChatResponse askModel(String userId, String userMessage) {
        User user = findUserOrThrow(userId);

        String title = generateTitleFromGemini(userMessage);
        if (title == null || title.isBlank()) title = "새 채팅";

        Chat chat = createChat(user, title);

        saveChatMessage(chat, Role.USER, userMessage, 1);

        String gptResponse = sendToGemini(generatePrompt(userMessage));
        saveChatMessage(chat, Role.GPT, gptResponse, 2);

        return new ChatResponse(chat.getId(), gptResponse);
    }

    /** 채팅 이어서 진행 */
    public ChatResponse continueChat(Long chatId, String userMessage) {
        Chat chat = findChatOrThrow(chatId);

        int nextOrder = (int) (chatMessageRepository.countByChat(chat) + 1);
        saveChatMessage(chat, Role.USER, userMessage, nextOrder);

        String gptResponse = sendToGemini(generateContinuePrompt(userMessage));
        saveChatMessage(chat, Role.GPT, gptResponse, nextOrder + 1);

        return new ChatResponse(chat.getId(), gptResponse);
    }

    /** 채팅 리스트 조회 */
    public Page<ChatDto> getChats(String userId, int page) {
        User user = findUserOrThrow(userId);
        Pageable pg = PageRequest.of(page, 10);
        return chatRepository.findDtoByUserOrderByLastAccessedAtDesc(user, pg);
    }

    /** 채팅 메시지 조회 */
    public Page<ChatMessageDto> getChatMessages(Long chatId, int page) {
        Pageable pg = PageRequest.of(page, 10);
        return chatMessageRepository.findDtoByChatId(chatId, pg);
    }

    /** 채팅 삭제 */
    @Transactional
    public void deleteChat(String userId, Long chatId) {
        User user = findUserOrThrow(userId);
        Chat chat = findChatOrThrow(chatId);

        if (!chat.getUser().equals(user)) throw new UnAuthorizedAccessException("본인의 채팅만 삭제할 수 있습니다.");

        chat.setIsDeleted(true);
        chat.setDeletedAt(LocalDateTime.now());

        chat.getMessages().forEach(message -> {
            message.setIsDeleted(true);
            message.setDeletedAt(LocalDateTime.now());
        });

        chatRepository.save(chat);
    }

    /** ------------------ 헬퍼 메서드 ------------------ */

    private User findUserOrThrow(String userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    private Chat findChatOrThrow(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("채팅이 존재하지 않습니다."));
    }

    private Chat createChat(User user, String title) {
        Chat chat = Chat.builder()
                .user(user)
                .title(title)
                .isDeleted(false)
                .lastAccessedAt(LocalDateTime.now())
                .build();
        return chatRepository.save(chat);
    }

    private ChatMessage saveChatMessage(Chat chat, Role role, String content, int orderNo) {
        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .role(role)
                .content(content)
                .orderNo(orderNo)
                .isDeleted(false)
                .build();
        return chatMessageRepository.save(message);
    }

    private String sendToGemini(String message) {
        RestTemplate restTemplate = new RestTemplate();

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
                """.formatted(message);

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

    private String generateTitleFromGemini(String userMessage) {
        String prompt = userMessage + "\n\n- 이 메시지를 대표하는 간결한 채팅 제목을 10자 이내로 만들어줘.";
        return sendToGemini(prompt);
    }

    private String generatePrompt(String userMessage) {
        return userMessage +
                "\n\n- 자연스럽고 간결하게 대답해줘." +
                "\n- userMessage에 ** 같은 기호는 빼고, 가독성을 위해 기호는 · 또는 - 만 사용해줘." +
                "\n- 내용이 표로 보여주기 편하면 표로 보여주고, 아니면 텍스트로 가독성 좋게 정리해줘." +
                "\n- 말투는 헬스 트레이너 스타일로.";
    }

    private String generateContinuePrompt(String userMessage) {
        return userMessage +
                "\n\n자연스럽고 간결한 말투로 답변해줘. 말투는 헬스 트레이너처럼 친근하고 명확하게 해줘." +
                "텍스트는 보기 편하게 문단 단위로 정리하고, 마크다운 기호는 사용하지 말아줘." +
                "예를 들어 별표(*), 하이픈(-), 밑줄(_) 같은 기호나 표 형식도 사용하지 말아줘.";
    }
}
