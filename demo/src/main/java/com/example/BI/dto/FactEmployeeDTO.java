package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactEmployeeDTO {
    private Integer employeeId;
    private Integer companyId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer departmentId;
    private Integer jobTitleId;
    private BigDecimal baseSalary;
    private Boolean isActive;
}