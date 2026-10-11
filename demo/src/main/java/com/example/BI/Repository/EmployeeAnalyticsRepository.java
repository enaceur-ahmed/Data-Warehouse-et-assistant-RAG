package com.example.BI.Repository;

import com.example.BI.Entity.FactEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface EmployeeAnalyticsRepository extends JpaRepository<FactEmployee, Integer> {

    // PAGE 1: DEPARTMENT ANALYSIS
    @Query(value = """
        SELECT dd.DepartmentName as name, COUNT(fe.EmployeeID) as count
        FROM Fact_Employees fe
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        WHERE fe.IsActive = 1
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:employmentStatus IS NULL OR fe.EmploymentStatus = :employmentStatus)
        AND (:gender IS NULL OR fe.Gender = :gender)
        GROUP BY dd.DepartmentName
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getEmployeesByDepartment(
            @Param("departmentId") Integer departmentId,
            @Param("employmentType") String employmentType,
            @Param("employmentStatus") String employmentStatus,
            @Param("gender") String gender
    );

    @Query(value = """
        SELECT dd.DepartmentName as name, AVG(fe.BaseSalary) as value
        FROM Fact_Employees fe
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        WHERE fe.IsActive = 1
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:employmentStatus IS NULL OR fe.EmploymentStatus = :employmentStatus)
        AND (:gender IS NULL OR fe.Gender = :gender)
        GROUP BY dd.DepartmentName
        ORDER BY value DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getAverageSalaryByDepartment(
            @Param("departmentId") Integer departmentId,
            @Param("employmentType") String employmentType,
            @Param("employmentStatus") String employmentStatus,
            @Param("gender") String gender
    );

    @Query(value = """
        SELECT dd.DepartmentName as department, fe.Gender as gender, COUNT(fe.EmployeeID) as count
        FROM Fact_Employees fe
        JOIN Dim_Departements dd ON fe.DepartmentID = dd.DepartmentID
        WHERE fe.IsActive = 1
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:employmentStatus IS NULL OR fe.EmploymentStatus = :employmentStatus)
        AND (:gender IS NULL OR fe.Gender = :gender)
        GROUP BY dd.DepartmentName, fe.Gender
        """, nativeQuery = true)
    List<Map<String, Object>> getGenderDistributionByDepartment(
            @Param("departmentId") Integer departmentId,
            @Param("employmentType") String employmentType,
            @Param("employmentStatus") String employmentStatus,
            @Param("gender") String gender
    );

    // PAGE 2: JOB ANALYSIS
    @Query(value = """
        SELECT dj.JobTitle as name, COUNT(fe.EmployeeID) as count
        FROM Fact_Employees fe
        JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID
        WHERE fe.IsActive = 1
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:gender IS NULL OR fe.Gender = :gender)
        GROUP BY dj.JobTitle
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getEmployeesByJobTitle(
            @Param("jobTitleId") Integer jobTitleId,
            @Param("employmentType") String employmentType,
            @Param("gender") String gender
    );

    @Query(value = """
        SELECT dj.JobTitle as name, AVG(fe.BaseSalary) as value
        FROM Fact_Employees fe
        JOIN Dim_Job dj ON fe.JobTitleID = dj.JobTitleID
        WHERE fe.IsActive = 1
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:gender IS NULL OR fe.Gender = :gender)
        GROUP BY dj.JobTitle
        ORDER BY value DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getAverageSalaryByJobTitle(
            @Param("jobTitleId") Integer jobTitleId,
            @Param("employmentType") String employmentType,
            @Param("gender") String gender
    );

    @Query(value = """
        SELECT fe.EmploymentType as employmentType, COUNT(fe.EmployeeID) as count
        FROM Fact_Employees fe
        WHERE fe.IsActive = 1
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:gender IS NULL OR fe.Gender = :gender)
        GROUP BY fe.EmploymentType
        """, nativeQuery = true)
    List<Map<String, Object>> getEmploymentTypeDistribution(
            @Param("jobTitleId") Integer jobTitleId,
            @Param("employmentType") String employmentType,
            @Param("gender") String gender
    );

    // PAGE 3: COMPANY ANALYSIS
    @Query(value = """
        SELECT dc.CompanyName as name, COUNT(fe.EmployeeID) as count
        FROM Fact_Employees fe
        JOIN Dim_Company dc ON fe.CompanyID = dc.CompanyID
        WHERE fe.IsActive = 1
        AND (:companyId IS NULL OR fe.CompanyID = :companyId)
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        GROUP BY dc.CompanyName
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getEmployeesByCompany(
            @Param("companyId") Integer companyId,
            @Param("departmentId") Integer departmentId,
            @Param("jobTitleId") Integer jobTitleId
    );

    @Query(value = """
        SELECT dc.CompanyName as name, AVG(fe.BaseSalary) as value
        FROM Fact_Employees fe
        JOIN Dim_Company dc ON fe.CompanyID = dc.CompanyID
        WHERE fe.IsActive = 1
        AND (:companyId IS NULL OR fe.CompanyID = :companyId)
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        GROUP BY dc.CompanyName
        ORDER BY value DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getAverageSalaryByCompany(
            @Param("companyId") Integer companyId,
            @Param("departmentId") Integer departmentId,
            @Param("jobTitleId") Integer jobTitleId
    );

    @Query(value = """
        SELECT dc.CompanyName as companyName, COUNT(fe.EmployeeID) as employeeCount, 
               AVG(fe.BaseSalary) as averageSalary, SUM(fe.BaseSalary) as totalSalaryMass
        FROM Fact_Employees fe
        JOIN Dim_Company dc ON fe.CompanyID = dc.CompanyID
        WHERE fe.IsActive = 1
        AND (:companyId IS NULL OR fe.CompanyID = :companyId)
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        GROUP BY dc.CompanyName
        """, nativeQuery = true)
    List<Map<String, Object>> getCompaniesDetailedData(
            @Param("companyId") Integer companyId,
            @Param("departmentId") Integer departmentId,
            @Param("jobTitleId") Integer jobTitleId
    );

    // PAGE 4: MANAGER ANALYSIS
    @Query(value = """
        SELECT fe.ManagerEmployeeID as managerId, COUNT(fe.EmployeeID) as subordinateCount,
               AVG(fe.BaseSalary) as averageSalary
        FROM Fact_Employees fe
        WHERE fe.IsActive = 1 AND fe.ManagerEmployeeID IS NOT NULL
        AND (:managerId IS NULL OR fe.ManagerEmployeeID = :managerId)
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        GROUP BY fe.ManagerEmployeeID
        ORDER BY subordinateCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getManagersAnalysis(
            @Param("managerId") Integer managerId,
            @Param("departmentId") Integer departmentId,
            @Param("jobTitleId") Integer jobTitleId
    );

    @Query(value = """
        SELECT fe.FirstName + ' ' + fe.LastName as managerName, 
               COUNT(sub.EmployeeID) as subordinateCount
        FROM Fact_Employees fe
        LEFT JOIN Fact_Employees sub ON fe.EmployeeID = sub.ManagerEmployeeID
        WHERE fe.IsActive = 1 AND fe.EmployeeID IN (
            SELECT DISTINCT ManagerEmployeeID FROM Fact_Employees 
            WHERE ManagerEmployeeID IS NOT NULL AND IsActive = 1
        )
        AND (:managerId IS NULL OR fe.EmployeeID = :managerId)
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:jobTitleId IS NULL OR fe.JobTitleID = :jobTitleId)
        GROUP BY fe.EmployeeID, fe.FirstName, fe.LastName
        ORDER BY subordinateCount DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getManagersList(
            @Param("managerId") Integer managerId,
            @Param("departmentId") Integer departmentId,
            @Param("jobTitleId") Integer jobTitleId
    );

    // GLOBAL KPIs
    @Query(value = """
        SELECT COUNT(fe.EmployeeID) as totalEmployees, 
               AVG(fe.BaseSalary) as averageSalary,
               MAX(fe.BaseSalary) as maxSalary,
               MIN(fe.BaseSalary) as minSalary,
               SUM(fe.BaseSalary) as totalSalaryMass
        FROM Fact_Employees fe
        WHERE fe.IsActive = 1
        AND (:departmentId IS NULL OR fe.DepartmentID = :departmentId)
        AND (:employmentType IS NULL OR fe.EmploymentType = :employmentType)
        AND (:employmentStatus IS NULL OR fe.EmploymentStatus = :employmentStatus)
        AND (:gender IS NULL OR fe.Gender = :gender)
        """, nativeQuery = true)
    Map<String, Object> getGlobalKpis(
            @Param("departmentId") Integer departmentId,
            @Param("employmentType") String employmentType,
            @Param("employmentStatus") String employmentStatus,
            @Param("gender") String gender
    );

    @Query(value = """
        SELECT COUNT(DISTINCT fe.DepartmentID) as departmentCount
        FROM Fact_Employees fe
        WHERE fe.IsActive = 1
        """, nativeQuery = true)
    Long getActiveDepartmentCount();
}
