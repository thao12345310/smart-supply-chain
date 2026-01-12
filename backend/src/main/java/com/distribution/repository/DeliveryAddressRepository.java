package com.distribution.repository;

import com.distribution.model.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    
    List<DeliveryAddress> findByCustomerId(Long customerId);
    
    @Query("SELECT da FROM DeliveryAddress da WHERE da.customer.id = :customerId AND da.isDefault = true")
    Optional<DeliveryAddress> findDefaultByCustomerId(Long customerId);
    
    @Query("SELECT da FROM DeliveryAddress da WHERE da.customer.id = :customerId ORDER BY da.isDefault DESC, da.addressName")
    List<DeliveryAddress> findByCustomerIdOrdered(Long customerId);
}
