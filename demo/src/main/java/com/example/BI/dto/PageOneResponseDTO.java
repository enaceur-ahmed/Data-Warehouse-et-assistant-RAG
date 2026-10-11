package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageOneResponseDTO {
    private List<EmployeeChartDTO> employeesByDepartment;
    private List<SalaryChartDTO> averageSalaryByDepartment;
    private List<GenderDistributionDTO> genderDistribution;
    private KpiDTO kpi;
}
