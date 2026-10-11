package com.example.BI.Controller;

import com.example.BI.dto.PageThreeResponseDTO;
import com.example.BI.Service.PageThreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/page3")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class PageThreeController {

    @Autowired
    private PageThreeService pageThreeService;

    @GetMapping
    public ResponseEntity<PageThreeResponseDTO> getCompanyAnalysis(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer jobTitleId) {

        PageThreeResponseDTO result = pageThreeService.getCompanyAnalysis(companyId, departmentId, jobTitleId);
        return ResponseEntity.ok(result);
    }
}
