package com.example.BI.Controller;

import com.example.BI.dto.PageOneResponseDTO;
import com.example.BI.Service.PageOneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/page1")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class PageOneController {

    @Autowired
    private PageOneService pageOneService;

    @GetMapping
    public ResponseEntity<PageOneResponseDTO> getDepartmentAnalysis(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String gender) {

        PageOneResponseDTO result = pageOneService.getDepartmentAnalysis(
                departmentId, employmentType, employmentStatus, gender
        );
        return ResponseEntity.ok(result);
    }
}
