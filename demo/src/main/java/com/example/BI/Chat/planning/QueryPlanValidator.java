package com.example.BI.Chat.planning;

import com.example.BI.Chat.dto.QueryPlan;
import com.example.BI.Chat.indexing.WarehouseKnowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validation stricte (liste blanche) du plan de requête produit
 * par le LLM. Vérifie opérations, colonnes, valeurs de filtres
 * (résolues contre l'index) et le plafond de limites.
 * Les valeurs sont RESOLUES en ids ici : l'exécuteur ne reçoit
 * que des ids ou des enum validés, jamais de texte libre.
 */
public final class QueryPlanValidator {

    private QueryPlanValidator() {
    }

    public static final Set<String> OPERATIONS = Set.of("count", "avg", "sum", "min", "max", "none");
    public static final Set<String> METRICS = Set.of("baseSalary", "employees", "annualBudget");
    public static final Set<String> GROUP_BY = Set.of("department", "job", "company", "manager",
            "gender", "employmentType", "employmentStatus", "hireYear", "none");
    public static final Set<String> ORDER_BY = Set.of("value", "label", "count");
    public static final Set<String> DIRECTIONS = Set.of("asc", "desc");
    public static final Set<String> FILTER_KEYS = Set.of("department", "job", "company", "manager",
            "gender", "employmentType", "employmentStatus", "hireYear");
    public static final int MAX_LIMIT = 50;
    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_FILTER_VALUE_LENGTH = 100;

    /** Caractères autorisés dans une valeur de filtre (défense en profondeur). */
    private static final java.util.regex.Pattern SAFE_VALUE =
            java.util.regex.Pattern.compile("^[A-Za-z0-9À-ÿ&+./()'\\- ]+$");

    public static final class ValidationResult {
        private final boolean valid;
        private final QueryPlan resolvedPlan; // filtres résolus en ids / valeurs validées
        private final List<String> errors;

        ValidationResult(boolean valid, QueryPlan resolvedPlan, List<String> errors) {
            this.valid = valid;
            this.resolvedPlan = resolvedPlan;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public QueryPlan getResolvedPlan() { return resolvedPlan; }
        public List<String> getErrors() { return errors; }
    }

    public static ValidationResult validate(QueryPlan plan, WarehouseKnowledge knowledge) {
        List<String> errors = new ArrayList<>();
        if (plan == null) {
            return new ValidationResult(false, null, List.of("plan null"));
        }

        // --- opération ---
        String operation = canonical(OPERATIONS, plan.operation());
        if (operation == null) {
            errors.add("opération inconnue : " + safe(plan.operation()));
        }

        // --- métrique ---
        String metric = canonical(METRICS, plan.metric());
        if (metric == null) {
            errors.add("métrique inconnue : " + safe(plan.metric()));
        }

        // --- regroupement ---
        String groupBy = canonical(GROUP_BY, plan.groupBy());
        if (groupBy == null) {
            errors.add("groupBy inconnu : " + safe(plan.groupBy()));
        }

        // --- cohérence métrique / opération / groupBy ---
        if (operation != null && !"none".equals(operation)) {
            if (metric != null && "employees".equals(metric) && !"count".equals(operation)) {
                errors.add("la métrique employees ne supporte que l'opération count");
            }
            if (metric != null && "annualBudget".equals(metric)) {
                if (!"sum".equals(operation)) {
                    errors.add("la métrique annualBudget ne supporte que l'opération sum");
                }
                if (groupBy != null && !"department".equals(groupBy) && !"none".equals(groupBy)) {
                    errors.add("la métrique annualBudget ne peut être groupée que par department");
                }
            }
        }

        // --- filtres : clés connues, valeurs sûres et résolues ---
        Map<String, String> rawFilters = plan.filters() == null ? Map.of() : plan.filters();
        Map<String, String> resolvedFilters = new HashMap<>();
        for (Map.Entry<String, String> entry : rawFilters.entrySet()) {
            String key = canonical(FILTER_KEYS, entry.getKey());
            String value = entry.getValue();
            if (key == null) {
                errors.add("clé de filtre inconnue : " + safe(entry.getKey()));
                continue;
            }
            if (value == null || value.isBlank()) {
                errors.add("filtre vide : " + key);
                continue;
            }
            if (value.length() > MAX_FILTER_VALUE_LENGTH) {
                errors.add("valeur de filtre trop longue : " + key);
                continue;
            }
            if (!SAFE_VALUE.matcher(value).matches()) {
                errors.add("caractères interdits dans le filtre : " + key);
                continue;
            }
            String resolved = resolveFilterValue(key, value.trim(), knowledge, errors);
            if (resolved != null) {
                resolvedFilters.put(key, resolved);
            }
        }

        // --- tri ---
        String orderBy = lower(plan.orderBy());
        if (orderBy != null && !orderBy.isEmpty() && !ORDER_BY.contains(orderBy)) {
            errors.add("orderBy inconnu : " + safe(plan.orderBy()));
        }
        String direction = lower(plan.direction());
        if (direction != null && !direction.isEmpty() && !DIRECTIONS.contains(direction)) {
            errors.add("direction inconnue : " + safe(plan.direction()));
        }

        // --- limite ---
        int limit = DEFAULT_LIMIT;
        if (plan.limit() != null) {
            if (plan.limit() < 1 || plan.limit() > MAX_LIMIT) {
                errors.add("limite hors bornes (1.." + MAX_LIMIT + ") : " + plan.limit());
            } else {
                limit = plan.limit();
            }
        }

        if (!errors.isEmpty()) {
            return new ValidationResult(false, null, errors);
        }

        QueryPlan resolved = new QueryPlan(operation, metric, groupBy, resolvedFilters,
                orderBy == null || orderBy.isEmpty() ? null : orderBy,
                direction == null || direction.isEmpty() ? null : direction,
                limit);
        return new ValidationResult(true, resolved, List.of());
    }

    /** Résout une valeur de filtre (id numérique ou nom) contre l'index. */
    private static String resolveFilterValue(String key, String value,
                                             WarehouseKnowledge knowledge, List<String> errors) {
        Integer id = switch (key) {
            case "department" -> knowledge.resolveDepartment(value);
            case "job" -> knowledge.resolveJob(value);
            case "company" -> knowledge.resolveCompany(value);
            case "manager" -> knowledge.resolveManager(value);
            default -> null;
        };
        if (id != null) {
            return String.valueOf(id);
        }
        // filtres à valeurs énumérées (insensible à la casse,
        // renvoie la valeur canonique de l'entrepôt)
        String canonicalValue = switch (key) {
            case "gender" -> canonicalEnum(knowledge.getGenders(), value);
            case "employmentType" -> canonicalEnum(knowledge.getEmploymentTypes(), value);
            case "employmentStatus" -> canonicalEnum(knowledge.getEmploymentStatuses(), value);
            case "hireYear" -> {
                try {
                    yield knowledge.getHireYears().contains(Integer.parseInt(value.trim()))
                            ? value.trim() : null;
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
        if (canonicalValue != null) {
            return canonicalValue;
        }
        errors.add("valeur de filtre inconnue pour " + key + " : " + safe(value));
        return null;
    }

    /** Correspondance insensible à la casse contre une liste blanche. */
    private static String canonical(Set<String> whitelist, String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        for (String candidate : whitelist) {
            if (candidate.equalsIgnoreCase(trimmed)) {
                return candidate;
            }
        }
        return null;
    }

    private static String canonicalEnum(Set<String> allowed, String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (String candidate : allowed) {
            if (candidate.equalsIgnoreCase(trimmed)) {
                return candidate;
            }
        }
        return null;
    }

    private static String lower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /** Tronque pour ne jamais polluer les logs avec du texte brut. */
    private static String safe(String s) {
        if (s == null) {
            return "null";
        }
        return s.length() > 40 ? s.substring(0, 40) + "…" : s;
    }
}
