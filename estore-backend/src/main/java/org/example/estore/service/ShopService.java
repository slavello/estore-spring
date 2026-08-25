package org.example.estore.service;

import org.example.estore.dto.ShopDto;
import org.example.estore.entity.Shop;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {

    private final ShopRepository shops;

    public ShopService(ShopRepository shops) {
        this.shops = shops;
    }

    @Transactional(readOnly = true)
    public Page<ShopDto> list(Pageable pageable) {
        return shops.findAll(pageable).map(ShopDto::of);
    }

    @Transactional(readOnly = true)
    public ShopDto get(Long id) {
        return ShopDto.of(find(id));
    }

    @Transactional
    public ShopDto create(ShopDto d) {
        Shop s = new Shop();
        apply(s, d);
        return ShopDto.of(shops.save(s));
    }

    @Transactional
    public ShopDto update(Long id, ShopDto d) {
        Shop s = find(id);
        apply(s, d);
        return ShopDto.of(shops.save(s));
    }

    @Transactional
    public void delete(Long id) {
        shops.delete(find(id));
    }

    private Shop find(Long id) {
        return shops
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Магазин не найден: id=" + id));
    }

    private void apply(Shop s, ShopDto d) {
        s.setName(d.getName().trim());
        s.setAddress(d.getAddress() == null ? null : d.getAddress().trim());
    }
}
