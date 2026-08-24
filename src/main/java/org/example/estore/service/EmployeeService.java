package org.example.estore.service;

import org.example.estore.dto.EmployeeDto;
import org.example.estore.entity.Employee;
import org.example.estore.exception.NotFoundException;
import org.example.estore.repository.EmployeeRepository;
import org.example.estore.repository.PositionRepository;
import org.example.estore.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employees;
    private final PositionRepository positions;
    private final ShopRepository shops;

    public EmployeeService(EmployeeRepository employees, PositionRepository positions, ShopRepository shops) {
        this.employees = employees;
        this.positions = positions;
        this.shops = shops;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> list(Pageable pageable) {
        return employees.findAll(pageable).map(EmployeeDto::of);
    }

    @Transactional(readOnly = true)
    public EmployeeDto get(Long id) {
        return EmployeeDto.of(find(id));
    }

    @Transactional
    public EmployeeDto create(EmployeeDto dto) {
        Employee e = new Employee();
        apply(e, dto);
        return EmployeeDto.of(employees.save(e));
    }

    @Transactional
    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee e = find(id);
        apply(e, dto);
        return EmployeeDto.of(employees.save(e));
    }

    @Transactional
    public void delete(Long id) {
        employees.delete(find(id));
    }

    private Employee find(Long id) {
        return employees
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Сотрудник не найден: id=" + id));
    }

    private void apply(Employee e, EmployeeDto d) {
        e.setLastName(d.getLastName().trim());
        e.setFirstName(d.getFirstName().trim());
        e.setMiddleName(d.getMiddleName() == null ? null : d.getMiddleName().trim());
        e.setBirthDate(d.getBirthDate());
        e.setPosition(
            positions
                .findById(d.getPositionId())
                .orElseThrow(() -> new NotFoundException("Должность не найдена: id=" + d.getPositionId())));
        e.setShop(
            shops
                .findById(d.getShopId())
                .orElseThrow(() -> new NotFoundException("Магазин не найден: id=" + d.getShopId())));
        e.setMale(d.getMale());
    }
}
