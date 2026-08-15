package com.example.lang.service;

import com.example.lang.entity.Card;
import com.example.lang.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private FsrsService fsrsService;

    public List<Card> getStudyCards(Long deckId) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return cardRepository.findCardsForStudy(deckId, oneDayAgo);
    }

    public List<Card> getAllStudyCards(Long userId) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return cardRepository.findAllCardsForStudy(userId, oneDayAgo);
    }

    public List<Card> getSpacedRepetitionCards(Long deckId) {
        return cardRepository.findCardsForSpacedRepetition(deckId, LocalDateTime.now());
    }

    public List<Card> getAllSpacedRepetitionCards(Long userId) {
        return cardRepository.findAllCardsForSpacedRepetition(userId, LocalDateTime.now());
    }

    public void processAnswer(Card card, boolean isCorrect) {
        if (isCorrect) {
            card.setTimesCorrect(card.getTimesCorrect() + 1);
        } else {
            card.setTimesWrong(card.getTimesWrong() + 1);
        }
        card.setLastReviewed(LocalDateTime.now());
        cardRepository.save(card);
    }

    public void processSpacedRepetitionAnswer(Card card, int rating) {
        LocalDateTime now = LocalDateTime.now();

        int elapsedDays = 0;
        if (card.getFsrsLastReview() != null) {
            elapsedDays = (int) Duration.between(card.getFsrsLastReview(), now).toDays();
        }

        FsrsService.FsrsResult result = fsrsService.processAnswer(
                card.getFsrsDifficulty(),
                card.getFsrsStability(),
                elapsedDays,
                rating
        );

        card.setFsrsDifficulty(result.difficulty());
        card.setFsrsStability(result.stability());
        card.setIntervalDays(result.interval());
        card.setDueDate(now.plusDays(result.interval()));
        card.setFsrsLastReview(now);

        if (rating == 1) {
            card.setTimesWrong(card.getTimesWrong() + 1);
        } else {
            card.setTimesCorrect(card.getTimesCorrect() + 1);
        }
        card.setLastReviewed(now);

        cardRepository.save(card);
    }
}