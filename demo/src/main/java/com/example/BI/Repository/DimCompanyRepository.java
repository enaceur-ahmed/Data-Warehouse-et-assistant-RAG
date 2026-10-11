package com.example.BI.Repository;

import com.example.BI.Entity.DimCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DimCompanyRepository extends JpaRepository<DimCompany, Integer> {
}
