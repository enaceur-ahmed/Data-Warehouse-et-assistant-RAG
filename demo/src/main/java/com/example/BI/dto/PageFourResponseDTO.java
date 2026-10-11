package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageFourResponseDTO {
    private List<ManagerDTO> managers;
    private List<EmployeeChartDTO> employeesByManager;
    private List<SalaryChartDTO> averageSalaryByManager;
    private Map<String, Long> departmentStats;
}