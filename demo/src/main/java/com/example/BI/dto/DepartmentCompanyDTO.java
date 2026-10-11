package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCompanyDTO {
    private String companyName;
    private Long employeeCount;
    private BigDecimal averageSalary;
    private BigDecimal totalSalaryMass;
}
