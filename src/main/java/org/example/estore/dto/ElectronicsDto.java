package org.example.estore.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/** Электротовар */
public class ElectronicsDto {

    private Long id;

    @Size(max = 150)
    private String name;

    /** Ссылка на справочник "Тип электроники" */
    @NotNull
    private Long typeId;

    /** Цена, руб. */
    private BigDecimal price;

    /** Общее количество в магазинах */
    @Min(0)
    private Integer quantity;

    /** true – снят с продаж (архивный), false – в продаже */
    private Boolean archived;

    private String description;

    // ---- вычисляемые поля для отображения ----
    private String typeName;

    public static ElectronicsDto of(org.example.estore.entity.Electronics e) {
        ElectronicsDto d = new ElectronicsDto();
        d.id = e.getId();
        d.name = e.getName();
        d.price = e.getPrice();
        d.quantity = e.getQuantity();
        d.archived = e.getArchived();
        d.description = e.getDescription();
        if (e.getType() != null) {
            d.typeId = e.getType().getId();
            d.typeName = e.getType().getName();
        }
        return d;
    }

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

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
