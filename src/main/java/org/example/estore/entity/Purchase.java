package org.example.estore.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/** Покупка */
@Entity
@Table(name = "purchases", indexes = @Index(name = "idx_purchases_date_time", columnList = "date_time"))
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Электротовар */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "electronics_id", nullable = false)
    private Electronics electronics;

    /** Сотрудник, оформивший покупку */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Магазин, где был продан товар */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    /** Дата/время совершения покупки */
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    /** Тип покупки */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_type_id", nullable = false)
    private PurchaseType purchaseType;

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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public PurchaseType getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(PurchaseType purchaseType) {
        this.purchaseType = purchaseType;
    }
}
