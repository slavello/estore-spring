package org.example.estore.dto;

import org.example.estore.entity.Position;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** Должность */
public class PositionDto {

    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;

    public static PositionDto of(Position p) {
        PositionDto d = new PositionDto();
        d.id = p.getId();
        d.name = p.getName();
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
