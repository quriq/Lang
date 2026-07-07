package com.example.lang.dto;

public class DashboardStatsDto {
    private long totalCards;
    private long learnedCards;
    private int dayStreak;
    private String userName;

    public long getTotalCards() {
        return totalCards;
    }

    public void setTotalCards(long totalCards) {
        this.totalCards = totalCards;
    }

    public long getLearnedCards() {
        return learnedCards;
    }

    public void setLearnedCards(long learnedCards) {
        this.learnedCards = learnedCards;
    }

    public int getDayStreak() {
        return dayStreak;
    }

    public void setDayStreak(int dayStreak) {
        this.dayStreak = dayStreak;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
