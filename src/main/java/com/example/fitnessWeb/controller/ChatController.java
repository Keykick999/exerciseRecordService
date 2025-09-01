package com.example.fitnessWeb.controller;
import com.example.fitnessWeb.dto.ChatDto;
import com.example.fitnessWeb.dto.ChatMessageDto;
import com.example.fitnessWeb.dto.ChatRequest;
import com.example.fitnessWeb.dto.ChatResponse;
import com.example.fitnessWeb.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> askCoach(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.askModel(request.getMessage()));
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
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<ChatDto> chats = chatService.getChats(page);
        return ResponseEntity.ok(chats);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }
}

