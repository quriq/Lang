package com.example.lang.service;


import com.example.lang.entity.Deck;
import com.example.lang.entity.User;
import com.example.lang.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeckService {
    @Autowired
    private DeckRepository deckRepository;

    public Deck createDeck(String name, String targetLanguage, User owner) {
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Название колоды не может быть пустым");
        }

        Deck deck = new Deck();
        deck.setName(name);
        deck.setTargetLanguage(targetLanguage);

        deck.setUser(owner);
        deck.setCreatedAt(LocalDateTime.now());

        return deckRepository.save(deck);
    }
    public List<Deck> getDecksByUser(User user) {
        return deckRepository.findByUserId(user.getId());
    }
}
