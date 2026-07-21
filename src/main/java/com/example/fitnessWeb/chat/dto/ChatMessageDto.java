package com.example.fitnessWeb.chat.dto;

import com.example.fitnessWeb.chat.domain.SenderType;

import java.time.LocalDateTime;

public record ChatMessageDto(Long id, SenderType senderType, String content, LocalDateTime createdAt, Long orderNo) {}
