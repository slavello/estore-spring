package org.example.estore.repository;

import org.example.estore.entity.Electronics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElectronicsRepository extends JpaRepository<Electronics, Long> {
    Optional<Electronics> findByNameIgnoreCase(String name);
}
