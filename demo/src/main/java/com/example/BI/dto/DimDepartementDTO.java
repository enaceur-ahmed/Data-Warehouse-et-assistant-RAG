package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimDepartementDTO {
    private Integer departmentId;
    private Integer companyId;
    private String departmentCode;
    private String departmentName;
    private BigDecimal annualBudget;
    private Boolean isActive;
}
