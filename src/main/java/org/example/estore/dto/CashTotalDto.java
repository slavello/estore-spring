package org.example.estore.dto;

import java.math.BigDecimal;

/** Результат отчёта "Сумма денежных средств, полученная через оплату наличными" */
public class CashTotalDto {

    /** Сумма, руб. */
    private BigDecimal amount;

    private Long shopId;
    private String shopName;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
