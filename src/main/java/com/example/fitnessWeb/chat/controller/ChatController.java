package com.example.fitnessWeb.chat.controller;
import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.chat.dto.ChatMessageDto;
import com.example.fitnessWeb.chat.dto.ChatRequest;
import com.example.fitnessWeb.chat.dto.ChatResponse;
import com.example.fitnessWeb.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> startChat(Authentication auth, @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.startChat(auth.getName(), request.message()));
    }

    @PostMapping("/{chatId}/message")
    public ResponseEntity<ChatResponse> continueChat(
            Authentication auth,
            @PathVariable Long chatId,
            @RequestBody ChatRequest request
    ) {
        return ResponseEntity.ok(chatService.continueChat(auth.getName(), chatId, request.message()));
    }


    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<ChatMessageDto>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(chatService.getChatMessages(chatId, page));
    }

    @GetMapping
    public ResponseEntity<Page<ChatDto>> getChats(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(chatService.getUserChats(auth.getName(), page));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(Authentication auth, @PathVariable Long chatId) {
        chatService.deleteChat(auth.getName(), chatId);
        return ResponseEntity.noContent().build();
    }
}

