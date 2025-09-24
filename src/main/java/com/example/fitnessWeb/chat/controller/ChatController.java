package com.example.fitnessWeb.chat.controller;
import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.chat.dto.ChatMessageDto;
import com.example.fitnessWeb.chat.dto.ChatRequest;
import com.example.fitnessWeb.chat.dto.ChatResponse;
import com.example.fitnessWeb.chat.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> askCoach(Authentication auth, @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.askModel(auth.getName(), request.getMessage()));
    }

    @PostMapping("/{chatId}/message")
    public ResponseEntity<ChatResponse> continueChat(
            @PathVariable Long chatId,
            @RequestBody ChatRequest request
    ) {
        return ResponseEntity.ok(chatService.continueChat(chatId, request.getMessage()));
    }


    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<ChatMessageDto>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<ChatMessageDto> messages = chatService.getChatMessages(chatId, page);
        return ResponseEntity.ok(messages);
    }

    @GetMapping
    public ResponseEntity<Page<ChatDto>> getChats(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<ChatDto> chats = chatService.getChats(auth.getName(), page);
        return ResponseEntity.ok(chats);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(Authentication auth, @PathVariable Long chatId) {
        chatService.deleteChat(auth.getName(), chatId);
        return ResponseEntity.noContent().build();
    }
}

