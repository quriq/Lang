package com.example.lang.repository;

import com.example.lang.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByDeckId(Long deckId);
    List<Card> findByDeckIdAndDueDateBefore(Long deckId, LocalDateTime date);
    long countByDeckUserId(Long userId);
    long countByDeckUserIdAndTimesCorrectGreaterThanEqual(Long userId, int threshold);
    List<Card> findByDeckIdOrderByCreatedAtDesc(Long deckId);

    @Query(value = "SELECT * FROM cards WHERE deck_id = :deckId " +
            "ORDER BY " +
            "CASE " +
            "  WHEN times_wrong > 0 AND times_wrong > times_correct THEN 1 " +
            "  WHEN times_correct = 0 AND times_wrong = 0 THEN 2 " +
            "  WHEN last_reviewed IS NOT NULL AND last_reviewed < :oneDayAgo THEN 3 " +
            "  ELSE 4 " +
            "END, " +
            "CASE " +
            "  WHEN times_wrong > 0 AND times_wrong > times_correct THEN times_wrong END DESC, " +
            "CASE " +
            "  WHEN times_correct = 0 AND times_wrong = 0 THEN created_at END ASC, " +
            "CASE " +
            "  WHEN last_reviewed IS NOT NULL AND last_reviewed < :oneDayAgo THEN last_reviewed END ASC, " +
            "times_correct ASC",
            nativeQuery = true)
    List<Card> findCardsForStudy(@Param("deckId") Long deckId,
                                 @Param("oneDayAgo") LocalDateTime oneDayAgo);

    @Query(value = "SELECT c.* FROM cards c " +
            "JOIN decks d ON c.deck_id = d.id " +
            "WHERE d.user_id = :userId " +
            "ORDER BY " +
            "CASE " +
            "  WHEN c.times_wrong > 0 AND c.times_wrong > c.times_correct THEN 1 " +
            "  WHEN c.times_correct = 0 AND c.times_wrong = 0 THEN 2 " +
            "  WHEN c.last_reviewed IS NOT NULL AND c.last_reviewed < :oneDayAgo THEN 3 " +
            "  ELSE 4 " +
            "END, " +
            "CASE " +
            "  WHEN c.times_wrong > 0 AND c.times_wrong > c.times_correct THEN c.times_wrong END DESC, " +
            "CASE " +
            "  WHEN c.times_correct = 0 AND c.times_wrong = 0 THEN c.created_at END ASC, " +
            "CASE " +
            "  WHEN c.last_reviewed IS NOT NULL AND c.last_reviewed < :oneDayAgo THEN c.last_reviewed END ASC, " +
            "c.times_correct ASC",
            nativeQuery = true)
    List<Card> findAllCardsForStudy(@Param("userId") Long userId,
                                    @Param("oneDayAgo") LocalDateTime oneDayAgo);

    @Query(value = "SELECT c.* FROM cards c " +
            "WHERE c.deck_id = :deckId AND c.due_date <= :now " +
            "ORDER BY c.due_date ASC, c.times_wrong DESC",
            nativeQuery = true)
    List<Card> findCardsForSpacedRepetition(@Param("deckId") Long deckId,
                                            @Param("now") LocalDateTime now);

    @Query(value = "SELECT c.* FROM cards c " +
            "JOIN decks d ON c.deck_id = d.id " +
            "WHERE d.user_id = :userId AND c.due_date <= :now " +
            "ORDER BY c.due_date ASC, c.times_wrong DESC",
            nativeQuery = true)
    List<Card> findAllCardsForSpacedRepetition(@Param("userId") Long userId,
                                               @Param("now") LocalDateTime now);
    }
