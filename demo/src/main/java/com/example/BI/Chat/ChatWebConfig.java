package com.example.BI.Chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enregistre le rate-limiter sur /api/chat/**.
 * La CORS existante (WebConfig) autorise déjà POST
 * depuis http://localhost:5173, 3000 et 4200.
 */
@Configuration
public class ChatWebConfig implements WebMvcConfigurer {

    @Autowired
    private ChatRateLimitInterceptor chatRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(chatRateLimitInterceptor)
                .addPathPatterns("/api/chat/**");
    }
}
