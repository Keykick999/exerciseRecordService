package com.example.fitnessWeb.chat.domain;

import com.example.fitnessWeb.chat.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    private ChatMessage(Chat chat, Role role, String content, int orderNo) {
        this.chat = chat;
        this.role = role;
        this.content = content;
        this.orderNo = orderNo;
        this.isDeleted = false;
    }

    public static ChatMessage createChatMessage(Chat chat, Role role, String content, int orderNo) {
        return new ChatMessage(chat, role, content, orderNo);
    }
}
