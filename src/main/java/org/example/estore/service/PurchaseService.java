package org.example.estore.service;

import org.example.estore.dto.PurchaseDto;
import org.example.estore.entity.*;
import org.example.estore.exception.NotFoundException;
import org.example.estore.exception.OutOfStockException;
import org.example.estore.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseService {

    private final PurchaseRepository purchases;
    private final ElectronicsRepository electronics;
    private final EmployeeRepository employees;
    private final ShopRepository shops;
    private final PurchaseTypeRepository purchaseTypes;
    private final ShopStockRepository stocks;

    public PurchaseService(
        PurchaseRepository purchases,
        ElectronicsRepository electronics,
        EmployeeRepository employees,
        ShopRepository shops,
        PurchaseTypeRepository purchaseTypes,
        ShopStockRepository stocks)
    {
        this.purchases = purchases;
        this.electronics = electronics;
        this.employees = employees;
        this.shops = shops;
        this.purchaseTypes = purchaseTypes;
        this.stocks = stocks;
    }

    /** Постраничный просмотр с сортировкой по дате покупки (по умолчанию — по убыванию) */
    @Transactional(readOnly = true)
    public Page<PurchaseDto> list(Pageable pageable) {
        Sort sort =
            pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "dateTime");
        Page<Purchase> page = purchases.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        List<PurchaseDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PurchaseDto get(Long id) {
        return toDto(find(id));
    }

    /**
     * Оформление покупки с проверкой наличия товара в магазине.
     * Если товара нет в наличии (или товар архивный) — запись не создаётся,
     * выбрасывается OutOfStockException с сообщением для пользователя.
     */
    @Transactional
    public PurchaseDto create(PurchaseDto d) {
        Purchase p = new Purchase();
        p.setElectronics(resolveElectronics(d.getElectronicsId()));
        p.setEmployee(resolveEmployee(d.getEmployeeId()));
        p.setShop(resolveShop(d.getShopId()));
        p.setPurchaseType(resolvePurchaseType(d.getPurchaseTypeId()));
        p.setDateTime(d.getDateTime() != null ? d.getDateTime() : LocalDateTime.now());

        registerSale(p.getElectronics(), p.getShop());
        return toDto(purchases.save(p));
    }

    /**
     * Редактирование покупки
     */
    @Transactional
    public PurchaseDto update(Long id, PurchaseDto d) {
        Purchase p = find(id);
        //releaseSale(p.getElectronics(), p.getShop());

        p.setElectronics(resolveElectronics(d.getElectronicsId()));
        p.setEmployee(resolveEmployee(d.getEmployeeId()));
        p.setShop(resolveShop(d.getShopId()));
        p.setPurchaseType(resolvePurchaseType(d.getPurchaseTypeId()));
        if (d.getDateTime() != null) {
            p.setDateTime(d.getDateTime());
        }
        //registerSale(p.getElectronics(), p.getShop());
        return toDto(purchases.save(p));
    }

    /** Удаление покупки возвращает товар на склад */
    @Transactional
    public void delete(Long id) {
        Purchase p = find(id);
        releaseSale(p.getElectronics(), p.getShop());
        purchases.delete(p);
    }

    /** Проверка наличия и списание единицы товара (общий остаток + остаток магазина) */
    public void registerSale(Electronics el, Shop shop) {
        if (Boolean.TRUE.equals(el.getArchived())) {
            throw new OutOfStockException(String.format(
                "Товар \"%s\" снят с продаж (архивный). Покупка не оформлена.", el.getName())
            );
        }
        ShopStock stock = stocks.findByElectronicsIdAndShopId(el.getId(), shop.getId()).orElse(null);
        int availableInShop = stock != null && stock.getQuantity() != null ? stock.getQuantity() : 0;
        int totalAvailable = el.getQuantity() != null ? el.getQuantity() : 0;
        if (availableInShop <= 0 || totalAvailable <= 0) {
            throw new OutOfStockException(String.format(
                "Товара \"%s\" нет в наличии в магазине \"%s\". Покупка не оформлена.", el.getName(), shop.getName())
            );
        }
        stock.setQuantity(availableInShop - 1);
        stocks.save(stock);
        el.setQuantity(totalAvailable - 1);
        electronics.save(el);
    }

    /** Возврат единицы товара на склад */
    public void releaseSale(Electronics el, Shop shop) {
        el.setQuantity((el.getQuantity() == null ? 0 : el.getQuantity()) + 1);
        electronics.save(el);
        ShopStock stock = stocks.findByElectronicsIdAndShopId(el.getId(), shop.getId()).orElse(null);
        if (stock == null) {
            stock = new ShopStock();
            stock.setElectronics(el);
            stock.setShop(shop);
            stock.setQuantity(0);
        }
        stock.setQuantity(stock.getQuantity() + 1);
        stocks.save(stock);
    }

    private Purchase find(Long id) {
        return purchases
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Покупка не найдена: id=" + id));
    }

    private PurchaseDto toDto(Purchase p) {
        PurchaseDto d = new PurchaseDto();
        d.setId(p.getId());
        d.setDateTime(p.getDateTime());
        if (p.getElectronics() != null) {
            d.setElectronicsId(p.getElectronics().getId());
            d.setElectronicsName(p.getElectronics().getName());
            BigDecimal price = p.getElectronics().getPrice();
            d.setPrice(price);
        }
        if (p.getEmployee() != null) {
            d.setEmployeeId(p.getEmployee().getId());
            d.setEmployeeName(fullName(p.getEmployee()));
        }
        if (p.getShop() != null) {
            d.setShopId(p.getShop().getId());
            d.setShopName(p.getShop().getName());
        }
        if (p.getPurchaseType() != null) {
            d.setPurchaseTypeId(p.getPurchaseType().getId());
            d.setPurchaseTypeName(p.getPurchaseType().getName());
        }
        return d;
    }

    private String fullName(Employee e) {
        StringBuilder sb = new StringBuilder();
        append(sb, e.getLastName());
        append(sb, e.getFirstName());
        append(sb, e.getMiddleName());
        return sb.toString().trim();
    }

    private void append(StringBuilder sb, String part) {
        if (part != null && !part.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part);
        }
    }

    private Electronics resolveElectronics(Long id) {
        return electronics
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Электротовар не найден: id=" + id));
    }

    private Employee resolveEmployee(Long id) {
        return employees
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Сотрудник не найден: id=" + id));
    }

    private Shop resolveShop(Long id) {
        return shops
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Магазин не найден: id=" + id));
    }

    private PurchaseType resolvePurchaseType(Long id) {
        return purchaseTypes
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Тип покупки не найден: id=" + id));
    }
}
