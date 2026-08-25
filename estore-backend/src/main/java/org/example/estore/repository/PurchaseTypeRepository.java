package org.example.estore.repository;

import org.example.estore.entity.PurchaseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseTypeRepository extends JpaRepository<PurchaseType, Long> {
    Optional<PurchaseType> findByNameIgnoreCase(String name);
}
