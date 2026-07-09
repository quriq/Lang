package com.example.lang.controller;

import com.example.lang.entity.Folder;
import com.example.lang.entity.User;
import com.example.lang.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FolderController {
    @Autowired
    private FolderService folderService;

    @GetMapping("/folders")
    public String showFolderForm(org.springframework.ui.Model model, @AuthenticationPrincipal User currentUser){
        model.addAttribute("folders", folderService.getFoldersByUser(currentUser));
        return "folder/folders";
    }
    @GetMapping("/folders/new")
    public String showCreateFolder(org.springframework.ui.Model model){
        model.addAttribute("folder",new Folder());
        return "folder/new";
    }
    @PostMapping("/folders/new")
    public String processCreateFolder(
            @RequestParam String name,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes){
        try {
            folderService.createFolder(name, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Папка успешно создана!");
            return "redirect:/folders";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/folders/new";
        }

    }
}
