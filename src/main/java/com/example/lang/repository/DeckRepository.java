package com.example.lang.repository;

import com.example.lang.entity.Deck;
import com.example.lang.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByFolderId(Long folderId);
    List<Deck> findByUserIdOrderByNameAsc(Long userId);
}