package com.example.BI.Chat;

import com.example.BI.Chat.indexing.WarehouseIndexer;
import com.example.BI.Entity.DimCompany;
import com.example.BI.Entity.DimDepartement;
import com.example.BI.Entity.DimJob;
import com.example.BI.Repository.ChatAnalyticsRepository;
import com.example.BI.Repository.DimCompanyRepository;
import com.example.BI.Repository.DimDepartementRepository;
import com.example.BI.Repository.DimJobRepository;
import com.example.BI.Repository.EmployeeAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Indexation : chunks construits depuis l'entrepôt,
 *  emails/contact EXCLUS, plafonds respectés,
 *  génération incrémentée à chaque réindexation. */
class WarehouseIndexerTest {

    private WarehouseIndexer indexer;
    private ChatAnalyticsRepository chatAnalyticsRepository;
    private EmployeeAnalyticsRepository employeeAnalyticsRepository;

    @BeforeEach
    void setUp() {
        indexer = new WarehouseIndexer();

        DimCompanyRepository dimCompanyRepository = mock(DimCompanyRepository.class);
        DimDepartementRepository dimDepartementRepository = mock(DimDepartementRepository.class);
        DimJobRepository dimJobRepository = mock(DimJobRepository.class);
        chatAnalyticsRepository = mock(ChatAnalyticsRepository.class);
        employeeAnalyticsRepository = mock(EmployeeAnalyticsRepository.class);

        ReflectionTestUtils.setField(indexer, "dimCompanyRepository", dimCompanyRepository);
        ReflectionTestUtils.setField(indexer, "dimDepartementRepository", dimDepartementRepository);
        ReflectionTestUtils.setField(indexer, "dimJobRepository", dimJobRepository);
        ReflectionTestUtils.setField(indexer, "chatAnalyticsRepository", chatAnalyticsRepository);
        ReflectionTestUtils.setField(indexer, "employeeAnalyticsRepository", employeeAnalyticsRepository);

        // --- Société (avec email qui ne doit JAMAIS fuir dans un chunk) ---
        DimCompany company = new DimCompany();
        company.setCompanyId(1);
        company.setCompanyCode("DMT");
        company.setCompanyName("DashMaster Tunisia");
        company.setCity("Tunis");
        company.setCountry("Tunisia");
        company.setWebsite("dashmaster.tn");
        company.setEmail("contact@dashmaster.tn");
        company.setPhone("+216 12345678");
        company.setIsActive(true);
        when(dimCompanyRepository.findAll()).thenReturn(List.of(company));

        // --- Départements ---
        DimDepartement it = new DimDepartement();
        it.setDepartmentId(3);
        it.setDepartmentCode("IT");
        it.setDepartmentName("Information Technology");
        it.setCompanyId(1);
        it.setAnnualBudget(new BigDecimal("250000"));
        it.setIsActive(true);
        DimDepartement sales = new DimDepartement();
        sales.setDepartmentId(2);
        sales.setDepartmentCode("SM");
        sales.setDepartmentName("Sales & Marketing");
        sales.setCompanyId(1);
        sales.setAnnualBudget(new BigDecimal("150000"));
        sales.setIsActive(true);
        when(dimDepartementRepository.findAll()).thenReturn(List.of(it, sales));

        // --- Postes ---
        DimJob dev = new DimJob();
        dev.setJobTitleId(8);
        dev.setJobTitleCode("DEV");
        dev.setJobTitle("Developer");
        dev.setJobLevel(3);
        dev.setMinSalary(new BigDecimal("2500"));
        dev.setMaxSalary(new BigDecimal("8000"));
        dev.setIsActive(true);
        when(dimJobRepository.findAll()).thenReturn(List.of(dev));

        // --- Agrégats ---
        when(chatAnalyticsRepository.getDepartmentAggregates()).thenReturn(List.of(
                aggRow(3, "Information Technology", 14L, "4757.14", "2520.00", "15500.00", "66600.00")));
        when(chatAnalyticsRepository.getGenderSplitByDepartment()).thenReturn(List.of(
                splitRow(3, "M", 8L), splitRow(3, "F", 6L)));
        when(chatAnalyticsRepository.getEmploymentTypeSplitByDepartment()).thenReturn(List.of(
                splitRow(3, "Full-Time", 14L)));
        when(chatAnalyticsRepository.getJobAggregates()).thenReturn(List.of(
                aggRow(8, "Developer", 4L, "3775.00", "3600.00", "4000.00", "15100.00")));
        when(chatAnalyticsRepository.getGenderSplitByJob()).thenReturn(List.of(
                splitRow(8, "M", 4L)));
        when(chatAnalyticsRepository.getEmploymentTypeSplitByJob()).thenReturn(List.of(
                splitRow(8, "Full-Time", 4L)));
        when(chatAnalyticsRepository.getCompanyAggregates()).thenReturn(List.of(
                aggRow(1, "DashMaster Tunisia", 50L, "4039.00", "1750.00", "18000.00", "201950.00")));
        when(chatAnalyticsRepository.getManagerAggregates()).thenReturn(List.of(
                Map.of("managerId", 19, "managerName", "Mohamed Zouari",
                        "subordinateCount", 11L, "averageSalary", new BigDecimal("2972.73"),
                        "minSalary", new BigDecimal("1750.00"), "maxSalary", new BigDecimal("8000.00"))));
        when(chatAnalyticsRepository.getActiveDepartmentCount()).thenReturn(Map.of("cnt", 10L));
        when(chatAnalyticsRepository.getActiveManagerCount()).thenReturn(Map.of("cnt", 8L));
        when(chatAnalyticsRepository.getDistinctGenders()).thenReturn(List.of(
                Map.of("value", "M"), Map.of("value", "F")));
        when(chatAnalyticsRepository.getDistinctEmploymentTypes()).thenReturn(List.of(
                Map.of("value", "Full-Time"), Map.of("value", "Part-Time")));
        when(chatAnalyticsRepository.getDistinctEmploymentStatuses()).thenReturn(List.of(
                Map.of("value", "Active"), Map.of("value", "Inactive")));
        when(chatAnalyticsRepository.getDistinctHireYears()).thenReturn(List.of(
                Map.of("value", 2020), Map.of("value", 2021)));
        when(chatAnalyticsRepository.getDistinctCurrencies()).thenReturn(List.of(
                Map.of("value", "TND")));

        // --- KPIs globaux ---
        when(employeeAnalyticsRepository.getGlobalKpis(null, null, null, null)).thenReturn(Map.of(
                "totalEmployees", 50L,
                "averageSalary", new BigDecimal("4039.00"),
                "minSalary", new BigDecimal("1750.00"),
                "maxSalary", new BigDecimal("18000.00"),
                "totalSalaryMass", new BigDecimal("201950.00")));

        // --- Cartes employés ---
        List<Map<String, Object>> employees = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> e = new HashMap<>();
            e.put("employeeId", i);
            e.put("firstName", "Employé" + i);
            e.put("lastName", "Test" + i);
            e.put("jobTitle", "Developer");
            e.put("jobTitleId", 8);
            e.put("departmentName", "Information Technology");
            e.put("departmentId", 3);
            e.put("companyName", "DashMaster Tunisia");
            e.put("managerName", "Mohamed Zouari");
            e.put("gender", i % 2 == 0 ? "F" : "M");
            e.put("employmentType", "Full-Time");
            e.put("employmentStatus", "Active");
            e.put("baseSalary", new BigDecimal("3000.00"));
            e.put("hireDate", "2020-01-1" + i);
            employees.add(e);
        }
        when(chatAnalyticsRepository.getEmployeeCards()).thenReturn(employees);
    }

    @Test
    void buildsChunksAndKnowledgeFromWarehouse() {
        indexer.reindex();

        assertTrue(indexer.isIndexed());
        assertFalse(indexer.getIndex().isEmpty());

        // cartes de dimensions
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("company-1")));
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("department-3")));
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("job-8")));
        // agrégats
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("agg-dept-3")));
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("agg-job-8")));
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("agg-manager-19")));
        // KPI global toujours présent
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("kpi-global")));
        // chunks employés
        assertTrue(indexer.getIndex().stream().anyMatch(c -> c.id().equals("employee-1")));

        // connaissances pour la validation des filtres
        assertEquals("Information Technology", indexer.getKnowledge().getDepartmentsById().get(3));
        assertEquals("Developer", indexer.getKnowledge().getJobsById().get(8));
        assertEquals("DashMaster Tunisia", indexer.getKnowledge().getCompaniesById().get(1));
        assertEquals("Mohamed Zouari", indexer.getKnowledge().getManagersById().get(19));
        assertTrue(indexer.getKnowledge().getGenders().contains("F"));
        assertTrue(indexer.getKnowledge().getEmploymentTypes().contains("Full-Time"));
        assertTrue(indexer.getKnowledge().getHireYears().contains(2020));
        assertTrue(indexer.getKnowledge().getCurrencies().contains("TND"));
    }

    @Test
    void emailsAndContactDataNeverAppearInChunks() {
        indexer.reindex();
        for (var chunk : indexer.getIndex()) {
            assertFalse(chunk.label().contains("@"),
                    "email dans le label : " + chunk.label());
            assertFalse(chunk.content().contains("@"),
                    "email dans le contenu du chunk " + chunk.id());
            assertFalse(chunk.content().contains("+216"),
                    "téléphone dans le chunk " + chunk.id());
            assertFalse(chunk.content().contains("contact@dashmaster.tn"),
                    "email de la société dans le chunk " + chunk.id());
        }
    }

    @Test
    void departmentAggregateContainsSplits() {
        indexer.reindex();
        var agg = indexer.getIndex().stream()
                .filter(c -> c.id().equals("agg-dept-3"))
                .findFirst().orElseThrow();
        // répartition genre et type de contrat incluses
        assertTrue(agg.content().contains("M 8"));
        assertTrue(agg.content().contains("F 6"));
        assertTrue(agg.content().contains("Full-Time 14"));
        // nombres exacts
        assertTrue(agg.content().contains("14 employés"));
        assertTrue(agg.content().contains("4757.14"));
    }

    @Test
    void employeeChunksAreCapped() {
        // 450 cartes employés → plafond MAX_EMPLOYEE_CHUNKS (400)
        List<Map<String, Object>> many = new ArrayList<>();
        for (int i = 1; i <= 450; i++) {
            Map<String, Object> e = new HashMap<>();
            e.put("employeeId", i);
            e.put("firstName", "Prenom" + i);
            e.put("lastName", "Nom" + i);
            e.put("jobTitle", "Developer");
            e.put("jobTitleId", 8);
            e.put("departmentName", "Information Technology");
            e.put("departmentId", 3);
            e.put("companyName", "DashMaster Tunisia");
            e.put("managerName", "Mohamed Zouari");
            e.put("gender", "M");
            e.put("employmentType", "Full-Time");
            e.put("employmentStatus", "Active");
            e.put("baseSalary", new BigDecimal("3000.00"));
            e.put("hireDate", "2020-01-01");
            many.add(e);
        }
        when(chatAnalyticsRepository.getEmployeeCards()).thenReturn(many);

        indexer.reindex();
        long employeeChunks = indexer.getIndex().stream()
                .filter(c -> c.id().startsWith("employee-")).count();
        assertEquals(WarehouseIndexer.MAX_EMPLOYEE_CHUNKS, employeeChunks,
                "Les chunks employés doivent être plafonnés");
        assertTrue(indexer.getIndex().size() <= WarehouseIndexer.MAX_CHUNKS,
                "Le nombre total de chunks doit être plafonné");
    }

    @Test
    void reindexIncrementsGeneration() {
        indexer.reindex();
        int g1 = indexer.getGeneration();
        indexer.reindex();
        assertEquals(g1 + 1, indexer.getGeneration());
    }

    // ---------- helpers ----------

    private static Map<String, Object> aggRow(int dimensionId, String dimensionName,
                                                long count, String avg, String min,
                                                String max, String mass) {
        Map<String, Object> row = new HashMap<>();
        row.put("dimensionId", dimensionId);
        row.put("dimensionName", dimensionName);
        row.put("employeeCount", count);
        row.put("averageSalary", new BigDecimal(avg));
        row.put("minSalary", new BigDecimal(min));
        row.put("maxSalary", new BigDecimal(max));
        row.put("totalSalaryMass", new BigDecimal(mass));
        return row;
    }

    private static Map<String, Object> splitRow(int dimensionId, String dimensionValue, long cnt) {
        Map<String, Object> row = new HashMap<>();
        row.put("dimensionId", dimensionId);
        row.put("dimensionValue", dimensionValue);
        row.put("cnt", cnt);
        return row;
    }
}
