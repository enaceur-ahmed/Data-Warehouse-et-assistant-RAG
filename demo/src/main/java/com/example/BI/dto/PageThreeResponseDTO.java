package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageThreeResponseDTO {
    private List<DepartmentCompanyDTO> companiesData;
    private List<EmployeeChartDTO> employeesByCompany;
    private List<SalaryChartDTO> averageSalaryByCompany;
}
