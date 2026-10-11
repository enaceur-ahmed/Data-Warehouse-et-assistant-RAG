package com.example.BI.Chat.retrieval;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Normalisation de texte pour BM25 : minuscules, accents retirés,
 * tokens français/anglais/translitérés conservés, expansion par alias
 * (ex. "employé" ↔ "employee", "salaire" ↔ "salary", "société" ↔ "company").
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    /** token -> équivalents à indexer/rechercher en plus du token lui-même */
    private static final Map<String, String[]> ALIASES = Map.ofEntries(
            Map.entry("employe", new String[]{"employee"}),
            Map.entry("employes", new String[]{"employees"}),
            Map.entry("employee", new String[]{"employe"}),
            Map.entry("employees", new String[]{"employes"}),
            Map.entry("departement", new String[]{"department"}),
            Map.entry("departements", new String[]{"departments"}),
            Map.entry("department", new String[]{"departement"}),
            Map.entry("departments", new String[]{"departements"}),
            Map.entry("salaire", new String[]{"salary"}),
            Map.entry("salaires", new String[]{"salaries"}),
            Map.entry("salary", new String[]{"salaire"}),
            Map.entry("salaries", new String[]{"salaires"}),
            Map.entry("moyen", new String[]{"average", "moyenne"}),
            Map.entry("moyenne", new String[]{"average", "moyen"}),
            Map.entry("moyennes", new String[]{"average"}),
            Map.entry("average", new String[]{"moyen", "moyenne"}),
            Map.entry("societe", new String[]{"company"}),
            Map.entry("societes", new String[]{"companies"}),
            Map.entry("company", new String[]{"societe"}),
            Map.entry("companies", new String[]{"societes"}),
            Map.entry("entreprise", new String[]{"company", "societe"}),
            Map.entry("entreprises", new String[]{"companies", "societes"}),
            Map.entry("poste", new String[]{"job"}),
            Map.entry("postes", new String[]{"jobs"}),
            Map.entry("job", new String[]{"poste"}),
            Map.entry("jobs", new String[]{"postes"}),
            Map.entry("manageur", new String[]{"manager"}),
            Map.entry("manageurs", new String[]{"managers"}),
            Map.entry("manager", new String[]{"manageur"}),
            Map.entry("managers", new String[]{"manageurs"}),
            Map.entry("subordonne", new String[]{"subordinate"}),
            Map.entry("subordonnes", new String[]{"subordinates"}),
            Map.entry("subordinate", new String[]{"subordonne"}),
            Map.entry("subordinates", new String[]{"subordonnes"}),
            Map.entry("femme", new String[]{"female"}),
            Map.entry("homme", new String[]{"male"}),
            Map.entry("female", new String[]{"femme"}),
            Map.entry("male", new String[]{"homme"}),
            Map.entry("genre", new String[]{"gender"}),
            Map.entry("gender", new String[]{"genre"}),
            Map.entry("emploi", new String[]{"employment"}),
            Map.entry("employment", new String[]{"emploi"}),
            Map.entry("contrat", new String[]{"employment"}),
            Map.entry("statut", new String[]{"status"}),
            Map.entry("status", new String[]{"statut"}),
            Map.entry("actif", new String[]{"active"}),
            Map.entry("active", new String[]{"actif"}),
            Map.entry("nombre", new String[]{"count", "number"}),
            Map.entry("count", new String[]{"nombre"}),
            Map.entry("number", new String[]{"nombre"}),
            Map.entry("combien", new String[]{"count", "many"}),
            Map.entry("total", new String[]{"sum"}),
            Map.entry("totale", new String[]{"sum"}),
            Map.entry("masse", new String[]{"mass"}),
            Map.entry("plus", new String[]{"most", "top"}),
            Map.entry("minimum", new String[]{"min"}),
            Map.entry("maximum", new String[]{"max"}),
            Map.entry("annee", new String[]{"year"}),
            Map.entry("year", new String[]{"annee"}),
            Map.entry("embauche", new String[]{"hire"}),
            Map.entry("hire", new String[]{"embauche"}),
            Map.entry("budget", new String[]{"budget"})
    );

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        String[] raw = normalized.split("[^a-z0-9]+");
        List<String> tokens = new ArrayList<>();
        for (String token : raw) {
            if (token.length() < 2) {
                continue;
            }
            tokens.add(token);
            String[] aliases = ALIASES.get(token);
            if (aliases != null) {
                for (String alias : aliases) {
                    if (!alias.equals(token)) {
                        tokens.add(alias);
                    }
                }
            }
        }
        return tokens;
    }
}
