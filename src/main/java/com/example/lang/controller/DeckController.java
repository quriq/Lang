package com.example.lang.controller;

import com.example.lang.entity.Folder;
import com.example.lang.repository.FolderRepository;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.DeckService;
import com.example.lang.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.lang.entity.User;

import java.nio.file.AccessDeniedException;

@Controller
public class DeckController {
    @Autowired
    private DeckService deckService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderRepository folderRepository;
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    @GetMapping("/decks")
    public String showDecksForm(org.springframework.ui.Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("decks", deckService.getDecksByUser(currentUser));
        model.addAttribute("folders", folderService.getFoldersByUser(currentUser));
        return "deck/decks";
    }
    @GetMapping("/decks/new")
    public String showCreateDecksForm(org.springframework.ui.Model model){
        User currentUser = getCurrentUser();
        model.addAttribute("deck",new com.example.lang.entity.Deck());
        model.addAttribute("folders", folderService.getFoldersByUser(currentUser));
        return "deck/new";
    }
    @PostMapping("/decks/new")
    public String processCreateDeck(
            @RequestParam String name,
            @RequestParam String targetLanguage,
            @RequestParam(required = false) Long folderId,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            Folder folder = folderId != null ? folderRepository.findById(folderId).orElse(null) : null;
            deckService.createDeck(name, targetLanguage, currentUser, folder);
            redirectAttributes.addFlashAttribute("successMessage", "Колода успешно создана!");
            return "redirect:/decks";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/decks/new";
        }
    }
    @PostMapping("/decks/{id}/delete")
    public String processDeleteDeck(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            deckService.deleteDeck(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Колода успешно удалена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/decks";
    }
}
