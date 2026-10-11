package com.example.BI.Service;

import com.example.BI.dto.*;
import com.example.BI.Repository.EmployeeAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PageTwoService {

    @Autowired
    private EmployeeAnalyticsRepository employeeAnalyticsRepository;

    public PageTwoResponseDTO getJobAnalysis(Integer jobTitleId, String employmentType, String gender) {
        PageTwoResponseDTO response = new PageTwoResponseDTO();

        // Employees by Job Title
        List<EmployeeChartDTO> employeesByJob = employeeAnalyticsRepository
                .getEmployeesByJobTitle(jobTitleId, employmentType, gender)
                .stream()
                .map(row -> {
                    EmployeeChartDTO dto = new EmployeeChartDTO();
                    dto.setName((String) row.get("name"));
                    dto.setCount(((Number) row.get("count")).longValue());
                    return dto;
                })
                .collect(Collectors.toList());
        response.setEmployeesByJobTitle(employeesByJob);

        // Average Salary by Job Title
        List<SalaryChartDTO> salaryByJob = employeeAnalyticsRepository
                .getAverageSalaryByJobTitle(jobTitleId, employmentType, gender)
                .stream()
                .map(row -> {
                    SalaryChartDTO dto = new SalaryChartDTO();
                    dto.setName((String) row.get("name"));
                    dto.setValue(new BigDecimal(row.get("value").toString()));
                    return dto;
                })
                .collect(Collectors.toList());
        response.setAverageSalaryByJobTitle(salaryByJob);

        // Employment Type Distribution
        List<EmploymentTypeDTO> empTypeDistribution = employeeAnalyticsRepository
                .getEmploymentTypeDistribution(jobTitleId, employmentType, gender)
                .stream()
                .map(row -> {
                    EmploymentTypeDTO dto = new EmploymentTypeDTO();
                    dto.setEmploymentType((String) row.get("employmentType"));
                    dto.setCount(((Number) row.get("count")).longValue());
                    return dto;
                })
                .collect(Collectors.toList());
        response.setEmploymentTypeDistribution(empTypeDistribution);

        // KPIs
        Map<String, Object> kpiData = employeeAnalyticsRepository
                .getGlobalKpis(null, employmentType, null, gender);

        KpiDTO kpi = KpiDTO.builder()
                .totalEmployees(((Number) kpiData.get("totalEmployees")).longValue())
                .averageSalary(kpiData.get("averageSalary") != null ?
                        new BigDecimal(kpiData.get("averageSalary").toString()) : BigDecimal.ZERO)
                .maxSalary(kpiData.get("maxSalary") != null ?
                        new BigDecimal(kpiData.get("maxSalary").toString()) : BigDecimal.ZERO)
                .minSalary(kpiData.get("minSalary") != null ?
                        new BigDecimal(kpiData.get("minSalary").toString()) : BigDecimal.ZERO)
                .build();
        response.setKpi(kpi);

        return response;
    }
}

