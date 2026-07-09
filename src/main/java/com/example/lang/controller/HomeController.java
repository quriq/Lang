package com.example.lang.controller;

import com.example.lang.dto.DashboardStatsDto;
import com.example.lang.entity.User;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String showDashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByLogin(username).orElseThrow();

        DashboardStatsDto stats = dashboardService.getDashboardStats(currentUser);
        model.addAttribute("stats", stats);

        return "home";
    }
}
