package com.example.BI.Service;

import com.example.BI.dto.*;
import com.example.BI.Repository.EmployeeAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PageOneService {

    private static final Logger logger = LoggerFactory.getLogger(PageOneService.class);

    @Autowired
    private EmployeeAnalyticsRepository employeeAnalyticsRepository;

    public PageOneResponseDTO getDepartmentAnalysis(Integer departmentId, String employmentType,
                                                    String employmentStatus, String gender) {
        logger.info("=== PAGE 1 SERVICE CALLED ===");
        logger.info("Params - departmentId: {}, employmentType: {}, employmentStatus: {}, gender: {}",
                departmentId, employmentType, employmentStatus, gender);

        PageOneResponseDTO response = new PageOneResponseDTO();

        try {
            // Employees by Department
            logger.info("Fetching employees by department...");
            List<Map<String, Object>> empByDept = employeeAnalyticsRepository
                    .getEmployeesByDepartment(departmentId, employmentType, employmentStatus, gender);
            logger.info("Employees by department result: {}", empByDept);

            List<EmployeeChartDTO> employeesByDept = empByDept
                    .stream()
                    .map(row -> {
                        EmployeeChartDTO dto = new EmployeeChartDTO();
                        dto.setName((String) row.get("name"));
                        dto.setCount(((Number) row.get("count")).longValue());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setEmployeesByDepartment(employeesByDept);

            // Average Salary by Department
            logger.info("Fetching average salary by department...");
            List<Map<String, Object>> salByDept = employeeAnalyticsRepository
                    .getAverageSalaryByDepartment(departmentId, employmentType, employmentStatus, gender);
            logger.info("Average salary by department result: {}", salByDept);

            List<SalaryChartDTO> salaryByDept = salByDept
                    .stream()
                    .map(row -> {
                        SalaryChartDTO dto = new SalaryChartDTO();
                        dto.setName((String) row.get("name"));
                        dto.setValue(new BigDecimal(row.get("value").toString()));
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setAverageSalaryByDepartment(salaryByDept);

            // Gender Distribution
            logger.info("Fetching gender distribution...");
            List<Map<String, Object>> genderDists = employeeAnalyticsRepository
                    .getGenderDistributionByDepartment(departmentId, employmentType, employmentStatus, gender);
            logger.info("Gender distribution result: {}", genderDists);

            List<GenderDistributionDTO> genderDist = genderDists
                    .stream()
                    .map(row -> {
                        GenderDistributionDTO dto = new GenderDistributionDTO();
                        dto.setDepartment((String) row.get("department"));
                        // FIX: Convertir Character en String
                        Object genderObj = row.get("gender");
                        String genderStr = genderObj != null ? genderObj.toString() : null;
                        dto.setGender(genderStr);
                        dto.setCount(((Number) row.get("count")).longValue());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setGenderDistribution(genderDist);

            // KPIs
            logger.info("Fetching KPIs...");
            Map<String, Object> kpiData = employeeAnalyticsRepository
                    .getGlobalKpis(departmentId, employmentType, employmentStatus, gender);
            logger.info("KPI data result: {}", kpiData);

            KpiDTO kpi = KpiDTO.builder()
                    .totalEmployees(((Number) kpiData.get("totalEmployees")).longValue())
                    .averageSalary(kpiData.get("averageSalary") != null ?
                            new BigDecimal(kpiData.get("averageSalary").toString()) : BigDecimal.ZERO)
                    .build();
            response.setKpi(kpi);

            logger.info("=== PAGE 1 SERVICE COMPLETED SUCCESSFULLY ===");
        } catch (Exception e) {
            logger.error("=== PAGE 1 SERVICE ERROR ===", e);
            logger.error("Error message: {}", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}