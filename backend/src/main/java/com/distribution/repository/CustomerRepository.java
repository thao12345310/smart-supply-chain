package com.distribution.repository;

import com.distribution.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByCode(String code);
    
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByActiveTrue();
    
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Customer c WHERE c.active = true ORDER BY c.name")
    List<Customer> findAllActive();
    
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Customer> search(String search);
    
    boolean existsByCode(String code);
    
    boolean existsByEmail(String email);
}
