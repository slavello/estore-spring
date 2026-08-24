package org.example.estore.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/** Сотрудник */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Фамилия */
    @Size(max = 100)
    @Column(length = 100)
    private String lastName;

    /** Имя */
    @Size(max = 100)
    @Column(length = 100)
    private String firstName;

    /** Отчество */
    @Size(max = 100)
    @Column(length = 100)
    private String middleName;

    /** Дата рождения */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Должность */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    /** Магазин, где работает сотрудник */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    /** Пол: true – мужской, false – женский */
    @Column(name = "male")
    private Boolean male;

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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Boolean getMale() {
        return male;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }
}
