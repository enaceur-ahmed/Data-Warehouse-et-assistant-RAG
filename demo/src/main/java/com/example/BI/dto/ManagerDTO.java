package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDTO {
    private Integer managerId;
    private String managerName;
    private Long subordinateCount;
    private BigDecimal averageSalary;
}
