package com.example.fitnessWeb.chat.service;

import com.example.fitnessWeb.ai.service.ChatAiService;
import com.example.fitnessWeb.chat.domain.Chat;
import com.example.fitnessWeb.chat.domain.ChatMessage;
import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.chat.dto.ChatMessageDto;
import com.example.fitnessWeb.chat.dto.ChatResponse;
import com.example.fitnessWeb.chat.domain.SenderType;
import com.example.fitnessWeb.chat.repository.ChatMessageRepository;
import com.example.fitnessWeb.chat.repository.ChatRepository;
import com.example.fitnessWeb.common.exception.ChatNotFoundException;
import com.example.fitnessWeb.common.exception.UnAuthorizedAccessException;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuthService authService;
    private final ChatAiService chatAiService;

    /** 새로운 채팅 시작 */
    @Transactional
    public ChatResponse startChat(String userId, String userMessage) {
        User user = authService.getUserReference(userId);

        String title = chatAiService.generateTitle(userMessage);
        if (!StringUtils.hasText(title)) title = "새 채팅";

        Chat chat = Chat.createChat(user, title);

        int messageOrder = 1;

        ChatMessage userChatMessage = ChatMessage.createChatMessage(chat, SenderType.USER, userMessage, messageOrder);
        chatMessageRepository.save(userChatMessage);

        String aiResponse = chatAiService.generateStartResponse(userMessage);
        ChatMessage aiChatMessage = ChatMessage.createChatMessage(chat, SenderType.AI, aiResponse, messageOrder + 1);
        chatMessageRepository.save(aiChatMessage);

        return new ChatResponse(chat.getId(), aiResponse);
    }

    /** 채팅 이어서 진행 */
    @Transactional
    public ChatResponse continueChat(String userId, Long chatId, String userMessage) {
        Chat chat = findChatForUpdateOrThrow(chatId);

        if (!chat.getUser().getUserId().equals(userId)) {
            throw new UnAuthorizedAccessException("해당 채팅에 접근할 권한이 없습니다.");
        }

        long messageOrder = chatMessageRepository.countByChat(chat) + 1;
        ChatMessage userChatMessage = ChatMessage.createChatMessage(chat, SenderType.USER, userMessage, messageOrder);
        chatMessageRepository.save(userChatMessage);

        String aiResponse = chatAiService.generateContinueResponse(userMessage);
        ChatMessage aiChatMessage = ChatMessage.createChatMessage(chat, SenderType.AI, aiResponse, messageOrder + 1);
        chatMessageRepository.save(aiChatMessage);

        return new ChatResponse(chat.getId(), aiResponse);
    }

    /** 채팅 리스트 조회 */
    @Transactional(readOnly = true)
    public Page<ChatDto> getUserChats(String userId, int page) {
        User user = authService.getUserReference(userId);

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        return chatRepository.findDtoByUserOrderByLastAccessedAtDesc(user, pageable);
    }

    /** 채팅 메시지 조회 */
    @Transactional(readOnly = true)
    public Page<ChatMessageDto> getChatMessages(Long chatId, int page) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        return chatMessageRepository.findDtoByChatId(chatId, pageable);
    }

    /** 채팅 삭제 */
    @Transactional
    public void deleteChat(String userId, Long chatId) {
        User user = authService.getUserReference(userId);
        Chat chat = findChatOrThrow(chatId);

        if (!chat.getUser().equals(user)) throw new UnAuthorizedAccessException("본인의 채팅만 삭제할 수 있습니다.");
        chat.delete();
    }

    private Chat findChatOrThrow(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("채팅이 존재하지 않습니다."));
    }

    private Chat findChatForUpdateOrThrow(Long chatId) {
        return chatRepository.findByIdForUpdate(chatId)
                .orElseThrow(() -> new ChatNotFoundException("채팅이 존재하지 않습니다."));
    }
}
