package com.example.lang.service;

import com.example.lang.dto.DashboardStatsDto;
import com.example.lang.entity.User;
import com.example.lang.repository.CardRepository;
import com.example.lang.repository.DeckRepository;
import com.example.lang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private CardRepository cardRepository;

    public DashboardStatsDto getDashboardStats(User user) {
        Long userId = user.getId();

        long totalCards = cardRepository.countByDeckUserId(userId);
        long learnedCards = cardRepository.countByDeckUserIdAndStabilityGreaterThan(userId, 1.0);
        int dayStreak = 7; // Заглушка

        return new DashboardStatsDto(totalCards, learnedCards, dayStreak, user.getLogin());
    }
}
