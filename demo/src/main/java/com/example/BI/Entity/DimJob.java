package com.example.BI.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "Dim_Job")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimJob {
    @Id
    @Column(name = "JobTitleID")
    private Integer jobTitleId;

    @Column(name = "JobTitleCode")
    private String jobTitleCode;

    @Column(name = "JobTitle")
    private String jobTitle;

    @Column(name = "JobLevel")
    private Integer jobLevel;

    @Column(name = "MinSalary")
    private BigDecimal minSalary;

    @Column(name = "MaxSalary")
    private BigDecimal maxSalary;

    @Column(name = "IsActive")
    private Boolean isActive;
}
