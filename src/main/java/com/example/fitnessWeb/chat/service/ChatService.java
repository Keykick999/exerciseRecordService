package com.example.fitnessWeb.chat.service;

import com.example.fitnessWeb.ai.service.ChatAiService;
import com.example.fitnessWeb.chat.domain.Chat;
import com.example.fitnessWeb.chat.domain.ChatMessage;
import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.chat.dto.ChatMessageDto;
import com.example.fitnessWeb.chat.dto.ChatResponse;
import com.example.fitnessWeb.chat.enums.Role;
import com.example.fitnessWeb.chat.repository.ChatMessageRepository;
import com.example.fitnessWeb.chat.repository.ChatRepository;
import com.example.fitnessWeb.common.exception.ChatNotFoundException;
import com.example.fitnessWeb.common.exception.UnAuthorizedAccessException;
import com.example.fitnessWeb.common.exception.UserNotFoundException;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatAiService chatAiService;

    /** 새로운 채팅 시작 */
    public ChatResponse startChat(String userId, String userMessage) {
        User user = findUserOrThrow(userId);

        String title = chatAiService.generateTitle(userMessage);
        if (title == null || title.isBlank()) title = "새 채팅";

        Chat chat = Chat.createChat(user, title);

        saveChatMessage(chat, Role.USER, userMessage, 1);

        String aiResponse = chatAiService.generateStartResponse(userMessage);
        saveChatMessage(chat, Role.GPT, aiResponse, 2);

        return new ChatResponse(chat.getId(), aiResponse);
    }

    /** 채팅 이어서 진행 */
    public ChatResponse continueChat(Long chatId, String userMessage) {
        Chat chat = findChatOrThrow(chatId);

        int nextOrder = (int) (chatMessageRepository.countByChat(chat) + 1);
        saveChatMessage(chat, Role.USER, userMessage, nextOrder);

        String aiResponse = chatAiService.generateContinueResponse(userMessage);
        saveChatMessage(chat, Role.GPT, aiResponse, nextOrder + 1);

        return new ChatResponse(chat.getId(), aiResponse);
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

        chat.delete();

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
}
