package com.example.lang.repository;

import com.example.lang.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByDeckId(Long deckId);
    List<Card> findByDeckIdAndDueDateBefore(Long deckId, LocalDateTime date);
    long countByDeck_User_Id(Long userId);
    long countByDeck_User_IdAndStabilityGreaterThan(Long userId, double threshold);
}