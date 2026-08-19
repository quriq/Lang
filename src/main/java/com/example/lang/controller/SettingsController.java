package com.example.lang.controller;

import com.example.lang.entity.User;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || "anonymousUser".equals(username)) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Страница настроек
     */
    @GetMapping("/settings")
    public String showSettings(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("user", currentUser);
        return "settings";
    }

    /**
     * Обработка смены имени
     */
    @PostMapping("/settings/username")
    public String updateUsername(@RequestParam String newLogin,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser();
            settingsService.updateUsername(currentUser.getId(), newLogin);

            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            newLogin,
                            null,
                            SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            }

            redirectAttributes.addFlashAttribute("successMessage", "Имя успешно изменено!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings";
    }

    /**
     * Обработка смены пароля
     */
    @PostMapping("/settings/password")
    public String updatePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            settingsService.updatePassword(currentUser.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно изменён!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings";
    }

    /**
     * Обработка смены темы
     */
    @PostMapping("/settings/theme")
    public String updateTheme(@RequestParam String theme,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            settingsService.updateTheme(currentUser.getId(), theme);
            redirectAttributes.addFlashAttribute("successMessage", "Тема изменена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings";
    }

    /**
     * Заглушка для достижений
     */
    @GetMapping("/achievements")
    public String showAchievements() {
        return "achievements";
    }
}