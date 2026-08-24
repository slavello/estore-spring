package org.example.estore.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** Наличие товара в магазине (остатки) */
public class ShopStockDto {

    private Long id;

    @NotNull
    private Long electronicsId;

    @NotNull
    private Long shopId;

    /** Количество товара в наличии в магазине */
    @Min(0)
    private Integer quantity;

    // ---- вычисляемые поля для отображения ----
    private String electronicsName;
    private String shopName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getElectronicsId() {
        return electronicsId;
    }

    public void setElectronicsId(Long electronicsId) {
        this.electronicsId = electronicsId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getElectronicsName() {
        return electronicsName;
    }

    public void setElectronicsName(String electronicsName) {
        this.electronicsName = electronicsName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
