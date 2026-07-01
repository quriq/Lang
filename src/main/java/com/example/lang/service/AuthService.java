package com.example.lang.service;

import com.example.lang.entity.Role;
import com.example.lang.entity.User;
import com.example.lang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String login, String password) {
        // Проверяем, не занят ли логин
        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("Пользователь с таким логином уже существует");
        }

        // Создаём нового пользователя
        User user = new User();
        user.setLogin(login);
        user.setPsw(passwordEncoder.encode(password)); // Кодируем пароль!
        user.setRole(Role.USER);
        user.setEnabled(true);

        // Сохраняем в БД
        return userRepository.save(user);
    }
}

