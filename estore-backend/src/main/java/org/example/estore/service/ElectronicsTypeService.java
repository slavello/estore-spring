package org.example.estore.service;

import org.example.estore.dto.ElectronicsTypeDto;
import org.example.estore.entity.ElectronicsType;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.ElectronicsTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectronicsTypeService {

    private final ElectronicsTypeRepository types;

    public ElectronicsTypeService(ElectronicsTypeRepository types) {
        this.types = types;
    }

    @Transactional(readOnly = true)
    public Page<ElectronicsTypeDto> list(Pageable pageable) {
        return types.findAll(pageable).map(ElectronicsTypeDto::of);
    }

    @Transactional(readOnly = true)
    public ElectronicsTypeDto get(Long id) {
        return ElectronicsTypeDto.of(find(id));
    }

    @Transactional
    public ElectronicsTypeDto create(ElectronicsTypeDto d) {
        ElectronicsType t = new ElectronicsType();
        apply(t, d);
        return ElectronicsTypeDto.of(types.save(t));
    }

    @Transactional
    public ElectronicsTypeDto update(Long id, ElectronicsTypeDto d) {
        ElectronicsType t = find(id);
        apply(t, d);
        return ElectronicsTypeDto.of(types.save(t));
    }

    @Transactional
    public void delete(Long id) {
        types.delete(find(id));
    }

    private ElectronicsType find(Long id) {
        return types
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Тип электроники не найден: id=" + id));
    }

    private void apply(ElectronicsType t, ElectronicsTypeDto d) {
        t.setName(d.getName().trim());
    }
}
