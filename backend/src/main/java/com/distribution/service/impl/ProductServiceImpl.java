package com.distribution.service.impl;

import com.distribution.dto.ProductDTO;
import com.distribution.model.Product;
import com.distribution.repository.ProductRepository;
import com.distribution.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;

    private ProductDTO toDto(Product p){
        return ProductDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .quantity(p.getQuantity())
                .price(p.getPrice())
                .build();
    }

    private Product toEntity(ProductDTO dto){
        return Product.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .build();
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        Product p = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        p.setName(dto.getName());
        p.setCode(dto.getCode());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setQuantity(dto.getQuantity());
        return toDto(repo.save(p));
    }

    @Override
    public ProductDTO getById(Long id) {
        return repo.findById(id).map(this::toDto).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<ProductDTO> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
