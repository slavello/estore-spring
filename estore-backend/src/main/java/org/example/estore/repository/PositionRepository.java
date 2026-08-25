package org.example.estore.repository;

import org.example.estore.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByNameIgnoreCase(String name);
}
