package org.example.estore.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/** Сотрудник */
public class EmployeeDto {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String middleName;

    @NotNull
    private LocalDate birthDate;

    /** Ссылка на справочник "Должности" */
    @NotNull
    private Long positionId;

    /** Ссылка на справочник "Магазин" */
    @NotNull
    private Long shopId;

    /** Пол: true – мужской, false – женский */
    @NotNull
    private Boolean male;

    // ---- вычисляемые поля для отображения ----
    private String positionName;
    private String shopName;

    public static EmployeeDto of(org.example.estore.entity.Employee e) {
        EmployeeDto d = new EmployeeDto();
        d.id = e.getId();
        d.lastName = e.getLastName();
        d.firstName = e.getFirstName();
        d.middleName = e.getMiddleName();
        d.birthDate = e.getBirthDate();
        d.male = e.getMale();
        if (e.getPosition() != null) {
            d.positionId = e.getPosition().getId();
            d.positionName = e.getPosition().getName();
        }
        if (e.getShop() != null) {
            d.shopId = e.getShop().getId();
            d.shopName = e.getShop().getName();
        }
        return d;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Boolean getMale() {
        return male;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
