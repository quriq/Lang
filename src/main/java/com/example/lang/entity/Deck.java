package com.example.lang.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "decks")
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "language", nullable = false)
    private String targetLanguage;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;
    @OneToMany(mappedBy = "deck", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Card> cards;

    public Deck() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Folder getFolder(){return folder;}
    public void setFolder(Folder folder){this.folder=folder;}

}
