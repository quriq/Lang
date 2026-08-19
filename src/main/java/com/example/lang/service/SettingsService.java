package com.example.lang.service;

import com.example.lang.entity.User;
import com.example.lang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User updateUsername(Long userId, String newLogin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка уникальности
        if (userRepository.findByLogin(newLogin).isPresent()) {
            throw new RuntimeException("Это имя уже занято");
        }

        user.setLogin(newLogin);
        return userRepository.save(user);
    }


    public User updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем старый пароль
        if (!passwordEncoder.matches(oldPassword, user.getPsw())) {
            throw new RuntimeException("Неверный текущий пароль");
        }

        user.setPsw(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }


    public User updateTheme(Long userId, String theme) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!theme.equals("dark") && !theme.equals("light")) {
            throw new RuntimeException("Недопустимая тема");
        }

        user.setTheme(theme);
        return userRepository.save(user);
    }
}