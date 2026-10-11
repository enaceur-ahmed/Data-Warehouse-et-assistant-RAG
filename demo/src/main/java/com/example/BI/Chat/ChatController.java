package com.example.BI.Chat;

import com.example.BI.Chat.dto.ChatRequest;
import com.example.BI.Chat.dto.ChatResponse;
import com.example.BI.Chat.dto.ChatStatusResponse;
import com.example.BI.Chat.service.ReindexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * POST   /api/chat         — question → réponse RAG
 * GET    /api/chat/status  — provider, modèle, clé configurée (true/false)
 * POST   /api/chat/reindex — reconstruction de l'index
 *
 * Le rate-limiting par IP est assuré par ChatRateLimitInterceptor.
 * CORS : la config existante autorise déjà POST depuis 5173/3000/4200.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'message' est requis."));
        }
        if (request.getMessage().length() > ChatSecurity.MAX_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message trop long (" + ChatSecurity.MAX_MESSAGE_LENGTH + " caractères maximum)."));
        }
        try {
            ChatResponse response = chatService.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Dernière ligne de défense : jamais de 500 à cause du LLM
            logger.error("[CHAT] erreur inattendue sur /api/chat", e);
            ChatResponse fallback = new ChatResponse(
                    "Une erreur interne est survenue. Veuillez réessayer.",
                    java.util.List.of(),
                    true,
                    "InternalError");
            return ResponseEntity.ok(fallback);
        }
    }

    @GetMapping("/status")
    public ChatStatusResponse status() {
        return chatService.status();
    }

    @PostMapping("/reindex")
    public ResponseEntity<?> reindex() {
        try {
            chatService.reindex();
            return ResponseEntity.ok(new ReindexResponse(true, "Index reconstruit"));
        } catch (Exception e) {
            logger.error("[CHAT] échec de la réindexation", e);
            return ResponseEntity.ok(new ReindexResponse(false,
                    "Échec : " + e.getClass().getSimpleName()));
        }
    }
}
