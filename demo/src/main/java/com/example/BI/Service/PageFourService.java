// PageFourService.java
package com.example.BI.Service;

import com.example.BI.dto.*;
import com.example.BI.Repository.EmployeeAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PageFourService {

    @Autowired
    private EmployeeAnalyticsRepository employeeAnalyticsRepository;

    public PageFourResponseDTO getHierarchyAnalysis(Integer managerId, Integer departmentId, Integer jobTitleId) {
        PageFourResponseDTO response = new PageFourResponseDTO();

        // Managers List with details
        List<ManagerDTO> managers = employeeAnalyticsRepository
                .getManagersList(managerId, departmentId, jobTitleId)
                .stream()
                .map(row -> {
                    ManagerDTO dto = new ManagerDTO();
                    dto.setManagerId(((Number) row.get("ManagerEmployeeID") != null ?
                            ((Number) row.get("ManagerEmployeeID")).intValue() : null));
                    dto.setManagerName((String) row.get("managerName"));
                    dto.setSubordinateCount(((Number) row.get("subordinateCount")).longValue());
                    dto.setAverageSalary(BigDecimal.ZERO);
                    return dto;
                })
                .collect(Collectors.toList());
        response.setManagers(managers);

        // Employees by Manager
        List<Map<String, Object>> managerAnalysisData = employeeAnalyticsRepository
                .getManagersAnalysis(managerId, departmentId, jobTitleId);

        List<EmployeeChartDTO> employeesByManager = managerAnalysisData
                .stream()
                .map(row -> {
                    Integer mgrId = ((Number) row.get("managerId")).intValue();
                    Long count = ((Number) row.get("subordinateCount")).longValue();
                    EmployeeChartDTO dto = new EmployeeChartDTO();
                    dto.setName("Manager " + mgrId);
                    dto.setCount(count);
                    return dto;
                })
                .collect(Collectors.toList());
        response.setEmployeesByManager(employeesByManager);

        // Average Salary by Manager
        List<SalaryChartDTO> salaryByManager = managerAnalysisData
                .stream()
                .map(row -> {
                    Integer mgrId = ((Number) row.get("managerId")).intValue();
                    BigDecimal avgSalary = row.get("averageSalary") != null ?
                            new BigDecimal(row.get("averageSalary").toString()) : BigDecimal.ZERO;
                    SalaryChartDTO dto = new SalaryChartDTO();
                    dto.setName("Manager " + mgrId);
                    dto.setValue(avgSalary);
                    return dto;
                })
                .collect(Collectors.toList());
        response.setAverageSalaryByManager(salaryByManager);

        // Department Statistics
        Long deptCount = employeeAnalyticsRepository.getActiveDepartmentCount();
        Map<String, Long> deptStats = new HashMap<>();
        deptStats.put("totalDepartments", deptCount);
        response.setDepartmentStats(deptStats);

        return response;
    }
}