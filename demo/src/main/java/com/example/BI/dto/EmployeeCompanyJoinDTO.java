package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeCompanyJoinDTO {
    private FactEmployeeDTO employee;
    private DimCompanyDTO company;

    public EmployeeCompanyJoinDTO(FactEmployeeDTO employee, DimCompanyDTO company) {
        this.employee = employee;
        this.company = company;
    }

    public FactEmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(FactEmployeeDTO employee) {
        this.employee = employee;
    }

    public DimCompanyDTO getCompany() {
        return company;
    }

    public void setCompany(DimCompanyDTO company) {
        this.company = company;
    }
}

