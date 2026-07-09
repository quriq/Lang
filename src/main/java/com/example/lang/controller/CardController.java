package com.example.lang.controller;

import com.example.lang.entity.Card;
import com.example.lang.entity.Deck;
import com.example.lang.entity.User;
import com.example.lang.repository.DeckRepository;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;


@Controller
public class CardController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardService cardService;
    @Autowired
    private DeckRepository deckRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @GetMapping("/decks/{deckId}/cards/new")
    public String showCreateCardsForm(@PathVariable Long deckId, org.springframework.ui.Model model) {
        User currentUser = getCurrentUser();
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));
        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Нельзя добавлять карточки в чужую колоду");
        }
        model.addAttribute("card", new com.example.lang.entity.Card());
        model.addAttribute("deck", deck);
        return "card/new";
    }

    @PostMapping("/decks/{deckId}/cards/new")
    public String processCreateDeck(
            @PathVariable Long deckId,
            @RequestParam String frontText,
            @RequestParam String backText,
            @RequestParam(required = false) String exampleSentence,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            Deck deck = deckRepository.findById(deckId)
                    .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));

            if (!deck.getUser().getId().equals(currentUser.getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Нельзя добавлять карточки в чужую колоду");
            }
            cardService.createCard(frontText, backText, exampleSentence, deck);
            redirectAttributes.addFlashAttribute("successMessage", "Карточка успешно создана!");
            return "redirect:/decks/" + deckId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/decks/" + deckId + "/cards/new";
        }
    }
    @GetMapping("/decks/{deckId}/cards")
    public String showCardsList(@PathVariable Long deckId, org.springframework.ui.Model model) throws AccessDeniedException {
        User currentUser = getCurrentUser();
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));

        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Нет доступа к карточкам этой колоды");
        }

        List<Card> cards = cardService.getCardsByDeck(deckId);
        model.addAttribute("deck", deck);
        model.addAttribute("cards", cards);
        return "card/list";
    }
    @PostMapping("/cards/{id}/delete")
    public String processDeleteCard(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            cardService.deleteCard(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Карта успешно удалена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/decks";
    }
}
