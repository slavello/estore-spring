package org.example.estore.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** Магазин */
public class ShopDto {

    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String address;

    public static ShopDto of(org.example.estore.entity.Shop s) {
        ShopDto d = new ShopDto();
        d.id = s.getId();
        d.name = s.getName();
        d.address = s.getAddress();
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
