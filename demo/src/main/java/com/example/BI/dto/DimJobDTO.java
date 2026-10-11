package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimJobDTO {
    private Integer jobTitleId;
    private String jobTitle;
    private Integer jobLevel;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
}
