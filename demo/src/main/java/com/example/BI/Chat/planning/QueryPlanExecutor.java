package com.example.BI.Chat.planning;

import com.example.BI.Chat.dto.QueryPlan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Exécuteur en LECTURE SEULE du plan validé. Le SQL est construit
 * UNIQUEMENT à partir de fragments en liste blanche ; les valeurs
 * sont passées via setParameter (jamais de concaténation de texte
 * utilisateur/LLM dans le SQL).
 */
@Service
public class QueryPlanExecutor {

    private static final Logger logger = LoggerFactory.getLogger(QueryPlanExecutor.class);

    @PersistenceContext
    private EntityManager entityManager;

    public record PlanRow(String label, BigDecimal value) {
    }

    @Transactional(readOnly = true)
    public List<PlanRow> execute(QueryPlan plan) {
        String operation = plan.operation();
        String metric = plan.metric();
        String groupBy = plan.groupBy();

        StringBuilder sql = new StringBuilder("SELECT ");

        // --- colonne de libellé ---
        boolean hasGroup = !"none".equals(groupBy);
        if (hasGroup) {
            sql.append(labelExpression(groupBy)).append(" AS label, ");
        }
        // --- colonne de valeur ---
        sql.append(metricExpression(metric, operation)).append(" AS value ");

        // --- FROM + jointures ---
        sql.append("FROM Fact_Employees fe ");
        String joins = joinExpression(groupBy);
        // annualBudget a toujours besoin de Dim_Departements
        if ("annualBudget".equals(metric) && !joins.contains("Dim_Departements")) {
            joins = joins + "JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID ";
        }
        sql.append(joins);

        // --- WHERE ---
        List<String> where = new ArrayList<>();
        where.add("fe.IsActive = 1");
        if ("manager".equals(groupBy)) {
            where.add("fe.ManagerEmployeeID IS NOT NULL");
        }
        List<String> paramNames = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();
        for (var entry : (plan.filters() == null ? java.util.Map.<String, String>of() : plan.filters()).entrySet()) {
            where.add(filterPredicate(entry.getKey()));
            paramNames.add("f_" + entry.getKey());
            paramValues.add(castFilterValue(entry.getKey(), entry.getValue()));
        }
        sql.append("WHERE ").append(String.join(" AND ", where));

        // --- GROUP BY ---
        if (hasGroup) {
            sql.append(" GROUP BY ").append(groupExpression(groupBy));
        }

        // --- ORDER BY ---
        String orderBy = plan.orderBy() == null ? null : plan.orderBy().toLowerCase();
        String direction = plan.direction() == null ? null : plan.direction().toLowerCase();
        if (orderBy != null) {
            String orderExpr = "value".equals(orderBy) || "count".equals(orderBy)
                    ? "value"
                    : "label";
            sql.append(" ORDER BY ").append(orderExpr)
               .append(" ").append("asc".equals(direction) ? "ASC" : "DESC");
        } else {
            // tri par défaut : décroissant pour count/sum/max, croissant sinon
            boolean desc = "count".equals(operation) || "sum".equals(operation) || "max".equals(operation);
            sql.append(" ORDER BY value ").append(desc ? "DESC" : "ASC");
        }

        logger.info("[QUERY-PLAN] exécution : opération={} métrique={} groupBy={} filtres={} limit={}",
                operation, metric, groupBy,
                plan.filters() == null ? "{}" : plan.filters().keySet(), plan.limit());

        Query q = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < paramNames.size(); i++) {
            q.setParameter(paramNames.get(i), paramValues.get(i));
        }
        q.setMaxResults(plan.limit());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        List<PlanRow> result = new ArrayList<>();
        for (Object rowObj : rows) {
            // Résultats multi-colonnes → Object[] par ligne ;
            // résultat scalaire (groupBy=none, une seule colonne)
            // → la valeur directement (Hibernate ne boxe pas en tableau).
            Object labelValue = null;
            Object valueValue;
            if (rowObj instanceof Object[] arr) {
                labelValue = hasGroup && arr.length > 0 ? arr[0] : null;
                valueValue = hasGroup ? (arr.length > 1 ? arr[1] : null)
                        : (arr.length > 0 ? arr[0] : null);
            } else {
                valueValue = rowObj;
            }
            String label = hasGroup && labelValue != null
                    ? String.valueOf(labelValue)
                    : "Total";
            BigDecimal value = valueValue == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(String.valueOf(valueValue));
            result.add(new PlanRow(label, value));
        }
        return result;
    }

    // ---------- fragments SQL en liste blanche ----------

    private static String labelExpression(String groupBy) {
        return switch (groupBy) {
            case "department" -> "dd.DepartmentName";
            case "job" -> "dj.JobTitle";
            case "company" -> "dc.CompanyName";
            case "manager" -> "(mgr.FirstName + ' ' + mgr.LastName)";
            case "gender" -> "fe.Gender";
            case "employmentType" -> "fe.EmploymentType";
            case "employmentStatus" -> "fe.EmploymentStatus";
            case "hireYear" -> "CAST(YEAR(fe.HireDate) AS VARCHAR)";
            default -> throw new IllegalArgumentException("groupBy non supporté : " + groupBy);
        };
    }

    private static String groupExpression(String groupBy) {
        return switch (groupBy) {
            case "department" -> "dd.DepartmentID, dd.DepartmentName";
            case "job" -> "dj.JobTitleID, dj.JobTitle";
            case "company" -> "dc.CompanyID, dc.CompanyName";
            case "manager" -> "mgr.EmployeeID, mgr.FirstName, mgr.LastName";
            case "gender" -> "fe.Gender";
            case "employmentType" -> "fe.EmploymentType";
            case "employmentStatus" -> "fe.EmploymentStatus";
            case "hireYear" -> "CAST(YEAR(fe.HireDate) AS VARCHAR)";
            default -> throw new IllegalArgumentException("groupBy non supporté : " + groupBy);
        };
    }

    private static String joinExpression(String groupBy) {
        return switch (groupBy) {
            case "department" -> "JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID ";
            case "job" -> "JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID ";
            case "company" -> "JOIN Dim_Company dc ON fe.CompanyID = dc.CompanyID ";
            case "manager" -> "JOIN Fact_Employees mgr ON fe.ManagerEmployeeID = mgr.EmployeeID ";
            default -> "";
        };
    }

    private static String metricExpression(String metric, String operation) {
        String agg = switch (operation) {
            case "count" -> "COUNT";
            case "avg" -> "AVG";
            case "sum" -> "SUM";
            case "min" -> "MIN";
            case "max" -> "MAX";
            default -> throw new IllegalArgumentException("opération non supportée : " + operation);
        };
        String column = switch (metric) {
            case "baseSalary" -> "fe.BaseSalary";
            case "employees" -> "fe.EmployeeID";
            case "annualBudget" -> "dd.AnnualBudget";
            default -> throw new IllegalArgumentException("métrique non supportée : " + metric);
        };
        return agg + "(" + column + ")";
    }

    private static String filterPredicate(String key) {
        return switch (key) {
            case "department" -> "fe.DepartmentID = :f_department";
            case "job" -> "fe.JobTitleID = :f_job";
            case "company" -> "fe.CompanyID = :f_company";
            case "manager" -> "fe.ManagerEmployeeID = :f_manager";
            case "gender" -> "fe.Gender = :f_gender";
            case "employmentType" -> "fe.EmploymentType = :f_employmentType";
            case "employmentStatus" -> "fe.EmploymentStatus = :f_employmentStatus";
            case "hireYear" -> "YEAR(fe.HireDate) = :f_hireYear";
            default -> throw new IllegalArgumentException("filtre non supporté : " + key);
        };
    }

    private static Object castFilterValue(String key, String value) {
        return switch (key) {
            case "department", "job", "company", "manager", "hireYear" -> Integer.parseInt(value);
            default -> value;
        };
    }
}
