package com.example.BI.Chat.dto;

/**
 * Plan de requête produit par le LLM (étape A), validé contre une
 * liste blanche stricte (étape B) puis exécuté en lecture seule
 * avec des requêtes paramétrées (étape C). Aucun texte utilisateur
 * n'est jamais concaténé dans le SQL.
 */
public record QueryPlan(
        String operation,          // count | avg | sum | min | max | none
        String metric,             // baseSalary | employees | annualBudget
        String groupBy,            // department | job | company | manager | gender | employmentType | employmentStatus | hireYear | none
        java.util.Map<String, String> filters, // dimension -> valeur (id ou nom)
        String orderBy,            // value | label | count
        String direction,          // asc | desc
        Integer limit              // 1..50
) {
}
