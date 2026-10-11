package com.example.BI.Repository;

import com.example.BI.Entity.FactEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactEmployeeRepository extends JpaRepository<FactEmployee, Integer> {
}