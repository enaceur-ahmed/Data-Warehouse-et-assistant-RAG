package com.example.BI.Chat.indexing;

import com.example.BI.Chat.retrieval.Chunk;
import com.example.BI.Repository.ChatAnalyticsRepository;
import com.example.BI.Repository.DimCompanyRepository;
import com.example.BI.Repository.DimDepartementRepository;
import com.example.BI.Repository.DimJobRepository;
import com.example.BI.Repository.EmployeeAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Construit l'index RAG en mémoire à partir de l'entrepôt DW-Projet :
 * cartes société/département/poste, agrégats par dimension, KPIs globaux,
 * et un chunk compact par employé. Les emails et données de contact sont
 * EXCLUS. Nombre de chunks plafonné (MAX_CHUNKS) et consigné dans les logs.
 * Relancé à la demande via POST /api/chat/reindex.
 */
@Service
public class WarehouseIndexer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseIndexer.class);
    public static final int MAX_CHUNKS = 500;
    public static final int MAX_EMPLOYEE_CHUNKS = 400;

    @Autowired
    private DimCompanyRepository dimCompanyRepository;
    @Autowired
    private DimDepartementRepository dimDepartementRepository;
    @Autowired
    private DimJobRepository dimJobRepository;
    @Autowired
    private ChatAnalyticsRepository chatAnalyticsRepository;
    @Autowired
    private EmployeeAnalyticsRepository employeeAnalyticsRepository;

    private volatile List<Chunk> index = List.of();
    private volatile WarehouseKnowledge knowledge = new WarehouseKnowledge();
    private volatile boolean indexed = false;
    private volatile int generation = 0;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        try {
            reindex();
        } catch (Exception e) {
            // L'indexation ne doit JAMAIS empêcher le démarrage
            logger.error("[INDEX] échec de l'indexation initiale (le chatbot fonctionnera sans index) : {}",
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public synchronized void reindex() {
        long start = System.currentTimeMillis();
        List<Chunk> chunks = new ArrayList<>();
        WarehouseKnowledge k = new WarehouseKnowledge();

        // --- Cartes des dimensions ---
        dimCompanyRepository.findAll().forEach(c -> {
            k.addCompany(c.getCompanyId(), c.getCompanyName());
            chunks.add(new Chunk("company-" + c.getCompanyId(), "company",
                    "Société : " + c.getCompanyName(),
                    String.format("Société %s (ID %d) — code %s — ville %s — pays %s — site %s — active : %s",
                            c.getCompanyName(), c.getCompanyId(),
                            orEmpty(c.getCompanyCode()), orEmpty(c.getCity()),
                            orEmpty(c.getCountry()), orEmpty(c.getWebsite()),
                            booleanOuiNon(c.getIsActive())),
                    Map.of("companyId", String.valueOf(c.getCompanyId()))));
        });

        dimDepartementRepository.findAll().forEach(d -> {
            k.addDepartment(d.getDepartmentId(), d.getDepartmentName());
            chunks.add(new Chunk("department-" + d.getDepartmentId(), "department",
                    "Département : " + d.getDepartmentName(),
                    String.format("Département %s (ID %d) — code %s — société ID %d — budget annuel %s — actif : %s",
                            d.getDepartmentName(), d.getDepartmentId(),
                            orEmpty(d.getDepartmentCode()),
                            d.getCompanyId() != null ? d.getCompanyId() : -1,
                            formatMoney(d.getAnnualBudget()),
                            booleanOuiNon(d.getIsActive())),
                    Map.of("departmentId", String.valueOf(d.getDepartmentId()))));
        });

        dimJobRepository.findAll().forEach(j -> {
            k.addJob(j.getJobTitleId(), j.getJobTitle());
            chunks.add(new Chunk("job-" + j.getJobTitleId(), "job",
                    "Poste : " + j.getJobTitle(),
                    String.format("Poste %s (ID %d) — code %s — niveau %d — salaire min %s — salaire max %s — actif : %s",
                            j.getJobTitle(), j.getJobTitleId(),
                            orEmpty(j.getJobTitleCode()),
                            j.getJobLevel() != null ? j.getJobLevel() : 0,
                            formatMoney(j.getMinSalary()), formatMoney(j.getMaxSalary()),
                            booleanOuiNon(j.getIsActive())),
                    Map.of("jobTitleId", String.valueOf(j.getJobTitleId()))));
        });

        // --- Agrégats par département (avec répartitions) ---
        Map<Integer, List<Map<String, Object>>> genderByDept = groupByInt(
                chatAnalyticsRepository.getGenderSplitByDepartment(), "dimensionId");
        Map<Integer, List<Map<String, Object>>> typeByDept = groupByInt(
                chatAnalyticsRepository.getEmploymentTypeSplitByDepartment(), "dimensionId");

        for (Map<String, Object> row : chatAnalyticsRepository.getDepartmentAggregates()) {
            int deptId = toInt(row.get("dimensionId"));
            String name = toStr(row.get("dimensionName"));
            String genderSplit = formatSplit(genderByDept.getOrDefault(deptId, List.of()));
            String typeSplit = formatSplit(typeByDept.getOrDefault(deptId, List.of()));
            chunks.add(new Chunk("agg-dept-" + deptId, "aggregate",
                    "Agrégat département : " + name,
                    String.format("Département %s (ID %d) : %d employés ; salaire moyen %s ; min %s ; max %s ; masse salariale %s ; genre : %s ; contrats : %s",
                            name, deptId, toLong(row.get("employeeCount")),
                            formatMoney(row.get("averageSalary")), formatMoney(row.get("minSalary")),
                            formatMoney(row.get("maxSalary")), formatMoney(row.get("totalSalaryMass")),
                            genderSplit, typeSplit),
                    Map.of("departmentId", String.valueOf(deptId))));
        }

        // --- Agrégats par poste (avec répartitions) ---
        Map<Integer, List<Map<String, Object>>> genderByJob = groupByInt(
                chatAnalyticsRepository.getGenderSplitByJob(), "dimensionId");
        Map<Integer, List<Map<String, Object>>> typeByJob = groupByInt(
                chatAnalyticsRepository.getEmploymentTypeSplitByJob(), "dimensionId");

        for (Map<String, Object> row : chatAnalyticsRepository.getJobAggregates()) {
            int jobId = toInt(row.get("dimensionId"));
            String name = toStr(row.get("dimensionName"));
            chunks.add(new Chunk("agg-job-" + jobId, "aggregate",
                    "Agrégat poste : " + name,
                    String.format("Poste %s (ID %d) : %d employés ; salaire moyen %s ; min %s ; max %s ; masse salariale %s ; genre : %s ; contrats : %s",
                            name, jobId, toLong(row.get("employeeCount")),
                            formatMoney(row.get("averageSalary")), formatMoney(row.get("minSalary")),
                            formatMoney(row.get("maxSalary")), formatMoney(row.get("totalSalaryMass")),
                            formatSplit(genderByJob.getOrDefault(jobId, List.of())),
                            formatSplit(typeByJob.getOrDefault(jobId, List.of()))),
                    Map.of("jobTitleId", String.valueOf(jobId))));
        }

        // --- Agrégats par société ---
        for (Map<String, Object> row : chatAnalyticsRepository.getCompanyAggregates()) {
            int companyId = toInt(row.get("dimensionId"));
            String name = toStr(row.get("dimensionName"));
            chunks.add(new Chunk("agg-company-" + companyId, "aggregate",
                    "Agrégat société : " + name,
                    String.format("Société %s (ID %d) : %d employés ; salaire moyen %s ; min %s ; max %s ; masse salariale %s",
                            name, companyId, toLong(row.get("employeeCount")),
                            formatMoney(row.get("averageSalary")), formatMoney(row.get("minSalary")),
                            formatMoney(row.get("maxSalary")), formatMoney(row.get("totalSalaryMass"))),
                    Map.of("companyId", String.valueOf(companyId))));
        }

        // --- Agrégats par manager (auto-jointure) ---
        for (Map<String, Object> row : chatAnalyticsRepository.getManagerAggregates()) {
            int managerId = toInt(row.get("managerId"));
            String name = toStr(row.get("managerName"));
            k.addManager(managerId, name);
            chunks.add(new Chunk("agg-manager-" + managerId, "manager",
                    "Manager : " + name,
                    String.format("Manager %s (ID employé %d) : %d subordonnés ; salaire moyen de l'équipe %s ; min %s ; max %s",
                            name, managerId, toLong(row.get("subordinateCount")),
                            formatMoney(row.get("averageSalary")), formatMoney(row.get("minSalary")),
                            formatMoney(row.get("maxSalary"))),
                    Map.of("managerId", String.valueOf(managerId))));
        }

        // --- Valeurs distinctes pour la validation des filtres ---
        // (collectées AVANT le chunk KPI : la devise y est utilisée)
        chatAnalyticsRepository.getDistinctGenders().forEach(r -> k.getGenders().add(toStr(r.get("value"))));
        chatAnalyticsRepository.getDistinctEmploymentTypes().forEach(r -> k.getEmploymentTypes().add(toStr(r.get("value"))));
        chatAnalyticsRepository.getDistinctEmploymentStatuses().forEach(r -> k.getEmploymentStatuses().add(toStr(r.get("value"))));
        chatAnalyticsRepository.getDistinctHireYears().forEach(r -> k.getHireYears().add(toInt(r.get("value"))));
        chatAnalyticsRepository.getDistinctCurrencies().forEach(r -> k.getCurrencies().add(toStr(r.get("value"))));

        // --- KPI globaux (toujours inclus dans la récupération) ---
        chunks.add(buildGlobalKpiChunk());

        // --- Chunks employés (sans email / téléphone / données de contact) ---
        List<Map<String, Object>> employees = chatAnalyticsRepository.getEmployeeCards();
        int employeeChunkLimit = Math.min(employees.size(), MAX_EMPLOYEE_CHUNKS);
        for (int i = 0; i < employeeChunkLimit; i++) {
            Map<String, Object> e = employees.get(i);
            int employeeId = toInt(e.get("employeeId"));
            String fullName = toStr(e.get("firstName")) + " " + toStr(e.get("lastName"));
            chunks.add(new Chunk("employee-" + employeeId, "employee",
                    "Employé : " + fullName,
                    String.format("Employé #%d — %s %s ; poste : %s (ID %d) ; département : %s (ID %d) ; société : %s ; manager : %s ; genre : %s ; contrat : %s ; statut : %s ; salaire de base : %s ; embauche : %s",
                            employeeId, toStr(e.get("firstName")), toStr(e.get("lastName")),
                            toStr(e.get("jobTitle")), toInt(e.get("jobTitleId")),
                            toStr(e.get("departmentName")), toInt(e.get("departmentId")),
                            toStr(e.get("companyName")),
                            e.get("managerName") != null ? toStr(e.get("managerName")) : "(aucun)",
                            toStr(e.get("gender")), toStr(e.get("employmentType")),
                            toStr(e.get("employmentStatus")),
                            formatMoney(e.get("baseSalary")), toStr(e.get("hireDate"))),
                    Map.of("employeeId", String.valueOf(employeeId))));
        }
        if (employees.size() > MAX_EMPLOYEE_CHUNKS) {
            logger.warn("[INDEX] {} employés, seuls {} chunks employés sont indexés (plafond {})",
                    employees.size(), MAX_EMPLOYEE_CHUNKS, MAX_EMPLOYEE_CHUNKS);
        }

        // --- Plafonnement du nombre de chunks ---
        if (chunks.size() > MAX_CHUNKS) {
            logger.warn("[INDEX] {} chunks dépassent le plafond de {}, troncature", chunks.size(), MAX_CHUNKS);
        }
        List<Chunk> finalChunks = chunks.size() > MAX_CHUNKS
                ? chunks.subList(0, MAX_CHUNKS)
                : chunks;

        this.index = List.copyOf(finalChunks);
        this.knowledge = k;
        this.indexed = true;
        this.generation++;

        logger.info("[INDEX] indexation terminée en {} ms : {} chunks (plafond {}) — {} sociétés, {} départements, {} postes, {} managers, {} employés",
                System.currentTimeMillis() - start, this.index.size(), MAX_CHUNKS,
                k.getCompaniesById().size(), k.getDepartmentsById().size(),
                k.getJobsById().size(), k.getManagersById().size(),
                employees.size());
    }

    private Chunk buildGlobalKpiChunk() {
        Map<String, Object> kpi;
        try {
            kpi = employeeAnalyticsRepository.getGlobalKpis(null, null, null, null);
        } catch (Exception e) {
            logger.warn("[INDEX] KPIs globaux indisponibles : {}", e.getClass().getSimpleName());
            kpi = Map.of("totalEmployees", 0L, "averageSalary", BigDecimal.ZERO);
        }
        long activeDepts = 0;
        long managers = 0;
        try {
            activeDepts = toLong(chatAnalyticsRepository.getActiveDepartmentCount().get("cnt"));
            managers = toLong(chatAnalyticsRepository.getActiveManagerCount().get("cnt"));
        } catch (Exception e) {
            logger.warn("[INDEX] compteurs de départements/managers indisponibles : {}", e.getClass().getSimpleName());
        }
        String currencies = String.join(", ", knowledge.getCurrencies().isEmpty()
                ? List.of("(inconnue)") : knowledge.getCurrencies());
        return new Chunk("kpi-global", "kpi",
                "KPIs globaux de l'entrepôt RH",
                String.format("KPIs globaux : %d employés actifs au total ; salaire moyen %s ; salaire minimum %s ; salaire maximum %s ; masse salariale totale %s ; %d départements avec des employés actifs ; %d managers ; devise : %s",
                        toLong(kpi.get("totalEmployees")),
                        formatMoney(kpi.get("averageSalary")),
                        kpi.get("minSalary") != null ? formatMoney(kpi.get("minSalary")) : "n/a",
                        kpi.get("maxSalary") != null ? formatMoney(kpi.get("maxSalary")) : "n/a",
                        kpi.get("totalSalaryMass") != null ? formatMoney(kpi.get("totalSalaryMass")) : "n/a",
                        activeDepts, managers, currencies),
                Map.of("kpi", "global"));
    }

    public List<Chunk> getIndex() {
        return index;
    }

    public WarehouseKnowledge getKnowledge() {
        return knowledge;
    }

    public boolean isIndexed() {
        return indexed;
    }

    /** Incrémenté à chaque (re)indexation — permet de détecter les changements. */
    public int getGeneration() {
        return generation;
    }

    // ---------- helpers ----------

    private static Map<Integer, List<Map<String, Object>>> groupByInt(
            List<Map<String, Object>> rows, String key) {
        Map<Integer, List<Map<String, Object>>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            result.computeIfAbsent(toInt(row.get(key)), x -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private static String formatSplit(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "n/a";
        }
        return rows.stream()
                .map(r -> toStr(r.get("dimensionValue")) + " " + toLong(r.get("cnt")))
                .collect(Collectors.joining(", "));
    }

    private static String formatMoney(Object value) {
        if (value == null) {
            return "n/a";
        }
        if (value instanceof BigDecimal bd) {
            return bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        }
        try {
            return new BigDecimal(value.toString()).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            return String.valueOf(value);
        }
    }

    private static String orEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String booleanOuiNon(Object value) {
        return Boolean.TRUE.equals(value) ? "oui" : "non";
    }

    private static int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static String toStr(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
