package com.example.BI.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Fact_Employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactEmployee {

    @Id
    @Column(name = "EmployeeID")
    private Integer employeeId;

    @Column(name = "CompanyID")
    private Integer companyId;

    @Column(name = "EmployeeCode")
    private String employeeCode;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email")
    private String email;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Mobile")
    private String mobile;

    @Column(name = "DateOfBirth")
    private String dateOfBirth;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "DepartmentID")
    private Integer departmentId;

    @Column(name = "JobTitleID")
    private Integer jobTitleId;

    @Column(name = "ManagerEmployeeID")
    private Integer managerEmployeeId;

    @Column(name = "HireDate")
    private String hireDate;

    @Column(name = "EmploymentType")
    private String employmentType;

    @Column(name = "EmploymentStatus")
    private String employmentStatus;

    @Column(name = "BaseSalary")
    private BigDecimal baseSalary;

    @Column(name = "Currency")
    private String currency;

    @Column(name = "IsActive")
    private Boolean isActive;
}