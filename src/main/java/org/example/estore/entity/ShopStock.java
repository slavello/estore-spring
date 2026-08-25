package org.example.estore.entity;

import javax.persistence.*;
import javax.validation.constraints.Min;

/**
 * Связующая сущность "Наличие товара в магазине":
 * остаток конкретного электротовара в конкретном магазине.
 */
@Entity
@Table(name = "shop_stocks",
       uniqueConstraints = @UniqueConstraint(name = "uk_stock_electronics_shop",
       columnNames = {"electronics_id", "shop_id"}))
public class ShopStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "electronics_id", nullable = false)
    private Electronics electronics;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Min(0)
    private Integer quantity;

    public ShopStock() {}

    public ShopStock(Electronics p, Shop s, int qty) {
        this.electronics = p;
        this.shop = s;
        this.quantity = qty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Electronics getElectronics() {
        return electronics;
    }

    public void setElectronics(Electronics electronics) {
        this.electronics = electronics;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
