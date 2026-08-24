package org.example.estore.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/** Электротовар */
@Entity
@Table(name = "electronics")
public class Electronics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 150)
    @Column(length = 150)
    private String name;

    /** Тип товара  */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private ElectronicsType type;

    /** Цена */
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    /** Общее количество товара в магазинах */
    private Integer quantity;

    /** Признак архивности: true – снят с продаж, false – в продаже */
    private Boolean archived;

    @Column(columnDefinition = "text")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ElectronicsType getType() {
        return type;
    }

    public void setType(ElectronicsType type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
