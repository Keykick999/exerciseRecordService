package com.example.fitnessWeb.chat.repository;

import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.chat.domain.Chat;
import com.example.fitnessWeb.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("""
        select new com.example.fitnessWeb.chat.dto.ChatDto(
            c.id,
            c.title,
            c.createdAt,
            c.lastAccessedAt
        )
        from Chat c
        where c.user = :user
          and c.isDeleted = false
        order by c.lastAccessedAt desc
    """)
    Page<ChatDto> findDtoByUserOrderByLastAccessedAtDesc(
            @Param("user") User user,
            Pageable pageable
    );


    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chat = :chat")
    long countByChat(@Param("chat") Chat chat);
}
