package com.example.lang.service;


import com.example.lang.entity.Deck;
import com.example.lang.entity.Folder;
import com.example.lang.entity.User;
import com.example.lang.repository.DeckRepository;
import com.example.lang.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeckService {
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private FolderRepository folderRepository;

    public Deck createDeck(String name, String targetLanguage, User owner, Folder folder) {
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Название колоды не может быть пустым");
        }

        Deck deck = new Deck();
        deck.setName(name);
        deck.setTargetLanguage(targetLanguage);

        deck.setFolder(folder);
        deck.setUser(owner);
        deck.setCreatedAt(LocalDateTime.now());

        return deckRepository.save(deck);
    }
    public List<Deck> getDecksByUser(User user) {
        return deckRepository.findByUserIdOrderByNameAsc(user.getId());
    }
    public void deleteDeck(Long deckId, User currentUser) throws AccessDeniedException {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));
        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Нельзя удалить чужую колоду");
        }

        deckRepository.delete(deck);
    }
public Deck updateDeck(Long deckId, User currentUser, String newName,
                       String newTargetLanguage, Long folderId) throws AccessDeniedException {

    Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));

    if (!deck.getUser().getId().equals(currentUser.getId())) {
        throw new AccessDeniedException("Нельзя редактировать чужую колоду");
    }

    if (newName == null || newName.trim().isEmpty()) {
        throw new IllegalArgumentException("Название колоды не может быть пустым");
    }

    deck.setName(newName);
    deck.setTargetLanguage(newTargetLanguage);

    if (folderId != null && folderId > 0) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Выбранная папка не найдена"));
        deck.setFolder(folder);
    } else {
        deck.setFolder(null);
    }

    return deckRepository.save(deck);
}
}
