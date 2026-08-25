package org.example.estore.service;

import org.example.estore.dto.PurchaseTypeDto;
import org.example.estore.entity.PurchaseType;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.PurchaseTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseTypeService {

    private final PurchaseTypeRepository purchaseTypes;

    public PurchaseTypeService(PurchaseTypeRepository purchaseTypes) {
        this.purchaseTypes = purchaseTypes;
    }

    @Transactional(readOnly = true)
    public Page<PurchaseTypeDto> list(Pageable pageable) {
        return purchaseTypes.findAll(pageable).map(PurchaseTypeDto::of);
    }

    @Transactional(readOnly = true)
    public PurchaseTypeDto get(Long id) {
        return PurchaseTypeDto.of(find(id));
    }

    @Transactional
    public PurchaseTypeDto create(PurchaseTypeDto d) {
        PurchaseType t = new PurchaseType();
        apply(t, d);
        return PurchaseTypeDto.of(purchaseTypes.save(t));
    }

    @Transactional
    public PurchaseTypeDto update(Long id, PurchaseTypeDto d) {
        PurchaseType t = find(id);
        apply(t, d);
        return PurchaseTypeDto.of(purchaseTypes.save(t));
    }

    @Transactional
    public void delete(Long id) {
        purchaseTypes.delete(find(id));
    }

    private PurchaseType find(Long id) {
        return purchaseTypes
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Тип покупки не найден: id=" + id));
    }

    private void apply(PurchaseType t, PurchaseTypeDto d) {
        t.setName(d.getName().trim());
    }
}
