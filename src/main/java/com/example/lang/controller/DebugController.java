package com.example.lang.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebugController {

    @GetMapping("/debug/env")
    public Map<String, String> checkEnv() {
        return Map.of(
                "JDBC_URL", System.getenv("JDBC_URL") != null ? "SET" : "NOT_SET",
                "DB_USER", System.getenv("DB_USER") != null ? "SET" : "NOT_SET",
                "DB_PASSWORD", System.getenv("DB_PASSWORD") != null ? "SET" : "NOT_SET",
                "PORT", System.getenv("PORT") != null ? System.getenv("PORT") : "NOT_SET"
        );
    }
}