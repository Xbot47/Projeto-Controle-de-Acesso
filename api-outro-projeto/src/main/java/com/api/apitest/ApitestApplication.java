package com.api.apitest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.api.apitest")
@EnableJpaRepositories(basePackages = "com.api.apitest.repository")
public class ApitestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApitestApplication.class, args);
        System.out.println("🎉 ====================================");
        System.out.println("🚀 API EDITPRO INICIADA!");
        System.out.println("📍 URL: http://localhost:8080");
        System.out.println("📚 Endpoints disponíveis:");
        System.out.println("   GET  http://localhost:8080/api/editpro/status");
        System.out.println("   GET  http://localhost:8080/api/editpro/token");
        System.out.println("   POST http://localhost:8080/api/editpro/entrada");
        System.out.println("   GET  http://localhost:8080/api/editpro/visitantes");
        System.out.println("🎉 ====================================");
    }
}