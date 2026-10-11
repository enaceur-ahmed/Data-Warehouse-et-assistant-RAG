package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageTwoResponseDTO {
    private List<EmployeeChartDTO> employeesByJobTitle;
    private List<SalaryChartDTO> averageSalaryByJobTitle;
    private List<EmploymentTypeDTO> employmentTypeDistribution;
    private KpiDTO kpi;
}
