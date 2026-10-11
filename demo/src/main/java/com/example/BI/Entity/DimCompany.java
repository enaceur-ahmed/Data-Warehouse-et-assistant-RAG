package com.example.BI.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Dim_Company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimCompany {
    @Id
    @Column(name = "CompanyID")
    private Integer companyId;

    @Column(name = "CompanyCode")
    private String companyCode;

    @Column(name = "CompanyName")
    private String companyName;

    @Column(name = "TaxID")
    private String taxId;

    @Column(name = "Address")
    private String address;

    @Column(name = "City")
    private String city;

    @Column(name = "Country")
    private String country;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Email")
    private String email;

    @Column(name = "Website")
    private String website;

    @Column(name = "IsActive")
    private Boolean isActive;
}
