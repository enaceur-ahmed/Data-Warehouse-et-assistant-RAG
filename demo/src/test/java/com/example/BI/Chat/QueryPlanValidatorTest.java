package com.example.BI.Chat;

import com.example.BI.Chat.dto.QueryPlan;
import com.example.BI.Chat.indexing.WarehouseKnowledge;
import com.example.BI.Chat.planning.QueryPlanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Validation stricte : opérations/colonnes inconnues, limites
 *  hors bornes, texte ressemblant à une injection SQL, valeurs
 *  de filtres inconnues. */
class QueryPlanValidatorTest {

    private WarehouseKnowledge knowledge;

    @BeforeEach
    void setUp() {
        knowledge = new WarehouseKnowledge();
        knowledge.addDepartment(3, "Information Technology");
        knowledge.addDepartment(2, "Sales & Marketing");
        knowledge.addJob(8, "Developer");
        knowledge.addCompany(1, "DashMaster Tunisia");
        knowledge.addManager(19, "Mohamed Zouari");
        knowledge.getGenders().add("M");
        knowledge.getGenders().add("F");
        knowledge.getEmploymentTypes().add("Full-Time");
        knowledge.getEmploymentTypes().add("Part-Time");
        knowledge.getEmploymentStatuses().add("Active");
        knowledge.getEmploymentStatuses().add("Inactive");
        knowledge.getHireYears().add(2020);
        knowledge.getHireYears().add(2021);
    }

    private QueryPlan plan(String operation, String metric, String groupBy,
                             Map<String, String> filters, Integer limit) {
        return new QueryPlan(operation, metric, groupBy, filters, "value", "desc", limit);
    }

    @Test
    void validPlanIsAcceptedAndFiltersAreResolved() {
        QueryPlan p = plan("count", "employees", "department",
                Map.of("department", "Information Technology", "gender", "F"), 10);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertTrue(vr.isValid());
        // le nom du département est résolu en id
        assertEquals("3", vr.getResolvedPlan().filters().get("department"));
        assertEquals("F", vr.getResolvedPlan().filters().get("gender"));
    }

    @Test
    void unknownOperationIsRejected() {
        QueryPlan p = plan("average", "baseSalary", "department", null, 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void unknownMetricIsRejected() {
        QueryPlan p = plan("count", "bonus", "department", null, 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void unknownGroupByIsRejected() {
        QueryPlan p = plan("count", "employees", "city", null, 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void unknownFilterKeyIsRejected() {
        QueryPlan p = plan("count", "employees", "department",
                Map.of("hackerColumn", "x"), 10);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertFalse(vr.isValid());
        assertTrue(vr.getErrors().stream().anyMatch(e -> e.contains("clé de filtre inconnue")));
    }

    @Test
    void oversizedLimitIsRejected() {
        QueryPlan p = plan("count", "employees", "department", null, 51);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertFalse(vr.isValid());
        assertTrue(vr.getErrors().stream().anyMatch(e -> e.contains("limite hors bornes")));
    }

    @Test
    void zeroLimitIsRejected() {
        QueryPlan p = plan("count", "employees", "department", null, 0);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void nullLimitDefaultsToDefaultLimit() {
        QueryPlan p = plan("count", "employees", "department", null, null);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertTrue(vr.isValid());
        assertEquals(QueryPlanValidator.DEFAULT_LIMIT, vr.getResolvedPlan().limit());
    }

    @Test
    void sqlInjectionLikeTextInFilterValueIsRejected() {
        QueryPlan p = plan("count", "employees", "department",
                Map.of("department", "3; DROP TABLE Fact_Employees"), 10);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertFalse(vr.isValid());
        assertTrue(vr.getErrors().stream().anyMatch(
                e -> e.contains("caractères interdits") || e.contains("valeur de filtre inconnue")));
    }

    @Test
    void singleQuoteInjectionIsRejected() {
        // l'apostrophe est autorisée pour les noms français, mais une
        // valeur inconnue est rejetée par le contrôle des valeurs connues
        QueryPlan p = plan("count", "employees", "department",
                Map.of("department", "Unknown' OR '1'='1"), 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void unknownFilterValueIsRejected() {
        QueryPlan p = plan("count", "employees", "department",
                Map.of("department", "Département Inexistant"), 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void managerNameIsResolvedToEmployeeId() {
        QueryPlan p = plan("avg", "baseSalary", "job",
                Map.of("manager", "Mohamed Zouari"), 10);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertTrue(vr.isValid(), "errors: " + vr.getErrors());
        assertEquals("19", vr.getResolvedPlan().filters().get("manager"));
    }

    @Test
    void employeesMetricOnlySupportsCount() {
        QueryPlan p = plan("avg", "employees", "department", null, 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void annualBudgetOnlyWithDepartmentOrNone() {
        QueryPlan p = plan("sum", "annualBudget", "job", null, 10);
        assertFalse(QueryPlanValidator.validate(p, knowledge).isValid());

        QueryPlan ok = plan("sum", "annualBudget", "department", null, 10);
        QueryPlanValidator.ValidationResult vr2 = QueryPlanValidator.validate(ok, knowledge);
        assertTrue(vr2.isValid(), "errors: " + vr2.getErrors());
    }

    @Test
    void operationNoneIsValid() {
        QueryPlan p = plan("none", "employees", "none", null, 10);
        assertTrue(QueryPlanValidator.validate(p, knowledge).isValid());
    }

    @Test
    void mixedCaseIdentifiersAreCanonicalized() {
        // le LLM peut retourner "BaseSalary" ou "AnnualBudget" :
        // la validation est insensible à la casse et le plan
        // résolu porte la valeur canonique (attendue par l'exécuteur)
        QueryPlan p = plan("SUM", "BaseSalary", "Department",
                Map.of("EmploymentType", "full-time"), 10);
        QueryPlanValidator.ValidationResult vr = QueryPlanValidator.validate(p, knowledge);
        assertTrue(vr.isValid(), "errors: " + vr.getErrors());
        QueryPlan resolved = vr.getResolvedPlan();
        assertEquals("sum", resolved.operation());
        assertEquals("baseSalary", resolved.metric());
        assertEquals("department", resolved.groupBy());
        assertEquals("Full-Time", resolved.filters().get("employmentType"));
    }

    @Test
    void nullPlanIsRejected() {
        assertFalse(QueryPlanValidator.validate(null, knowledge).isValid());
    }
}
