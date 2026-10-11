package com.example.BI.Controller;

import com.example.BI.dto.PageTwoResponseDTO;
import com.example.BI.Service.PageTwoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/page2")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class PageTwoController {

    @Autowired
    private PageTwoService pageTwoService;

    @GetMapping
    public ResponseEntity<PageTwoResponseDTO> getJobAnalysis(
            @RequestParam(required = false) Integer jobTitleId,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String gender) {

        PageTwoResponseDTO result = pageTwoService.getJobAnalysis(jobTitleId, employmentType, gender);
        return ResponseEntity.ok(result);
    }
}
