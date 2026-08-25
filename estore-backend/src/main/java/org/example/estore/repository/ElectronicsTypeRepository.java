package org.example.estore.repository;

import org.example.estore.entity.ElectronicsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElectronicsTypeRepository extends JpaRepository<ElectronicsType, Long> {
    Optional<ElectronicsType> findByNameIgnoreCase(String name);
}
