package org.example.estore.service;

import org.example.estore.dto.BestEmployeeDto;
import org.example.estore.dto.CashTotalDto;
import org.example.estore.entity.Electronics;
import org.example.estore.entity.Position;
import org.example.estore.entity.PurchaseType;
import org.example.estore.entity.Shop;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.BestEmployeeRow;
import org.example.estore.repository.ElectronicsRepository;
import org.example.estore.repository.PositionRepository;
import org.example.estore.repository.PurchaseRepository;
import org.example.estore.repository.PurchaseTypeRepository;
import org.example.estore.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 2) лучший сотрудник выбранной должности, продавший больше всех выбранного товара;
 * 3) сумма денежных средств, полученная через оплату выбранным типом оплаты.
 */
@Service
public class ReportService {

    /** "Последний го"» — скользящий период: последние 12 месяцев от текущей даты */
    public static final String PERIOD_DESCRIPTION = "за последний год (последние 12 месяцев)";

    /** Условная граница «с самого начала»: покупки раньше этой даты невозможны */
    private static final LocalDateTime ALL_TIME = LocalDate.of(1900, 1, 1).atStartOfDay();

    private final PurchaseRepository purchases;
    private final ShopRepository shops;
    private final ElectronicsRepository electronics;
    private final PurchaseTypeRepository purchaseTypes;
    private final PositionRepository positions;

    public ReportService(
        PurchaseRepository purchases,
        ShopRepository shops,
        ElectronicsRepository electronics,
        PurchaseTypeRepository purchaseTypes,
        PositionRepository positions)
    {
        this.purchases = purchases;
        this.shops = shops;
        this.electronics = electronics;
        this.purchaseTypes = purchaseTypes;
        this.positions = positions;
    }

    /**
     * Лучший сотрудник в каждой должности.
     *
     * @param criterion
     *     "count" - по количеству проданных товаров,
     *     "sum" - по сумме проданных товаров
     * @param startDate начальная дата периода (без даты — последний год)
     */
    @Transactional(readOnly = true)
    public List<BestEmployeeDto> bestEmployeesByPosition(String criterion, LocalDate startDate) {
        String c = criterion == null ? "count" : criterion.trim().toLowerCase();
        boolean bySum = c.equals("sum") || c.equals("amount") || c.equals("сумма");

        LocalDateTime since =
            startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(1);
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
     * Лучший сотрудник выбранной должности, продавший больше всех выбранного товара
     * за период с начальной даты
     * (возвращается отсортированный по убыванию список; первый элемент — лучший).
     *
     * @param startDate начальная дата периода (без даты — всё время)
     */
    @Transactional(readOnly = true)
    public List<BestEmployeeDto> bestProductSellers(Long electronicsId, Long positionId, LocalDate startDate) {
        Electronics product = electronics
            .findById(electronicsId)
            .orElseThrow(() -> new NotFoundException("Товар не найден: id=" + electronicsId));
        Position position = positions
            .findById(positionId)
            .orElseThrow(() -> new NotFoundException("Должность не найдена: id=" + positionId));

        List<BestEmployeeDto> result = new ArrayList<>();
        for (BestEmployeeRow row :
            purchases.findSalesByElectronicsIdAndPositionId(
                product.getId(), position.getId(), sinceOrAllTime(startDate)))
        {
            result.add(toDto(row));
        }
        return result;
    }

    /**
     * Сумма денежных средств, полученная через оплату выбранным типом оплаты
     * за период с начальной даты (по сети или конкретному магазину).
     *
     * @param startDate начальная дата периода (без даты — всё время)
     */
    @Transactional(readOnly = true)
    public CashTotalDto cashTotal(Long shopId, Long purchaseTypeId, LocalDate startDate) {
        PurchaseType type = purchaseTypes
            .findById(purchaseTypeId)
            .orElseThrow(() -> new NotFoundException("Тип покупки не найден: id=" + purchaseTypeId));

        LocalDateTime since = sinceOrAllTime(startDate);
        BigDecimal amount =
            shopId == null
                ? purchases.sumSoldAmountByPurchaseTypeId(type.getId(), since)
                : purchases.sumSoldAmountByPurchaseTypeIdAndShop(type.getId(), shopId, since);

        CashTotalDto dto = new CashTotalDto();
        dto.setPurchaseTypeId(type.getId());
        dto.setPurchaseTypeName(type.getName());
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

    /** Начальная граница периода: указанная дата либо «с самого начала» */
    private LocalDateTime sinceOrAllTime(LocalDate startDate) {
        return startDate != null ? startDate.atStartOfDay() : ALL_TIME;
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
