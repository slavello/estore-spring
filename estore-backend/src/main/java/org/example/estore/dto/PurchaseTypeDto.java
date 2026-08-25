package org.example.estore.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** Тип покупки */
public class PurchaseTypeDto {

    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;

    public static PurchaseTypeDto of(org.example.estore.entity.PurchaseType t) {
        PurchaseTypeDto d = new PurchaseTypeDto();
        d.id = t.getId();
        d.name = t.getName();
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
}
