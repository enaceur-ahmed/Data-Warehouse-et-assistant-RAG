package com.example.BI.Repository;

import com.example.BI.Entity.DimDepartement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DimDepartementRepository extends JpaRepository<DimDepartement, Integer> {
}
