package org.example.estore.dto;

import java.math.BigDecimal;

/** Результат отчёта "Сумма денежных средств, полученная через оплату выбранным типом оплаты" */
public class CashTotalDto {

    /** Сумма, руб. */
    private BigDecimal amount;

    private Long purchaseTypeId;
    private String purchaseTypeName;

    private Long shopId;
    private String shopName;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getPurchaseTypeId() {
        return purchaseTypeId;
    }

    public void setPurchaseTypeId(Long purchaseTypeId) {
        this.purchaseTypeId = purchaseTypeId;
    }

    public String getPurchaseTypeName() {
        return purchaseTypeName;
    }

    public void setPurchaseTypeName(String purchaseTypeName) {
        this.purchaseTypeName = purchaseTypeName;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
