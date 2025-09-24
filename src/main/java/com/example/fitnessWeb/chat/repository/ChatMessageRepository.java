package com.example.fitnessWeb.chat.repository;

import com.example.fitnessWeb.chat.dto.ChatMessageDto;
import com.example.fitnessWeb.chat.domain.Chat;
import com.example.fitnessWeb.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("""
        select new com.example.fitnessWeb.chat.dto.ChatMessageDto(
            m.id,
            m.role,
            m.content,
            m.createdAt,
            m.orderNo
        )
        from ChatMessage m
        where m.chat.id = :chatId
          and m.isDeleted = false
        order by m.orderNo asc
    """)
    Page<ChatMessageDto> findDtoByChatId(
            @Param("chatId") Long chatId,
            Pageable pageable
    );

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chat = :chat")
    int countByChat(@Param("chat") Chat chat);
}


