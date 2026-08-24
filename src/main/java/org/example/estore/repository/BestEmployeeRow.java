package org.example.estore.repository;

import java.math.BigDecimal;

public interface BestEmployeeRow {

    Long getPositionId();

    String getPositionName();

    Long getEmployeeId();

    String getLastName();

    String getFirstName();

    String getMiddleName();

    /** Количество проданных товаров */
    Long getSoldCount();

    /** Сумма проданных товаров, руб. */
    BigDecimal getSoldSum();
}
