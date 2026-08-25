package org.example.estore.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Реестр "Покупки".
 * При создании покупки выполняется проверка наличия товара в магазине:
 * если товара нет — запись не создаётся, возвращается ошибка (см. OutOfStockException).
 */
public class PurchaseDto {

    private Long id;

    @NotNull
    private Long electronicsId;

    @NotNull
    private Long employeeId;

    @NotNull
    private Long shopId;

    @NotNull
    private Long purchaseTypeId;

    /** Дата/время совершения покупки (если не задана — текущий момент) */
    private LocalDateTime dateTime;

    // ---- вычисляемые поля для отображения ----
    private String electronicsName;
    private String employeeName;
    private String shopName;
    private String purchaseTypeName;
    private BigDecimal price;

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

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getPurchaseTypeId() {
        return purchaseTypeId;
    }

    public void setPurchaseTypeId(Long purchaseTypeId) {
        this.purchaseTypeId = purchaseTypeId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getElectronicsName() {
        return electronicsName;
    }

    public void setElectronicsName(String electronicsName) {
        this.electronicsName = electronicsName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getPurchaseTypeName() {
        return purchaseTypeName;
    }

    public void setPurchaseTypeName(String purchaseTypeName) {
        this.purchaseTypeName = purchaseTypeName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
