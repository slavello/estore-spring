package org.example.estore.service;

import org.example.estore.dto.PositionDto;
import org.example.estore.entity.Position;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positions;

    public PositionService(PositionRepository positions) {
        this.positions = positions;
    }

    @Transactional(readOnly = true)
    public Page<PositionDto> list(Pageable pageable) {
        return positions.findAll(pageable).map(PositionDto::of);
    }

    @Transactional(readOnly = true)
    public PositionDto get(Long id) {
        return PositionDto.of(find(id));
    }

    @Transactional
    public PositionDto create(PositionDto d) {
        Position p = new Position();
        apply(p, d);
        return PositionDto.of(positions.save(p));
    }

    @Transactional
    public PositionDto update(Long id, PositionDto d) {
        Position p = find(id);
        apply(p, d);
        return PositionDto.of(positions.save(p));
    }

    @Transactional
    public void delete(Long id) {
        positions.delete(find(id));
    }

    private Position find(Long id) {
        return positions
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Должность не найдена: id=" + id));
    }

    private void apply(Position p, PositionDto d) {
        p.setName(d.getName().trim());
    }
}
