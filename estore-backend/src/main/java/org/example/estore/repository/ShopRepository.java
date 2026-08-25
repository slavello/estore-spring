package org.example.estore.repository;

import org.example.estore.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByNameIgnoreCase(String name);
}
