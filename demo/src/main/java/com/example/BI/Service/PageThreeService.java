package com.example.BI.Service;

import com.example.BI.dto.*;
import com.example.BI.Repository.EmployeeAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PageThreeService {

    @Autowired
    private EmployeeAnalyticsRepository employeeAnalyticsRepository;

    public PageThreeResponseDTO getCompanyAnalysis(Integer companyId, Integer departmentId, Integer jobTitleId) {
        PageThreeResponseDTO response = new PageThreeResponseDTO();

        // Detailed Company Data
        List<DepartmentCompanyDTO> companiesData = employeeAnalyticsRepository
                .getCompaniesDetailedData(companyId, departmentId, jobTitleId)
                .stream()
                .map(row -> {
                    DepartmentCompanyDTO dto = new DepartmentCompanyDTO();
                    dto.setCompanyName((String) row.get("companyName"));
                    dto.setEmployeeCount(((Number) row.get("employeeCount")).longValue());
                    dto.setAverageSalary(row.get("averageSalary") != null ?
                            new BigDecimal(row.get("averageSalary").toString()) : BigDecimal.ZERO);
                    dto.setTotalSalaryMass(row.get("totalSalaryMass") != null ?
                            new BigDecimal(row.get("totalSalaryMass").toString()) : BigDecimal.ZERO);
                    return dto;
                })
                .collect(Collectors.toList());
        response.setCompaniesData(companiesData);

        // Employees by Company
        List<EmployeeChartDTO> employeesByCompany = employeeAnalyticsRepository
                .getEmployeesByCompany(companyId, departmentId, jobTitleId)
                .stream()
                .map(row -> {
                    EmployeeChartDTO dto = new EmployeeChartDTO();
                    dto.setName((String) row.get("name"));
                    dto.setCount(((Number) row.get("count")).longValue());
                    return dto;
                })
                .collect(Collectors.toList());
        response.setEmployeesByCompany(employeesByCompany);

        // Average Salary by Company
        List<SalaryChartDTO> salaryByCompany = employeeAnalyticsRepository
                .getAverageSalaryByCompany(companyId, departmentId, jobTitleId)
                .stream()
                .map(row -> {
                    SalaryChartDTO dto = new SalaryChartDTO();
                    dto.setName((String) row.get("name"));
                    dto.setValue(new BigDecimal(row.get("value").toString()));
                    return dto;
                })
                .collect(Collectors.toList());
        response.setAverageSalaryByCompany(salaryByCompany);

        return response;
    }
}