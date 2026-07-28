package com.example.lang.repository;

import com.example.lang.entity.Folder;
import com.example.lang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUserIdOrderByNameAsc(Long userId);

}
