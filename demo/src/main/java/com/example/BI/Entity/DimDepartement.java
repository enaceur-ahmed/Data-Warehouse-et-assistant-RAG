package com.example.BI.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "Dim_Departements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimDepartement {
    @Id
    @Column(name = "DepartmentID")
    private Integer departmentId;

    @Column(name = "CompanyID")
    private Integer companyId;

    @Column(name = "LocationID")
    private Integer locationId;

    @Column(name = "DepartmentCode")
    private String departmentCode;

    @Column(name = "DepartmentName")
    private String departmentName;

    @Column(name = "ParentDepartmentID")
    private Integer parentDepartmentId;

    @Column(name = "ManagerEmployeeID")
    private Integer managerEmployeeId;

    @Column(name = "CostCenter")
    private String costCenter;

    @Column(name = "AnnualBudget")
    private BigDecimal annualBudget;

    @Column(name = "IsActive")
    private Boolean isActive;
}

