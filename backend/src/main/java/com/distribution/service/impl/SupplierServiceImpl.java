package com.distribution.service.impl;

import com.distribution.dto.SupplierDTO;
import com.distribution.model.Supplier;
import com.distribution.repository.SupplierRepository;
import com.distribution.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repo;

    private SupplierDTO toDto(Supplier s){
        return SupplierDTO.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .contactName(s.getContactName())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .build();
    }

    private Supplier toEntity(SupplierDTO dto){
        return Supplier.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .contactName(dto.getContactName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .build();
    }

    @Override
    public SupplierDTO create(SupplierDTO dto) {
        return toDto(repo.save(toEntity(dto)));
    }

    @Override
    public SupplierDTO update(Long id, SupplierDTO dto) {
        Supplier s = repo.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
        s.setCode(dto.getCode());
        s.setName(dto.getName());
        s.setContactName(dto.getContactName());
        s.setPhone(dto.getPhone());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
        return toDto(repo.save(s));
    }

    @Override
    public SupplierDTO getById(Long id) {
        return repo.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    @Override
    public List<SupplierDTO> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
