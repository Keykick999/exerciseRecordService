package com.example.fitnessWeb.chat.dto;

import com.example.fitnessWeb.chat.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDto {
    private Long id;
    private Role role;
    private String content;
    private LocalDateTime createdAt;
    private Integer orderNo;
}
