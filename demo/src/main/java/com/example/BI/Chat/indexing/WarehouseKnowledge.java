package com.example.BI.Chat.indexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Connaissances du schéma collectées à l'indexation : dimensions connues
 * (ids + noms) et valeurs autorisées pour les filtres. Utilisé par le
 * validateur de plan de requête pour vérifier les valeurs fournies par le LLM.
 */
public class WarehouseKnowledge {

    private final Map<Integer, String> departmentsById = new HashMap<>();
    private final Map<String, Integer> departmentsByName = new HashMap<>();
    private final Map<Integer, String> jobsById = new HashMap<>();
    private final Map<String, Integer> jobsByName = new HashMap<>();
    private final Map<Integer, String> companiesById = new HashMap<>();
    private final Map<String, Integer> companiesByName = new HashMap<>();
    private final Map<Integer, String> managersById = new HashMap<>();
    private final Map<String, Integer> managersByName = new HashMap<>();
    private final Set<String> genders = new HashSet<>();
    private final Set<String> employmentTypes = new HashSet<>();
    private final Set<String> employmentStatuses = new HashSet<>();
    private final Set<Integer> hireYears = new HashSet<>();
    private final Set<String> currencies = new HashSet<>();

    public void addDepartment(int id, String name) {
        departmentsById.put(id, name);
        departmentsByName.put(normalize(name), id);
    }

    public void addJob(int id, String name) {
        jobsById.put(id, name);
        jobsByName.put(normalize(name), id);
    }

    public void addCompany(int id, String name) {
        companiesById.put(id, name);
        companiesByName.put(normalize(name), id);
    }

    public void addManager(int id, String name) {
        managersById.put(id, name);
        managersByName.put(normalize(name), id);
    }

    public Integer resolveDepartment(String value) {
        return resolve(value, departmentsById, departmentsByName);
    }

    public Integer resolveJob(String value) {
        return resolve(value, jobsById, jobsByName);
    }

    public Integer resolveCompany(String value) {
        return resolve(value, companiesById, companiesByName);
    }

    public Integer resolveManager(String value) {
        return resolve(value, managersById, managersByName);
    }

    private static Integer resolve(String value, Map<Integer, String> byId, Map<String, Integer> byName) {
        if (value == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(value.trim());
            if (byId.containsKey(id)) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // pas un id : on essaie le nom
        }
        return byName.get(normalize(value));
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    public Map<Integer, String> getDepartmentsById() { return departmentsById; }
    public Map<Integer, String> getJobsById() { return jobsById; }
    public Map<Integer, String> getCompaniesById() { return companiesById; }
    public Map<Integer, String> getManagersById() { return managersById; }
    public Set<String> getGenders() { return genders; }
    public Set<String> getEmploymentTypes() { return employmentTypes; }
    public Set<String> getEmploymentStatuses() { return employmentStatuses; }
    public Set<Integer> getHireYears() { return hireYears; }
    public Set<String> getCurrencies() { return currencies; }
}
