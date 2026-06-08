package com.distribution.service.impl;

import com.distribution.dto.WarehouseDTO;
import com.distribution.model.Warehouse;
import com.distribution.repository.WarehouseRepository;
import com.distribution.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repo;

    private WarehouseDTO toDto(Warehouse w){
        return WarehouseDTO.builder()
                .id(w.getId())
                .code(w.getCode())
                .name(w.getName())
                .location(w.getLocation())
                .description(w.getDescription())
                .build();
    }

    private Warehouse toEntity(WarehouseDTO dto){
        return Warehouse.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .build();
    }

    @Override
    public WarehouseDTO create(WarehouseDTO dto) {
        return toDto(repo.save(toEntity(dto)));
    }

    @Override
    public WarehouseDTO update(Long id, WarehouseDTO dto) {
        Warehouse w = repo.findById(id).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        w.setCode(dto.getCode());
        w.setName(dto.getName());
        w.setLocation(dto.getLocation());
        w.setDescription(dto.getDescription());
        return toDto(repo.save(w));
    }

    @Override
    public WarehouseDTO getById(Long id) {
        return repo.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }

    @Override
    public List<WarehouseDTO> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

