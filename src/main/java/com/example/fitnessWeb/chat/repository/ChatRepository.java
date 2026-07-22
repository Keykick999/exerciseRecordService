package com.example.fitnessWeb.chat.repository;

import com.example.fitnessWeb.chat.domain.Chat;
import com.example.fitnessWeb.chat.dto.ChatDto;
import com.example.fitnessWeb.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select c
    from Chat c
    where c.id = :chatId
      and c.isDeleted = false
""")
    Optional<Chat> findByIdForUpdate(@Param("chatId") Long chatId);
}
