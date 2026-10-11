package com.example.BI.Chat;

import com.example.BI.Chat.dto.QueryPlan;
import com.example.BI.Chat.planning.QueryPlanExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** L'exécuteur construit du SQL uniquement à partir de fragments
 *  en liste blanche et passe les valeurs via setParameter :
 *  aucune concaténation de texte (LLM/utilisateur) dans le SQL. */
class QueryPlanExecutorTest {

    private EntityManager entityManager;
    private Query nativeQuery;
    private QueryPlanExecutor executor;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of(
                new Object[]{"Information Technology", new BigDecimal("14")},
                new Object[]{"Sales & Marketing", new BigDecimal("12")}));
        executor = new QueryPlanExecutor();
        ReflectionTestUtils.setField(executor, "entityManager", entityManager);
    }

    private QueryPlan plan(String operation, String metric, String groupBy,
                             Map<String, String> filters, int limit) {
        return new QueryPlan(operation, metric, groupBy, filters, "value", "desc", limit);
    }

    @Test
    void executesCountByDepartmentWithParameters() {
        QueryPlan p = plan("count", "employees", "department",
                Map.of("gender", "F"), 10);
        List<QueryPlanExecutor.PlanRow> rows = executor.execute(p);

        assertEquals(2, rows.size());
        assertEquals("Information Technology", rows.get(0).label());
        assertEquals(14, rows.get(0).value().intValue());

        // le SQL ne contient JAMAIS la valeur du filtre
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("COUNT(fe.EmployeeID)"), "agrégat count attendu");
        assertTrue(sql.contains("JOIN Dim_Departements dd"), "jointure département attendue");
        assertTrue(sql.contains("fe.Gender = :f_gender"), "filtre paramétré attendu");
        assertFalse(sql.contains("'F'"), "la valeur du filtre ne doit pas être dans le SQL");
        assertFalse(sql.contains(";"), "aucun caractère d'injection dans le SQL");

        // la valeur passe par setParameter
        verify(nativeQuery).setParameter(eq("f_gender"), eq("F"));
        verify(nativeQuery).setMaxResults(10);
    }

    @Test
    void resolvesNumericFilterParameterToInteger() {
        QueryPlan p = plan("avg", "baseSalary", "job",
                Map.of("department", "3"), 5);
        executor.execute(p);
        verify(nativeQuery).setParameter(eq("f_department"), eq(3));
        verify(nativeQuery).setMaxResults(5);
    }

    @Test
    void managerGroupByUsesSelfJoin() {
        QueryPlan p = plan("count", "employees", "manager", null, 10);
        executor.execute(p);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("JOIN Fact_Employees mgr ON fe.ManagerEmployeeID = mgr.EmployeeID"));
        assertTrue(sql.contains("fe.ManagerEmployeeID IS NOT NULL"));
    }

    @Test
    void annualBudgetWithGroupByNoneJoinsDepartments() {
        QueryPlan p = plan("sum", "annualBudget", "none", null, 10);
        // groupBy=none → une seule colonne renvoyée par la DB (le total)
        Object[] totalRow = new Object[]{new BigDecimal("400000.00")};
        when(nativeQuery.getResultList()).thenReturn(List.<Object[]>of(totalRow));
        executor.execute(p);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("SUM(dd.AnnualBudget)"), "SOMME du budget attendue");
        assertTrue(sql.contains("JOIN Dim_Departements dd"),
                "annualBudget nécessite la jointure Dim_Departements même sans groupBy");
    }

    @Test
    void hireYearUsesCastExpression() {
        QueryPlan p = plan("count", "employees", "hireYear",
                Map.of("hireYear", "2020"), 10);
        executor.execute(p);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("CAST(YEAR(fe.HireDate) AS VARCHAR) AS label"));
        assertTrue(sql.contains("YEAR(fe.HireDate) = :f_hireYear"));
    }

    @Test
    void defaultSortIsDescForCount() {
        QueryPlan p = plan("count", "employees", "department", null, 10);
        executor.execute(p);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("ORDER BY value DESC"));
    }

    @Test
    void scalarSingleColumnResultIsMapped() {
        // groupBy=none → la base renvoie une seule colonne :
        // Hibernate retourne la valeur scalaire directement
        // (Integer/BigDecimal), pas un Object[]
        QueryPlan p = plan("count", "employees", "none",
                Map.of("gender", "F"), 10);
        when(nativeQuery.getResultList()).thenReturn(List.<Object>of(22));
        List<QueryPlanExecutor.PlanRow> rows = executor.execute(p);

        assertEquals(1, rows.size());
        assertEquals("Total", rows.get(0).label());
        assertEquals(22, rows.get(0).value().intValue());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("COUNT(fe.EmployeeID) AS value"));
        assertFalse(sql.contains("GROUP BY"), "groupBy=none → pas de GROUP BY");
        assertTrue(sql.contains("fe.Gender = :f_gender"));
        verify(nativeQuery).setParameter(eq("f_gender"), eq("F"));
    }

    @Test
    void whitelistedFragmentsOnly() {
        // Chaque fragment SQL utilisé doit provenir de la liste blanche
        QueryPlan p = plan("max", "baseSalary", "company",
                Map.of("employmentStatus", "Active"), 10);
        executor.execute(p);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue().toUpperCase();
        assertTrue(sql.startsWith("SELECT"));
        assertTrue(sql.contains("MAX(FE.BASESALARY) AS VALUE"));
        assertTrue(sql.contains("JOIN DIM_COMPANY DC"));
        assertTrue(sql.contains("FE.EMPLOYMENTSTATUS = :F_EMPLOYMENTSTATUS"));
        assertTrue(sql.contains("FE.ISACTIVE = 1"));
        // aucune injection possible : pas de DROP/INSERT/UPDATE/DELETE
        assertFalse(sql.contains("DROP"), "DROP interdit");
        assertFalse(sql.contains("INSERT"), "INSERT interdit");
        assertFalse(sql.contains("UPDATE"), "UPDATE interdit");
        assertFalse(sql.contains("DELETE"), "DELETE interdit");
    }
}
