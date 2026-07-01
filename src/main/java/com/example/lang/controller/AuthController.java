package com.example.lang.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/reg")
    public String showRegistrationForm(Model model) {
        // Передаём пустой объект User в форму
        // Важно: класс User должен существовать!
        model.addAttribute("user", new com.example.lang.entity.User());
        return "auth/reg";
    }
}