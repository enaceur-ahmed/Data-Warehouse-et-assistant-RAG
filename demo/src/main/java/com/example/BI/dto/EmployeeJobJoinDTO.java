package com.example.BI.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class EmployeeJobJoinDTO {
    private FactEmployeeDTO employee;
    private DimJobDTO job;

    public EmployeeJobJoinDTO(FactEmployeeDTO employee, DimJobDTO job) {
        this.employee = employee;
        this.job = job;
    }

    public FactEmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(FactEmployeeDTO employee) {
        this.employee = employee;
    }

    public DimJobDTO getJob() {
        return job;
    }

    public void setJob(DimJobDTO job) {
        this.job = job;
    }
}
