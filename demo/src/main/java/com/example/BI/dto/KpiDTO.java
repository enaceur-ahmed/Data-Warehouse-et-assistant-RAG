package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiDTO {
    private Long totalEmployees;
    private BigDecimal averageSalary;
    private BigDecimal maxSalary;
    private BigDecimal minSalary;
    private BigDecimal totalSalaryMass;
}
