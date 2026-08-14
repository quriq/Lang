package com.example.lang.service;

import com.example.lang.entity.Card;
import com.example.lang.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyService {

    @Autowired
    private CardRepository cardRepository;
    public List<Card> getStudyCards(Long deckId) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return cardRepository.findCardsForStudy(deckId, oneDayAgo);
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
    public List<Card> getAllStudyCards(Long userId) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return cardRepository.findAllCardsForStudy(userId, oneDayAgo);
    }
}