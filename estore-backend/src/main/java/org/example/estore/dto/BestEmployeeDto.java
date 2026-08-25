package org.example.estore.dto;

/** Результат отчёта "Лучшие сотрудники по должности за последний год" */
public class BestEmployeeDto {

    private Long positionId;
    private String positionName;
    private Long employeeId;
    private String fullName;

    /** Количество проданных товаров за последний год */
    private long soldCount;

    /** Сумма проданных товаров за последний год, руб. */
    private java.math.BigDecimal soldSum;

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(long soldCount) {
        this.soldCount = soldCount;
    }

    public java.math.BigDecimal getSoldSum() {
        return soldSum;
    }

    public void setSoldSum(java.math.BigDecimal soldSum) {
        this.soldSum = soldSum;
    }
}
