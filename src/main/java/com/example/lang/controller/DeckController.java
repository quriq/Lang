package com.example.lang.controller;


import com.example.lang.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import com.example.lang.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.lang.entity.User;

@Controller
public class DeckController {
    @Autowired
    private DeckService deckService;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/decks")
    public String showDecksForm(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByLogin(username).orElseThrow();

        model.addAttribute("decks", deckService.getDecksByUser(currentUser));
        return "deck/decks";
    }
    @GetMapping("/decks/new")
    public String showCreateDecksForm(org.springframework.ui.Model model){
        model.addAttribute("deck",new com.example.lang.entity.Deck());
        return "deck/new";
    }
//    @PostMapping("/decks/new")
//    public String processCreateDeck(
//            @RequestParam String name,
//            @RequestParam String targetLanguage,
//            @AuthenticationPrincipal User currentUser,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            deckService.createDeck(name, targetLanguage, currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", true);
//            return "redirect:/decks";
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//            return "redirect:/decks/new";
//        }
//    }
@PostMapping("/decks/new")
public String processCreateDeck(
        @RequestParam String name,
        @RequestParam String targetLanguage,
        RedirectAttributes redirectAttributes) {

    try {
        // Получаем аутентификацию
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new RuntimeException("Не удалось получить аутентификацию");
        }

        String username = auth.getName();
        System.out.println(">>> Текущий пользователь: " + username);

        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        User currentUser = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

        System.out.println(">>> Пользователь найден в БД: " + currentUser.getLogin());

        deckService.createDeck(name, targetLanguage, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Колода успешно создана!");
        return "redirect:/decks";
    } catch (RuntimeException e) {
        System.err.println(">>> ОШИБКА: " + e.getMessage());
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/decks/new";
    }
}



}
