package com.distribution.repository;

import com.distribution.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);
    
    @EntityGraph(attributePaths = {"supplier"})
    @Override
    List<Product> findAll();
    
    @EntityGraph(attributePaths = {"supplier"})
    @Override
    Optional<Product> findById(Long id);
}
