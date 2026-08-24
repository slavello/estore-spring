package org.example.estore.service;

import org.example.estore.dto.BestEmployeeDto;
import org.example.estore.dto.CashTotalDto;
import org.example.estore.entity.Shop;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.BestEmployeeRow;
import org.example.estore.repository.PurchaseRepository;
import org.example.estore.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Отчёты по ТЗ:
 * 1) лучшие сотрудники по должности за последний год:
 *    - количество проданных товаров;
 *    - сумма проданных товаров (за последний год);
 * 2) лучший младший продавец-консультант, продавший больше всех умных часов;
 * 3) сумма денежных средств, полученная магазином через оплату наличными.
 */
@Service
public class ReportService {

    /** "Последний го"» — скользящий период: последние 12 месяцев от текущей даты */
    public static final String PERIOD_DESCRIPTION = "за последний год (последние 12 месяцев)";

    public static final String SMART_WATCH_TYPE = "Умные часы";
    public static final String JUNIOR_CONSULTANT_POSITION = "Младший продавец-консультант";
    public static final String CASH_PAYMENT_TYPE = "Наличные";

    private final PurchaseRepository purchases;
    private final ShopRepository shops;

    public ReportService(PurchaseRepository purchases, ShopRepository shops) {
        this.purchases = purchases;
        this.shops = shops;
    }

    /**
     * Лучший сотрудник в каждой должности.
     *
     * @param criterion
     *     "count" - по количеству проданных товаров,
     *     "sum" - по сумме проданных товаров
     */
    @Transactional(readOnly = true)
    public List<BestEmployeeDto> bestEmployeesByPosition(String criterion) {
        String c = criterion == null ? "count" : criterion.trim().toLowerCase();
        boolean bySum = c.equals("sum") || c.equals("amount") || c.equals("сумма");

        LocalDateTime since = LocalDateTime.now().minusYears(1);
        Map<Long, BestEmployeeDto> bestByPosition = new HashMap<>();

        for (BestEmployeeRow row : purchases.aggregateSalesSince(since)) {
            BestEmployeeDto current = bestByPosition.get(row.getPositionId());
            boolean better;
            if (current == null) {
                better = true;
            } else if (bySum) {
                better = row.getSoldSum().compareTo(current.getSoldSum()) > 0;
            } else {
                better = row.getSoldCount() > current.getSoldCount();
            }
            if (better) {
                bestByPosition.put(row.getPositionId(), toDto(row));
            }
        }

        List<BestEmployeeDto> result = new ArrayList<>(bestByPosition.values());
        result.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getPositionName(), b.getPositionName()));
        return result;
    }

    /**
     * Лучший младший продавец-консультант, продавший больше всех умных часов
     * (возвращается отсортированный по убыванию список; первый элемент — лучший).
     */
    @Transactional(readOnly = true)
    public List<BestEmployeeDto> bestSmartWatchSellers() {
        List<BestEmployeeDto> result = new ArrayList<>();
        for (BestEmployeeRow row :
            purchases.findSalesByTypeNameAndPositionName(SMART_WATCH_TYPE, JUNIOR_CONSULTANT_POSITION))
        {
            result.add(toDto(row));
        }
        return result;
    }

    /** Сумма денежных средств, полученная через оплату наличными (по сети или конкретному магазину) */
    @Transactional(readOnly = true)
    public CashTotalDto cashTotal(Long shopId) {
        BigDecimal amount =
            shopId == null
                ? purchases.sumSoldAmountByPurchaseTypeName(CASH_PAYMENT_TYPE)
                : purchases.sumSoldAmountByPurchaseTypeNameAndShop(CASH_PAYMENT_TYPE, shopId);

        CashTotalDto dto = new CashTotalDto();
        dto.setAmount(amount == null ? BigDecimal.ZERO : amount);
        if (shopId != null) {
            Shop shop = shops
                .findById(shopId)
                .orElseThrow(() -> new NotFoundException("Магазин не найден: id=" + shopId));
            dto.setShopId(shop.getId());
            dto.setShopName(shop.getName());
        }
        return dto;
    }

    private BestEmployeeDto toDto(BestEmployeeRow row) {
        BestEmployeeDto d = new BestEmployeeDto();
        d.setPositionId(row.getPositionId());
        d.setPositionName(row.getPositionName());
        d.setEmployeeId(row.getEmployeeId());

        StringBuilder sb = new StringBuilder();
        append(sb, row.getLastName());
        append(sb, row.getFirstName());
        append(sb, row.getMiddleName());
        d.setFullName(sb.toString().trim());

        long count = row.getSoldCount() == null ? 0 : row.getSoldCount();
        d.setSoldCount(count);
        d.setSoldSum(row.getSoldSum() == null ? BigDecimal.ZERO : row.getSoldSum());
        return d;
    }

    private void append(StringBuilder sb, String part) {
        if (part != null && !part.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part.trim());
        }
    }
}
