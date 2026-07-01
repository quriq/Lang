package com.example.lang.controller;

import com.example.lang.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // Показ формы регистрации
    @GetMapping("/reg")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new com.example.lang.entity.User());
        return "auth/reg";
    }

    // Обработка отправки формы
    @PostMapping("/reg")
    public String processRegistration(
            @RequestParam String login,
            @RequestParam String psw,
            RedirectAttributes redirectAttributes) {

        try {
            authService.registerUser(login, psw);
            redirectAttributes.addFlashAttribute("success", true);
            return "redirect:/login?success";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reg?error";
        }
    }
}