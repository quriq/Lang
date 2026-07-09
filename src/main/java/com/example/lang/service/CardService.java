package com.example.lang.service;

import com.example.lang.entity.Card;
import com.example.lang.entity.Deck;
import com.example.lang.entity.User;
import com.example.lang.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;
    public Card createCard(String frontText, String backText, String exampleSentence, Deck deck) {

        if (frontText == null || frontText.trim().isEmpty()) {
            throw new IllegalArgumentException("Слово не может быть пустым");
        }
        if (backText == null || backText.trim().isEmpty()) {
            throw new IllegalArgumentException("Перевод не может быть пустым");
        }
        Card card = new Card();
        card.setFrontText(frontText);
        card.setBackText(backText);
        card.setExampleSentence(exampleSentence);
        card.setCreatedAt(LocalDateTime.now());
        card.setDueDate(LocalDateTime.now());
        card.setDeck(deck);

        return cardRepository.save(card);
    }

    public List<Card> getCardsByDeck(Long deckId) {
        return cardRepository.findByDeckId(deckId);
    }
    public void deleteCard(Long cardId, User currentUser) throws AccessDeniedException {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта не найдена"));
        if (!card.getDeck().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Нельзя удалить чужую карту");
        }

        cardRepository.delete(card);

    }
}
