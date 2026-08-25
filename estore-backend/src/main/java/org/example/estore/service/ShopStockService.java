package org.example.estore.service;

import org.example.estore.dto.ShopStockDto;
import org.example.estore.entity.Electronics;
import org.example.estore.entity.Shop;
import org.example.estore.entity.ShopStock;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.ElectronicsRepository;
import org.example.estore.repository.ShopRepository;
import org.example.estore.repository.ShopStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopStockService {

    private final ShopStockRepository stocks;
    private final ElectronicsRepository electronics;
    private final ShopRepository shops;

    public ShopStockService(
        ShopStockRepository stocks,
        ElectronicsRepository electronics,
        ShopRepository shops)
    {
        this.stocks = stocks;
        this.electronics = electronics;
        this.shops = shops;
    }

    @Transactional(readOnly = true)
    public Page<ShopStockDto> list(Pageable pageable) {
        return stocks.findAll(pageable).map(this::toDto);
    }

    /** Создание или обновление остатка по паре "товар-магазин" */
    @Transactional
    public ShopStockDto save(ShopStockDto d) {
        Electronics el = electronics
            .findById(d.getElectronicsId())
            .orElseThrow(() -> new NotFoundException("Электротовар не найден: id=" + d.getElectronicsId()));
        Shop shop = shops
            .findById(d.getShopId())
            .orElseThrow(() -> new NotFoundException("Магазин не найден: id=" + d.getShopId()));

        ShopStock stock = stocks.findByElectronicsIdAndShopId(el.getId(), shop.getId()).orElseGet(() -> {
            ShopStock s = new ShopStock();
            s.setElectronics(el);
            s.setShop(shop);
            return s;
        });
        stock.setQuantity(d.getQuantity() == null ? 0 : Math.max(0, d.getQuantity()));
        return toDto(stocks.save(stock));
    }

    @Transactional
    public void delete(Long id) {
        ShopStock stock = stocks
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Запись о наличии не найдена: id=" + id));
        stocks.delete(stock);
    }

    private ShopStockDto toDto(ShopStock s) {
        ShopStockDto d = new ShopStockDto();
        d.setId(s.getId());
        d.setQuantity(s.getQuantity());
        if (s.getElectronics() != null) {
            d.setElectronicsId(s.getElectronics().getId());
            d.setElectronicsName(s.getElectronics().getName());
        }
        if (s.getShop() != null) {
            d.setShopId(s.getShop().getId());
            d.setShopName(s.getShop().getName());
        }
        return d;
    }
}
