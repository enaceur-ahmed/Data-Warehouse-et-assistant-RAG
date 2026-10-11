package com.example.BI.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class EmployeeDepartmentJoinDTO {
    private FactEmployeeDTO employee;
    private DimDepartementDTO department;

    public EmployeeDepartmentJoinDTO(FactEmployeeDTO employee, DimDepartementDTO department) {
        this.employee = employee;
        this.department = department;
    }

    public FactEmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(FactEmployeeDTO employee) {
        this.employee = employee;
    }

    public DimDepartementDTO getDepartment() {
        return department;
    }

    public void setDepartment(DimDepartementDTO department) {
        this.department = department;
    }
}
