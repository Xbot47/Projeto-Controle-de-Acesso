package com.api.apitest.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/status")
    public String status() {
        return "✅ API do Registrador está ONLINE! - " + java.time.LocalDateTime.now();
    }

    @GetMapping("/info")
    public String info() {
        return """
               📋 SISTEMA REGISTRADOR - API MOBILE
               🔧 Spring Boot 3.5.6
               ☕ Java 25
               🌐 Porta: 8080
               🗄️ Banco: EditPro (SQL Server)
               📊 Tabela: Visitantes
               📱 Compatível com React Native
               """;
    }
}