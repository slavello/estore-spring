package org.example.estore.repository;

import org.example.estore.entity.ShopStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopStockRepository extends JpaRepository<ShopStock, Long> {
    Optional<ShopStock> findByElectronicsIdAndShopId(Long electronicsId, Long shopId);
}
