package com.example.BI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimCompanyDTO {
    private Integer companyId;
    private String companyCode;
    private String companyName;
    private String taxId;
    private String address;
    private String city;
    private String country;
    private String phone;
    private String email;
    private String website;
    private Boolean isActive;
}