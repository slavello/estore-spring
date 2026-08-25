package org.example.estore.service;

import org.example.estore.dto.ElectronicsDto;
import org.example.estore.entity.Electronics;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.ElectronicsRepository;
import org.example.estore.repository.ElectronicsTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectronicsService {

    private final ElectronicsRepository electronics;
    private final ElectronicsTypeRepository types;

    public ElectronicsService(ElectronicsRepository electronics, ElectronicsTypeRepository types) {
        this.electronics = electronics;
        this.types = types;
    }

    @Transactional(readOnly = true)
    public Page<ElectronicsDto> list(Pageable pageable) {
        return electronics.findAll(pageable).map(ElectronicsDto::of);
    }

    @Transactional(readOnly = true)
    public ElectronicsDto get(Long id) {
        return ElectronicsDto.of(find(id));
    }

    @Transactional
    public ElectronicsDto create(ElectronicsDto d) {
        Electronics e = new Electronics();
        apply(e, d);
        return ElectronicsDto.of(electronics.save(e));
    }

    @Transactional
    public ElectronicsDto update(Long id, ElectronicsDto d) {
        Electronics e = find(id);
        apply(e, d);
        return ElectronicsDto.of(electronics.save(e));
    }

    @Transactional
    public void delete(Long id) {
        electronics.delete(find(id));
    }

    private Electronics find(Long id) {
        return electronics
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Электротовар не найден: id=" + id));
    }

    private void apply(Electronics e, ElectronicsDto d) {
        e.setName(d.getName() == null ? null : d.getName().trim());
        e.setType(
            types
                .findById(d.getTypeId())
                .orElseThrow(() -> new NotFoundException("Тип электроники не найден: id=" + d.getTypeId())));
        e.setPrice(d.getPrice());
        e.setQuantity(d.getQuantity() == null ? 0 : d.getQuantity());
        e.setArchived(d.getArchived() != null && d.getArchived());
        e.setDescription(d.getDescription());
    }
}
