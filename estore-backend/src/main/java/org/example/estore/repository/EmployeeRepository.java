package org.example.estore.repository;

import org.example.estore.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByLastNameIgnoreCase(String lastName);
}
