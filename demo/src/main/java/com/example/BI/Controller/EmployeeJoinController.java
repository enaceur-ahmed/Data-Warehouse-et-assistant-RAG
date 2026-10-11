package com.example.BI.Controller;

import com.example.BI.Service.EmployeeJoinService;
import com.example.BI.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:5173")

public class EmployeeJoinController {

    @Autowired
    private EmployeeJoinService employeeJoinService;

    /**
     * Récupère tous les employés avec leurs informations de société
     * GET /api/employees/with-company
     */
    @GetMapping("/with-company")
    public ResponseEntity<?> getAllEmployeesWithCompany() {
        try {
            List<?> result = employeeJoinService.getAllEmployeesWithCompany();
            return ResponseEntity.ok()
                    .body(new ApiResponse("Succès", "Employés avec informations de société récupérés avec succès", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Erreur", "Erreur lors de la récupération des données: " + e.getMessage(), null));
        }
    }

    /**
     * Récupère tous les employés avec leurs informations de département
     * GET /api/employees/with-department
     */
    @GetMapping("/with-department")
    public ResponseEntity<?> getAllEmployeesWithDepartment() {
        try {
            List<?> result = employeeJoinService.getAllEmployeesWithDepartment();
            return ResponseEntity.ok()
                    .body(new ApiResponse("Succès", "Employés avec informations de département récupérés avec succès", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Erreur", "Erreur lors de la récupération des données: " + e.getMessage(), null));
        }
    }

    /**
     * Récupère tous les employés avec leurs informations de poste
     * GET /api/employees/with-job
     */
    @GetMapping("/with-job")
    public ResponseEntity<?> getAllEmployeesWithJob() {
        try {
            List<?> result = employeeJoinService.getAllEmployeesWithJob();
            return ResponseEntity.ok()
                    .body(new ApiResponse("Succès", "Employés avec informations de poste récupérés avec succès", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Erreur", "Erreur lors de la récupération des données: " + e.getMessage(), null));
        }
    }
}
