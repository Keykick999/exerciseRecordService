package com.example.fitnessWeb.dto;

import com.example.fitnessWeb.entity.Role;
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
