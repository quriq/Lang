package com.example.lang.service;

import com.example.lang.entity.Deck;
import com.example.lang.entity.Folder;
import com.example.lang.repository.FolderRepository;
import com.example.lang.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderService {
    @Autowired
    private FolderRepository folderRepository;

    public Folder createFolder(String name, User owner){
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Название папки не может быть пустым");
        }
        Folder folder = new Folder();
        folder.setName(name);

        folder.setUser(owner);
        folder.setCreatedAt(LocalDateTime.now());

        return folderRepository.save(folder);

    }
    public List<Folder> getFoldersByUser(User user){return folderRepository.findByUserId(user.getId());}
    public void deleteFolder(Long folderId, User currentUser) throws AccessDeniedException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Папка не найдена"));
        if (!folder.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Нельзя удалить чужую папку");
        }

        folderRepository.delete(folder);

    }
}
