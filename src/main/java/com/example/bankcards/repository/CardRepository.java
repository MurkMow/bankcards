package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("""
           SELECT c FROM Card c
           WHERE c.client.id = :userId
           AND (:status IS NULL OR c.cardStatus = :status)
           """)
    Page<Card> findByClientAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status, Pageable pageable);
}
