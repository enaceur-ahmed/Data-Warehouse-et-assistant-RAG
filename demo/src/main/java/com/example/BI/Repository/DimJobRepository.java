package com.example.BI.Repository;

import com.example.BI.Entity.DimJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DimJobRepository extends JpaRepository<DimJob, Integer> {
}