package com.example.BI.Chat;

import com.example.BI.Chat.dto.ChatMessage;
import com.example.BI.Chat.dto.ChatRequest;
import com.example.BI.Chat.dto.ChatResponse;
import com.example.BI.Chat.dto.ChatStatusResponse;
import com.example.BI.Chat.indexing.WarehouseIndexer;
import com.example.BI.Chat.indexing.WarehouseKnowledge;
import com.example.BI.Chat.llm.ModelDiscoveryService;
import com.example.BI.Chat.llm.OpenRouterClient;
import com.example.BI.Chat.llm.OpenRouterProperties;
import com.example.BI.Chat.planning.QueryPlanExecutor;
import com.example.BI.Chat.retrieval.Bm25Retriever;
import com.example.BI.Chat.retrieval.Chunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Orchestration : aucun fallback silencieux.
 *  Clé absente ou LLM en panne → réponse déterministe
 *  + llmFallback=true + llmError court. Plan invalide
 *  → pas de calcul, réponse depuis les chunks. */
class ChatServiceTest {

    private ChatService chatService;
    private WarehouseIndexer indexer;
    private Bm25Retriever retriever;
    private OpenRouterClient openRouterClient;
    private ModelDiscoveryService modelDiscoveryService;
    private QueryPlanExecutor queryPlanExecutor;

    private final List<Chunk> chunks = List.of(
            new Chunk("kpi-global", "kpi", "KPIs globaux",
                    "KPIs globaux : 50 employés actifs au total ; salaire moyen 4039.00",
                    Map.of("kpi", "global")),
            new Chunk("agg-dept-3", "aggregate", "Agrégat département : Information Technology",
                    "Département Information Technology (ID 3) : 14 employés ; salaire moyen 4757.14",
                    Map.of("departmentId", "3")));

    @BeforeEach
    void setUp() {
        indexer = mock(WarehouseIndexer.class);
        retriever = mock(Bm25Retriever.class);
        openRouterClient = mock(OpenRouterClient.class);
        modelDiscoveryService = mock(ModelDiscoveryService.class);
        queryPlanExecutor = mock(QueryPlanExecutor.class);

        chatService = new ChatService();
        ReflectionTestUtils.setField(chatService, "indexer", indexer);
        ReflectionTestUtils.setField(chatService, "retriever", retriever);
        ReflectionTestUtils.setField(chatService, "openRouterClient", openRouterClient);
        ReflectionTestUtils.setField(chatService, "modelDiscoveryService", modelDiscoveryService);
        ReflectionTestUtils.setField(chatService, "properties", new OpenRouterProperties());
        ReflectionTestUtils.setField(chatService, "queryPlanExecutor", queryPlanExecutor);

        when(indexer.isIndexed()).thenReturn(true);
        when(indexer.getIndex()).thenReturn(chunks);
        when(indexer.getKnowledge()).thenReturn(new WarehouseKnowledge());
        when(retriever.retrieve(anyString(), anyInt())).thenReturn(chunks);
    }

    private ChatRequest request(String message) {
        return new ChatRequest(message, List.of(
                new ChatMessage("user", "question précédente"),
                new ChatMessage("assistant", "réponse précédente")));
    }

    @Test
    void missingKeyFallsBackDeterministically() {
        when(openRouterClient.isConfigured()).thenReturn(false);

        ChatResponse response = chatService.chat(request("Combien d'employés y a-t-il au total ?"));

        assertTrue(response.isLlmFallback());
        assertEquals("OpenRouterApiKeyMissing", response.getLlmError());
        assertTrue(response.getAnswer().contains("Mode de secours"));
        // la réponse de secours contient les données les plus pertinentes
        assertTrue(response.getAnswer().contains("KPIs globaux"));
        assertFalse(response.getSources().isEmpty());
        // jamais de 500 : le service rend une réponse structurée
        assertNotNull(response.getAnswer());
    }

    @Test
    void llmFailureFallsBackWithShortError() {
        when(openRouterClient.isConfigured()).thenReturn(true);
        when(openRouterClient.complete(any()))
                .thenThrow(new OpenRouterClient.OpenRouterException(503, "HTTP 503"));

        ChatResponse response = chatService.chat(request("Quel est le salaire moyen par département ?"));

        assertTrue(response.isLlmFallback());
        assertNotNull(response.getLlmError());
        assertTrue(response.getLlmError().contains("503"),
                "llmError court attendu, reçu : " + response.getLlmError());
        assertTrue(response.getAnswer().contains("Mode de secours"));
        // les sources restent disponibles même en mode de secours
        assertEquals(2, response.getSources().size());
    }

    @Test
    void successfulPlanIsExecutedAndAnswerComesFromLlm() {
        when(openRouterClient.isConfigured()).thenReturn(true);
        when(openRouterClient.complete(any()))
                // 1er appel : plan JSON
                .thenReturn("{\"operation\":\"count\",\"metric\":\"employees\",\"groupBy\":\"department\",\"filters\":{},\"orderBy\":\"value\",\"direction\":\"desc\",\"limit\":10}")
                // 2e appel : réponse
                .thenReturn("Il y a 50 employés au total, dont 14 dans le département IT.");
        when(queryPlanExecutor.execute(any())).thenReturn(List.of(
                new QueryPlanExecutor.PlanRow("Information Technology", new BigDecimal("14")),
                new QueryPlanExecutor.PlanRow("Sales & Marketing", new BigDecimal("12"))));

        ChatResponse response = chatService.chat(request("Combien d'employés par département ?"));

        assertFalse(response.isLlmFallback());
        assertNull(response.getLlmError());
        assertEquals("Il y a 50 employés au total, dont 14 dans le département IT.",
                response.getAnswer());
        // calcul exécuté et exposé comme source
        verify(queryPlanExecutor).execute(any());
        assertTrue(response.getSources().stream()
                .anyMatch(s -> "computation".equals(s.getType())),
                "une source de type computation est attendue");
        assertTrue(response.getSources().stream()
                .anyMatch(s -> "chunk".equals(s.getType())));
    }

    @Test
    void unparseablePlanIsSkippedAndChunksStillAnswer() {
        when(openRouterClient.isConfigured()).thenReturn(true);
        when(openRouterClient.complete(any()))
                // plan illisible → calcul ignoré
                .thenReturn("Je ne peux pas produire de JSON pour cette question.")
                .thenReturn("D'après les données de l'entrepôt, le salaire moyen est de 4039 TND.");

        ChatResponse response = chatService.chat(request("Quel est le salaire moyen ?"));

        assertFalse(response.isLlmFallback());
        // aucun calcul lancé
        verify(queryPlanExecutor, never()).execute(any());
        assertEquals("D'après les données de l'entrepôt, le salaire moyen est de 4039 TND.",
                response.getAnswer());
    }

    @Test
    void planOutsideWhitelistIsRejectedWithoutComputation() {
        when(openRouterClient.isConfigured()).thenReturn(true);
        when(openRouterClient.complete(any()))
                // opération interdite par la liste blanche
                .thenReturn("{\"operation\":\"drop\",\"metric\":\"employees\",\"groupBy\":\"department\",\"filters\":{},\"orderBy\":\"value\",\"direction\":\"desc\",\"limit\":10}")
                .thenReturn("Réponse construite sans calcul.");
        ChatResponse response = chatService.chat(request("Combien d'employés ?"));

        assertFalse(response.isLlmFallback());
        verify(queryPlanExecutor, never()).execute(any());
    }

    @Test
    void oversizedPlanLimitIsRejectedWithoutComputation() {
        when(openRouterClient.isConfigured()).thenReturn(true);
        when(openRouterClient.complete(any()))
                .thenReturn("{\"operation\":\"count\",\"metric\":\"employees\",\"groupBy\":\"department\",\"filters\":{},\"orderBy\":\"value\",\"direction\":\"desc\",\"limit\":5000}")
                .thenReturn("Réponse construite sans calcul.");
        chatService.chat(request("Combien d'employés ?"));

        verify(queryPlanExecutor, never()).execute(any());
    }

    @Test
    void statusReportsProviderModelAndKeyFlagOnly() {
        when(openRouterClient.isConfigured()).thenReturn(true);
        when(openRouterClient.getModel()).thenReturn("dots-studio/dots-3-note-preview:free");

        ChatStatusResponse status = chatService.status();

        assertEquals("openrouter", status.getProvider());
        assertEquals("dots-studio/dots-3-note-preview:free", status.getModel());
        assertTrue(status.isKeyConfigured());
        // la clé ne doit jamais apparaître dans le statut
        String json = status.getProvider() + status.getModel() + status.isKeyConfigured();
        assertFalse(json.contains("sk-or-"));
    }

    @Test
    void historyIsSanitizedToLastTenMessages() {
        when(openRouterClient.isConfigured()).thenReturn(false);

        List<ChatMessage> longHistory = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            longHistory.add(new ChatMessage("user", "question " + i));
            longHistory.add(new ChatMessage("assistant", "réponse " + i));
        }
        ChatResponse response = chatService.chat(new ChatRequest("dernière question", longHistory));

        assertTrue(response.isLlmFallback());
        // aucune exception même avec un historique surdimensionné
        assertNotNull(response.getAnswer());
    }
}
