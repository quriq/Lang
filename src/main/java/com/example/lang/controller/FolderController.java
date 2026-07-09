package com.example.lang.controller;

import com.example.lang.entity.Deck;
import com.example.lang.entity.Folder;
import com.example.lang.entity.User;
import com.example.lang.repository.DeckRepository;
import com.example.lang.repository.FolderRepository;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FolderController {
    @Autowired
    private FolderService folderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private DeckRepository deckRepository;
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @GetMapping("/folders")
    public String showFolderForm(org.springframework.ui.Model model){
        User currentUser = getCurrentUser();
        model.addAttribute("folders", folderService.getFoldersByUser(currentUser));
        return "folder/folders";
    }
    @GetMapping("/folders/new")
    public String showCreateFolder(org.springframework.ui.Model model){
        model.addAttribute("folder",new Folder());
        return "folder/new";
    }
    @GetMapping("/folders/{id}")
    public String showFolderDecks(@PathVariable Long id, org.springframework.ui.Model model) {
        Folder folders = folderRepository.findById(id).orElseThrow();
        List<Deck> decks = deckRepository.findByFolderId(id);

        model.addAttribute("folders", folders);
        model.addAttribute("decks", decks);
        return "deck/decks";
    }
    @PostMapping("/folders/new")
    public String processCreateFolder(
            @RequestParam String name,
            RedirectAttributes redirectAttributes){
        try {
            User currentUser = getCurrentUser();
            folderService.createFolder(name, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Папка успешно создана!");
            return "redirect:/folders";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/folders/new";
        }

    }
}
