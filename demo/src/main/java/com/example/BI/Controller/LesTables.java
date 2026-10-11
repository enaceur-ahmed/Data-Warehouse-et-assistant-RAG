package com.example.BI.Controller;

import com.example.BI.dto.*;
import com.example.BI.Service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173") // À adapter selon ton front React
public class LesTables {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/companies")
    public ResponseEntity<List<DimCompanyDTO>> getAllCompanies() {
        return ResponseEntity.ok(dashboardService.getAllCompanies());
    }

    @GetMapping("/departements")
    public ResponseEntity<List<DimDepartementDTO>> getAllDepartements() {
        return ResponseEntity.ok(dashboardService.getAllDepartements());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<DimJobDTO>> getAllJobs() {
        return ResponseEntity.ok(dashboardService.getAllJobs());
    }

    @GetMapping("/employees")
    public ResponseEntity<List<FactEmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(dashboardService.getAllEmployees());
    }
}