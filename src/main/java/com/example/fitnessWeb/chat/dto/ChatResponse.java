package com.example.fitnessWeb.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChatResponse {
    private Long chatId;
    private String reply;
}
