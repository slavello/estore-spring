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
     * Продажи выбранного товара сотрудниками выбранной должности за период
     * с начальной даты (для поиска лучшего продавца товара).
     */
    @Query(
        "select p.id as positionId, p.name as positionName, e.id as employeeId, " +
        "e.lastName as lastName, e.firstName as firstName, e.middleName as middleName, " +
        "count(pu.id) as soldCount, coalesce(sum(el.price), 0) as soldSum " +
        "from Purchase pu " +
        " join pu.employee e " +
        " join e.position p " +
        " join pu.electronics el " +
        "where el.id = :electronicsId and p.id = :positionId and pu.dateTime >= :since " +
        "group by p.id, p.name, e.id, e.lastName, e.firstName, e.middleName " +
        "order by count(pu.id) desc")
    List<BestEmployeeRow> findSalesByElectronicsIdAndPositionId(
        @Param("electronicsId") Long electronicsId,
        @Param("positionId") Long positionId,
        @Param("since") LocalDateTime since
    );

    /** Сумма денежных средств, полученных через оплату выбранного типа за период с начальной даты (по всей сети) */
    @Query(
        "select coalesce(sum(el.price), 0) " +
        "from Purchase pu " +
        " join pu.electronics el " +
        "where pu.purchaseType.id = :purchaseTypeId and pu.dateTime >= :since")
    BigDecimal sumSoldAmountByPurchaseTypeId(
        @Param("purchaseTypeId") Long purchaseTypeId,
        @Param("since") LocalDateTime since
    );

    /** Сумма денежных средств, полученных через оплату выбранного типа за период с начальной даты (по магазину) */
    @Query(
        "select coalesce(sum(el.price), 0) " +
        "from Purchase pu " +
        " join pu.electronics el " +
        "where pu.purchaseType.id = :purchaseTypeId and pu.shop.id = :shopId and pu.dateTime >= :since")
    BigDecimal sumSoldAmountByPurchaseTypeIdAndShop(
        @Param("purchaseTypeId") Long purchaseTypeId,
        @Param("shopId") Long shopId,
        @Param("since") LocalDateTime since
    );
}
