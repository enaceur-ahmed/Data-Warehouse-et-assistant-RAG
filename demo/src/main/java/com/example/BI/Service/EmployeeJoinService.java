package com.example.BI.Service;

import com.example.BI.Entity.FactEmployee;
import com.example.BI.Entity.DimCompany;
import com.example.BI.Entity.DimDepartement;
import com.example.BI.Entity.DimJob;
import com.example.BI.dto.*;
import com.example.BI.Repository.FactEmployeeRepository;
import com.example.BI.Repository.DimCompanyRepository;
import com.example.BI.Repository.DimDepartementRepository;
import com.example.BI.Repository.DimJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeJoinService {

    @Autowired
    private FactEmployeeRepository factEmployeeRepository;

    @Autowired
    private DimCompanyRepository dimCompanyRepository;

    @Autowired
    private DimDepartementRepository dimDepartementRepository;

    @Autowired
    private DimJobRepository dimJobRepository;

    /**
     * Jointure entre FactEmployee et DimCompany
     * Récupère tous les employés avec leurs informations de société
     */
    public List<EmployeeCompanyJoinDTO> getAllEmployeesWithCompany() {
        return factEmployeeRepository.findAll().stream()
                .map(employee -> {
                    DimCompany company = dimCompanyRepository.findById(employee.getCompanyId())
                            .orElse(null);
                    return new EmployeeCompanyJoinDTO(
                            convertEmployeeToDTO(employee),
                            company != null ? convertCompanyToDTO(company) : null
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Jointure entre FactEmployee et DimDepartement
     * Récupère tous les employés avec leurs informations de département
     */
    public List<EmployeeDepartmentJoinDTO> getAllEmployeesWithDepartment() {
        return factEmployeeRepository.findAll().stream()
                .map(employee -> {
                    DimDepartement department = dimDepartementRepository.findById(employee.getDepartmentId())
                            .orElse(null);
                    return new EmployeeDepartmentJoinDTO(
                            convertEmployeeToDTO(employee),
                            department != null ? convertDepartmentToDTO(department) : null
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Jointure entre FactEmployee et DimJob
     * Récupère tous les employés avec leurs informations de poste
     */
    public List<EmployeeJobJoinDTO> getAllEmployeesWithJob() {
        return factEmployeeRepository.findAll().stream()
                .map(employee -> {
                    DimJob job = dimJobRepository.findById(employee.getJobTitleId())
                            .orElse(null);
                    return new EmployeeJobJoinDTO(
                            convertEmployeeToDTO(employee),
                            job != null ? convertJobToDTO(job) : null
                    );
                })
                .collect(Collectors.toList());
    }

    // Méthodes de conversion
    private FactEmployeeDTO convertEmployeeToDTO(FactEmployee employee) {
        return new FactEmployeeDTO(
                employee.getEmployeeId(),
                employee.getCompanyId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartmentId(),
                employee.getJobTitleId(),
                employee.getBaseSalary(),
                employee.getIsActive()
        );
    }

    private DimCompanyDTO convertCompanyToDTO(DimCompany company) {
        return new DimCompanyDTO(
                company.getCompanyId(),
                company.getCompanyCode(),
                company.getCompanyName(),
                company.getTaxId(),
                company.getAddress(),
                company.getCity(),
                company.getCountry(),
                company.getPhone(),
                company.getEmail(),
                company.getWebsite(),
                company.getIsActive()
        );
    }

    private DimDepartementDTO convertDepartmentToDTO(DimDepartement department) {
        return new DimDepartementDTO(
                department.getDepartmentId(),
                department.getCompanyId(),
                department.getDepartmentCode(),
                department.getDepartmentName(),
                department.getAnnualBudget(),
                department.getIsActive()
        );
    }

    private DimJobDTO convertJobToDTO(DimJob job) {
        return new DimJobDTO(
                job.getJobTitleId(),
                job.getJobTitle(),
                job.getJobLevel(),
                job.getMinSalary(),
                job.getMaxSalary()
        );
    }
}

