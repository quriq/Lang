package com.example.lang.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 300)
    private String frontText;
    @Column(nullable = false, length = 300)
    private String backText;
    @Column(name = "exampleSentence", length = 500)
    private String exampleSentence;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "fsrs_difficulty", nullable = false, columnDefinition = "double precision default 5.0")
    private double fsrsDifficulty = 5.0;

    @Column(name = "fsrs_stability", nullable = false, columnDefinition = "double precision default 0.1")
    private double fsrsStability = 0.1;

    @Column(name = "fsrs_last_review")
    private LocalDateTime fsrsLastReview;
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;
    @Column(name = "interval_days", nullable = false, columnDefinition = "integer default 0")
    private int intervalDays = 0;
    @Column(name = "times_correct", nullable = false, columnDefinition = "integer default 0")
    private int timesCorrect = 0;

    @Column(name = "times_wrong", nullable = false, columnDefinition = "integer default 0")
    private int timesWrong = 0;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    public Card() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFrontText() { return frontText; }
    public void setFrontText(String frontText) { this.frontText = frontText; }
    public String getBackText() { return backText; }
    public void setBackText(String backText) { this.backText = backText; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getFsrsDifficulty() { return fsrsDifficulty; }
    public void setFsrsDifficulty(double fsrsDifficulty) { this.fsrsDifficulty = fsrsDifficulty; }

    public double getFsrsStability() { return fsrsStability; }
    public void setFsrsStability(double fsrsStability) { this.fsrsStability = fsrsStability; }

    public LocalDateTime getFsrsLastReview() { return fsrsLastReview; }
    public void setFsrsLastReview(LocalDateTime fsrsLastReview) { this.fsrsLastReview = fsrsLastReview; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }
    public int getTimesCorrect() { return timesCorrect; }
    public void setTimesCorrect(int timesCorrect) { this.timesCorrect = timesCorrect; }

    public int getTimesWrong() { return timesWrong; }
    public void setTimesWrong(int timesWrong) { this.timesWrong = timesWrong; }

    public LocalDateTime getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(LocalDateTime lastReviewed) { this.lastReviewed = lastReviewed; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }
}
