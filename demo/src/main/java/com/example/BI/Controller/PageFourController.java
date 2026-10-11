package com.example.BI.Controller;

import com.example.BI.dto.PageFourResponseDTO;
import com.example.BI.Service.PageFourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/page4")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class PageFourController {

    @Autowired
    private PageFourService pageFourService;

    @GetMapping
    public ResponseEntity<PageFourResponseDTO> getHierarchyAnalysis(
            @RequestParam(required = false) Integer managerId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer jobTitleId) {

        PageFourResponseDTO result = pageFourService.getHierarchyAnalysis(managerId, departmentId, jobTitleId);
        return ResponseEntity.ok(result);
    }
}