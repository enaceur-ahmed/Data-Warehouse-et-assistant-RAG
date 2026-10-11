package com.example.BI.Service;

import com.example.BI.dto.*;
import com.example.BI.Entity.DimCompany;
import com.example.BI.Entity.DimDepartement;
import com.example.BI.Entity.DimJob;
import com.example.BI.Entity.FactEmployee;
import com.example.BI.Repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private DimCompanyRepository companyRepository;

    @Autowired
    private DimDepartementRepository departementRepository;

    @Autowired
    private DimJobRepository jobRepository;

    @Autowired
    private FactEmployeeRepository employeeRepository;

    public List<DimCompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToCompanyDTO)
                .collect(Collectors.toList());
    }

    public List<DimDepartementDTO> getAllDepartements() {
        return departementRepository.findAll().stream()
                .map(this::convertToDepartementDTO)
                .collect(Collectors.toList());
    }

    public List<DimJobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::convertToJobDTO)
                .collect(Collectors.toList());
    }

    public List<FactEmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToEmployeeDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de conversion manuelles (pour sélectionner seulement les champs nécessaires)

    private DimCompanyDTO convertToCompanyDTO(DimCompany entity) {
        DimCompanyDTO dto = new DimCompanyDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private DimDepartementDTO convertToDepartementDTO(DimDepartement entity) {
        DimDepartementDTO dto = new DimDepartementDTO();
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setDepartmentCode(entity.getDepartmentCode());
        dto.setDepartmentName(entity.getDepartmentName());
        dto.setAnnualBudget(entity.getAnnualBudget());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    private DimJobDTO convertToJobDTO(DimJob entity) {
        DimJobDTO dto = new DimJobDTO();
        dto.setJobTitleId(entity.getJobTitleId());
        dto.setJobTitle(entity.getJobTitle());
        dto.setJobLevel(entity.getJobLevel());
        dto.setMinSalary(entity.getMinSalary());
        dto.setMaxSalary(entity.getMaxSalary());
        return dto;
    }

    private FactEmployeeDTO convertToEmployeeDTO(FactEmployee entity) {
        FactEmployeeDTO dto = new FactEmployeeDTO();
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setJobTitleId(entity.getJobTitleId());
        dto.setBaseSalary(entity.getBaseSalary());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}