package org.example.estore.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** Тип электроники */
public class ElectronicsTypeDto {

    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;

    public static ElectronicsTypeDto of(org.example.estore.entity.ElectronicsType t) {
        ElectronicsTypeDto d = new ElectronicsTypeDto();
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
