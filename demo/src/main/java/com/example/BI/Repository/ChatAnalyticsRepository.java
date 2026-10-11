package com.example.BI.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.BI.Entity.FactEmployee;

import java.util.List;
import java.util.Map;

/**
 * Requêtes natives en LECTURE SEULE dédiées au chatbot RAG.
 * Auto-jointure sur ManagerEmployeeID pour les questions de hiérarchie.
 * Aucune modification du schéma, aucune écriture.
 */
@Repository
public interface ChatAnalyticsRepository extends JpaRepository<FactEmployee, Integer> {

    // --- Cartes employé (un chunk par employé, SANS email ni données de contact) ---
    @Query(value = """
        SELECT fe.EmployeeID as employeeId, fe.FirstName as firstName, fe.LastName as lastName,
               fe.Gender as gender, fe.EmploymentType as employmentType, fe.EmploymentStatus as employmentStatus,
               fe.BaseSalary as baseSalary, fe.Currency as currency, fe.HireDate as hireDate,
               fe.CompanyID as companyId, dc.CompanyName as companyName,
               fe.DepartmentID as departmentId, dd.DepartmentName as departmentName,
               fe.JobTitleID as jobTitleId, dj.JobTitle as jobTitle,
               fe.ManagerEmployeeID as managerId,
               (mgr.FirstName + ' ' + mgr.LastName) as managerName
        FROM Fact_Employees fe
        JOIN Dim_Company dc ON fe.CompanyID = dc.CompanyID
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID
        LEFT JOIN Fact_Employees mgr ON fe.ManagerEmployeeID = mgr.EmployeeID
        WHERE fe.IsActive = 1
        ORDER BY fe.EmployeeID
        """, nativeQuery = true)
    List<Map<String, Object>> getEmployeeCards();

    // --- Agrégats par département ---
    @Query(value = """
        SELECT dd.DepartmentID as dimensionId, dd.DepartmentName as dimensionName,
               COUNT(fe.EmployeeID) as employeeCount,
               AVG(fe.BaseSalary) as averageSalary, MIN(fe.BaseSalary) as minSalary,
               MAX(fe.BaseSalary) as maxSalary, SUM(fe.BaseSalary) as totalSalaryMass
        FROM Fact_Employees fe
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        WHERE fe.IsActive = 1
        GROUP BY dd.DepartmentID, dd.DepartmentName
        ORDER BY employeeCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getDepartmentAggregates();

    // --- Agrégats par poste ---
    @Query(value = """
        SELECT dj.JobTitleID as dimensionId, dj.JobTitle as dimensionName,
               COUNT(fe.EmployeeID) as employeeCount,
               AVG(fe.BaseSalary) as averageSalary, MIN(fe.BaseSalary) as minSalary,
               MAX(fe.BaseSalary) as maxSalary, SUM(fe.BaseSalary) as totalSalaryMass
        FROM Fact_Employees fe
        JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID
        WHERE fe.IsActive = 1
        GROUP BY dj.JobTitleID, dj.JobTitle
        ORDER BY employeeCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getJobAggregates();

    // --- Agrégats par société ---
    @Query(value = """
        SELECT dc.CompanyID as dimensionId, dc.CompanyName as dimensionName,
               COUNT(fe.EmployeeID) as employeeCount,
               AVG(fe.BaseSalary) as averageSalary, MIN(fe.BaseSalary) as minSalary,
               MAX(fe.BaseSalary) as maxSalary, SUM(fe.BaseSalary) as totalSalaryMass
        FROM Fact_Employees fe
        JOIN Dim_Company dc ON fe.CompanyID = dc.CompanyID
        WHERE fe.IsActive = 1
        GROUP BY dc.CompanyID, dc.CompanyName
        ORDER BY employeeCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getCompanyAggregates();

    // --- Répartition par genre par département ---
    @Query(value = """
        SELECT dd.DepartmentID as dimensionId, fe.Gender as dimensionValue, COUNT(fe.EmployeeID) as cnt
        FROM Fact_Employees fe
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        WHERE fe.IsActive = 1
        GROUP BY dd.DepartmentID, fe.Gender
        """, nativeQuery = true)
    List<Map<String, Object>> getGenderSplitByDepartment();

    // --- Répartition par type de contrat par département ---
    @Query(value = """
        SELECT dd.DepartmentID as dimensionId, fe.EmploymentType as dimensionValue, COUNT(fe.EmployeeID) as cnt
        FROM Fact_Employees fe
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        WHERE fe.IsActive = 1
        GROUP BY dd.DepartmentID, fe.EmploymentType
        """, nativeQuery = true)
    List<Map<String, Object>> getEmploymentTypeSplitByDepartment();

    // --- Répartition par genre par poste ---
    @Query(value = """
        SELECT dj.JobTitleID as dimensionId, fe.Gender as dimensionValue, COUNT(fe.EmployeeID) as cnt
        FROM Fact_Employees fe
        JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID
        WHERE fe.IsActive = 1
        GROUP BY dj.JobTitleID, fe.Gender
        """, nativeQuery = true)
    List<Map<String, Object>> getGenderSplitByJob();

    // --- Répartition par type de contrat par poste ---
    @Query(value = """
        SELECT dj.JobTitleID as dimensionId, fe.EmploymentType as dimensionValue, COUNT(fe.EmployeeID) as cnt
        FROM Fact_Employees fe
        JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID
        WHERE fe.IsActive = 1
        GROUP BY dj.JobTitleID, fe.EmploymentType
        """, nativeQuery = true)
    List<Map<String, Object>> getEmploymentTypeSplitByJob();

    // --- Agrégats par manager (auto-jointure) ---
    @Query(value = """
        SELECT mgr.EmployeeID as managerId,
               (mgr.FirstName + ' ' + mgr.LastName) as managerName,
               COUNT(sub.EmployeeID) as subordinateCount,
               AVG(sub.BaseSalary) as averageSalary,
               MIN(sub.BaseSalary) as minSalary,
               MAX(sub.BaseSalary) as maxSalary
        FROM Fact_Employees mgr
        JOIN Fact_Employees sub ON mgr.EmployeeID = sub.ManagerEmployeeID
        WHERE mgr.IsActive = 1 AND sub.IsActive = 1
        GROUP BY mgr.EmployeeID, mgr.FirstName, mgr.LastName
        ORDER BY subordinateCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getManagerAggregates();

    // --- Valeurs distinctes (pour la validation des filtres et les exemples) ---
    @Query(value = "SELECT DISTINCT fe.Gender as value FROM Fact_Employees fe WHERE fe.IsActive = 1 ORDER BY value", nativeQuery = true)
    List<Map<String, Object>> getDistinctGenders();

    @Query(value = "SELECT DISTINCT fe.EmploymentType as value FROM Fact_Employees fe WHERE fe.IsActive = 1 ORDER BY value", nativeQuery = true)
    List<Map<String, Object>> getDistinctEmploymentTypes();

    @Query(value = "SELECT DISTINCT fe.EmploymentStatus as value FROM Fact_Employees fe WHERE fe.IsActive = 1 ORDER BY value", nativeQuery = true)
    List<Map<String, Object>> getDistinctEmploymentStatuses();

    @Query(value = "SELECT DISTINCT YEAR(fe.HireDate) as value FROM Fact_Employees fe WHERE fe.IsActive = 1 ORDER BY value", nativeQuery = true)
    List<Map<String, Object>> getDistinctHireYears();

    @Query(value = "SELECT DISTINCT fe.Currency as value FROM Fact_Employees fe WHERE fe.IsActive = 1 AND fe.Currency IS NOT NULL ORDER BY value", nativeQuery = true)
    List<Map<String, Object>> getDistinctCurrencies();

    // --- Nombre de départements distincts avec des employés actifs ---
    @Query(value = "SELECT COUNT(DISTINCT fe.DepartmentID) as cnt FROM Fact_Employees fe WHERE fe.IsActive = 1", nativeQuery = true)
    Map<String, Object> getActiveDepartmentCount();

    // --- Nombre de managers distincts ---
    @Query(value = "SELECT COUNT(DISTINCT fe.ManagerEmployeeID) as cnt FROM Fact_Employees fe WHERE fe.IsActive = 1 AND fe.ManagerEmployeeID IS NOT NULL", nativeQuery = true)
    Map<String, Object> getActiveManagerCount();
}
