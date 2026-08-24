package org.example.estore.repository;

import org.example.estore.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** Все покупки с сортировкой по дате/времени по убыванию */
    List<Purchase> findAllByOrderByDateTimeDesc();

    /**
     * Агрегированные продажи сотрудников за период (последний год):
     * количество проданных товаров и сумма проданных товаров,
     * в разрезе должностей — для вывода лучших сотрудников.
     */
    @Query(
        "select p.id as positionId, p.name as positionName, e.id as employeeId, " +
        "e.lastName as lastName, e.firstName as firstName, e.middleName as middleName, " +
        "count(pu.id) as soldCount, coalesce(sum(el.price), 0) as soldSum " +
        "from Purchase pu " +
        " join pu.employee e " +
        " join e.position p " +
        " join pu.electronics el " +
        "where pu.dateTime >= :since " +
        "group by p.id, p.name, e.id, e.lastName, e.firstName, e.middleName")
    List<BestEmployeeRow> aggregateSalesSince(@Param("since") LocalDateTime since);

    /**
     * Продажи умных часов сотрудниками определённой должности
     * (для поиска лучшего младшего продавца-консультанта).
     */
    @Query(
        "select p.id as positionId, p.name as positionName, e.id as employeeId, " +
        "e.lastName as lastName, e.firstName as firstName, e.middleName as middleName, " +
        "count(pu.id) as soldCount, coalesce(sum(el.price), 0) as soldSum " +
        "from Purchase pu " +
        " join pu.employee e " +
        " join e.position p " +
        " join pu.electronics el " +
        " join el.type t " +
        "where lower(t.name) = lower(:typeName) and lower(p.name) = lower(:positionName) " +
        "group by p.id, p.name, e.id, e.lastName, e.firstName, e.middleName " +
        "order by count(pu.id) desc")
    List<BestEmployeeRow> findSalesByTypeNameAndPositionName(
        @Param("typeName") String typeName,
        @Param("positionName") String positionName
    );

    /** Сумма денежных средств, полученных через оплату (по всей сети или по магазину) */
    @Query(
        "select coalesce(sum(el.price), 0) " +
        "from Purchase pu " +
        " join pu.electronics el " +
        " join pu.purchaseType pt " +
        "where lower(pt.name) = lower(:typeName)")
    BigDecimal sumSoldAmountByPurchaseTypeName(@Param("typeName") String typeName);

    @Query(
        "select coalesce(sum(el.price), 0) " +
        "from Purchase pu " +
        " join pu.electronics el " +
        " join pu.purchaseType pt " +
        "where lower(pt.name) = lower(:typeName) and pu.shop.id = :shopId")
    BigDecimal sumSoldAmountByPurchaseTypeNameAndShop(
        @Param("typeName") String typeName,
        @Param("shopId") Long shopId
    );
}
