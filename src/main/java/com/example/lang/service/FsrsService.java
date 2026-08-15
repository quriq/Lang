package com.example.lang.service;

import org.springframework.stereotype.Service;

@Service
public class FsrsService {

    // Параметры FSRS-6 (стандартные веса)
    private static final double[] WEIGHTS = {
            0.4072, 1.1829, 3.1262, 15.4722, 7.2102, 0.5316,
            1.0651, 0.0234, 1.616, 0.0, 1.53, 0.115
    };

    private static final double REQUEST_RETENTION = 0.9; // 90% вероятность вспоминания

    public double nextStability(double difficulty, double stability, int elapsedDays, int rating) {
        // rating: 1=Again, 2=Hard, 3=Good, 4=Easy
        double hardPenalty = (rating == 2) ? 0.8 : 1.0;
        double easyBonus = (rating == 4) ? 1.15 : 1.0;

        double delta = WEIGHTS[4] * (0.1 - difficulty) +
                WEIGHTS[5] * (Math.log10(elapsedDays + 1) - Math.log10(stability)) +
                WEIGHTS[6] * (Math.log10(elapsedDays + 1) / Math.log10(stability + 1));

        double newStability = stability * (1.0 + Math.exp(delta)) * hardPenalty * easyBonus;
        return Math.max(newStability, 0.1);
    }

    public double nextDifficulty(double difficulty, int rating) {
        double delta = WEIGHTS[7] * (rating - 3);
        double newDifficulty = difficulty + delta;
        return Math.max(1.0, Math.min(10.0, newDifficulty));
    }

    public int optimalInterval(double stability) {
        double interval = stability * Math.log(1.0 / REQUEST_RETENTION);
        return Math.max(1, (int) Math.round(interval));
    }

    public FsrsResult processAnswer(double currentDifficulty, double currentStability,
                                    int elapsedDays, int rating) {
        double difficulty, stability;

        if (rating == 1) {
            // Сброс при "Again"
            stability = 0.1;
            difficulty = nextDifficulty(currentDifficulty, rating);
        } else {
            // Успешное повторение
            stability = nextStability(currentDifficulty, currentStability, elapsedDays, rating);
            difficulty = nextDifficulty(currentDifficulty, rating);
        }

        int interval = optimalInterval(stability);
        return new FsrsResult(difficulty, stability, interval);
    }

    public record FsrsResult(double difficulty, double stability, int interval) {}
}
