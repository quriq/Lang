package com.example.lang.controller;

import com.example.lang.dto.DashboardStatsDto;
import com.example.lang.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private DashboardService dashboardService;
    @GetMapping("/decks")
    public String showDecksForm(org.springframework.ui.Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("decks", deckService.getDecksByUser(currentUser));
        return "deck/decks";
    }
    @GetMapping("/decks/new")
    public String showCreateDecksForm(org.springframework.ui.Model model){
        model.addAttribute("deck",new com.example.lang.entity.Deck());
        return "deck/new";
    }
    @PostMapping("/decks/new")
    public String processCreateDeck(
            @RequestParam String name,
            @RequestParam String targetLanguage,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            deckService.createDeck(name, targetLanguage, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", true);
            return "redirect:/decks";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/decks/new";
        }
    }
    @GetMapping("/")
    public String showDashboard(org.springframework.ui.Model model, @AuthenticationPrincipal User currentUser) {
        DashboardStatsDto stats = dashboardService.getStats(currentUser);
        model.addAttribute("stats", stats);
        model.addAttribute("decks", deckService.getDecksByUser(currentUser));
        return "home";
    }



}
