package com.distribution.service;

import com.distribution.dto.CustomerDTO;
import com.distribution.dto.DeliveryAddressDTO;

import java.util.List;

/**
 * Service interface for Customer operations
 */
public interface CustomerService {
    
    /**
     * Create a new Customer
     */
    CustomerDTO create(CustomerDTO dto);
    
    /**
     * Update an existing Customer
     */
    CustomerDTO update(Long id, CustomerDTO dto);
    
    /**
     * Get Customer by ID
     */
    CustomerDTO getById(Long id);
    
    /**
     * Get Customer by code
     */
    CustomerDTO getByCode(String code);
    
    /**
     * Get all Customers
     */
    List<CustomerDTO> getAll();
    
    /**
     * Get all active Customers
     */
    List<CustomerDTO> getActiveCustomers();
    
    /**
     * Search customers
     */
    List<CustomerDTO> search(String query);
    
    /**
     * Delete Customer (soft delete)
     */
    void delete(Long id);
    
    // Delivery Address operations
    
    /**
     * Add delivery address to customer
     */
    DeliveryAddressDTO addDeliveryAddress(Long customerId, DeliveryAddressDTO dto);
    
    /**
     * Update delivery address
     */
    DeliveryAddressDTO updateDeliveryAddress(Long addressId, DeliveryAddressDTO dto);
    
    /**
     * Get delivery addresses for customer
     */
    List<DeliveryAddressDTO> getDeliveryAddresses(Long customerId);
    
    /**
     * Delete delivery address
     */
    void deleteDeliveryAddress(Long addressId);
    
    /**
     * Set default delivery address
     */
    void setDefaultAddress(Long customerId, Long addressId);
}
