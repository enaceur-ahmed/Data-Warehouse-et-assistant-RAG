package com.example.BI.Chat;

import com.example.BI.Chat.retrieval.Bm25Retriever;
import com.example.BI.Chat.retrieval.Chunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** BM25 : le bon chunk est retrouvé pour une question
 *  de département / de poste, avec alias FR↔EN. */
class Bm25RetrieverTest {

    private Bm25Retriever retriever;

    @BeforeEach
    void setUp() {
        retriever = new Bm25Retriever();
        retriever.rebuild(List.of(
                new Chunk("kpi-global", "kpi", "KPIs globaux",
                        "KPIs globaux : 50 employés actifs au total ; salaire moyen 4039.00",
                        Map.of("kpi", "global")),
                new Chunk("agg-dept-3", "aggregate", "Agrégat département : Information Technology",
                        "Département Information Technology (ID 3) : 14 employés ; salaire moyen 4757.14 ; min 2520.00 ; max 15500.00 ; masse salariale 66600.00",
                        Map.of("departmentId", "3")),
                new Chunk("agg-dept-2", "aggregate", "Agrégat département : Sales & Marketing",
                        "Département Sales & Marketing (ID 2) : 12 employés ; salaire moyen 3391.67 ; min 2000.00 ; max 8000.00 ; masse salariale 40700.00",
                        Map.of("departmentId", "2")),
                new Chunk("agg-job-8", "aggregate", "Agrégat poste : Developer",
                        "Poste Developer (ID 8) : 4 employés ; salaire moyen 3775.00 ; min 3600.00 ; max 4000.00",
                        Map.of("jobTitleId", "8")),
                new Chunk("agg-manager-19", "manager", "Manager : Mohamed Zouari",
                        "Manager Mohamed Zouari (ID employé 19) : 11 subordonnés ; salaire moyen de l'équipe 2972.73",
                        Map.of("managerId", "19"))
        ));
    }

    @Test
    void topChunkForDepartmentQuestionIsTheRightDepartment() {
        List<Chunk> result = retriever.retrieve(
                "Combien d'employés y a-t-il dans le département IT ?", 3);
        assertFalse(result.isEmpty());
        assertEquals("agg-dept-3", result.get(0).id(),
                "Le chunk du département IT doit arriver en tête");
        assertTrue(result.get(0).content().contains("Information Technology"));
    }

    @Test
    void topChunkForJobQuestionIsTheRightJob() {
        List<Chunk> result = retriever.retrieve(
                "What is the average salary for the Developer job ?", 3);
        assertFalse(result.isEmpty());
        assertEquals("agg-job-8", result.get(0).id());
    }

    @Test
    void managerQuestionRetrievesManagerChunk() {
        List<Chunk> result = retriever.retrieve(
                "Qui est le manager avec le plus de subordonnés ?", 3);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(c -> c.id().equals("agg-manager-19")),
                "Le chunk manager doit être dans le top-3");
    }

    @Test
    void frenchEnglishAliasesAreExpanded() {
        // "employés" (accents retirés → "employes") doit matcher
        // "employes" et son alias anglais "employees"
        List<String> tokens = com.example.BI.Chat.retrieval.TextNormalizer
                .tokenize("combien d'employés");
        assertTrue(tokens.contains("employes"), "token normalisé attendu");
        assertTrue(tokens.contains("employees"),
                "Le token français doit être expandé en anglais");
    }

    @Test
    void emptyQueryReturnsEmpty() {
        assertTrue(retriever.retrieve("", 5).isEmpty());
        assertTrue(retriever.retrieve(null, 5).isEmpty());
    }
}
