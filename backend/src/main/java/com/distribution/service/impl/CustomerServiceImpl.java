package com.distribution.service.impl;

import com.distribution.dto.CustomerDTO;
import com.distribution.dto.DeliveryAddressDTO;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.exception.BusinessException;
import com.distribution.model.Customer;
import com.distribution.model.DeliveryAddress;
import com.distribution.repository.CustomerRepository;
import com.distribution.repository.DeliveryAddressRepository;
import com.distribution.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;

    @Override
    public CustomerDTO create(CustomerDTO dto) {
        log.info("Creating customer: {}", dto.getName());
        
        // Generate code if not provided
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            dto.setCode(Customer.generateCode());
        }
        
        // Check for duplicate code
        if (customerRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("Customer with code " + dto.getCode() + " already exists");
        }
        
        // Check for duplicate email if provided
        if (dto.getEmail() != null && !dto.getEmail().isBlank() 
            && customerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Customer with email " + dto.getEmail() + " already exists");
        }
        
        Customer customer = mapToEntity(dto);
        customer = customerRepository.save(customer);
        
        log.info("Customer created with ID: {}", customer.getId());
        return mapToDTO(customer);
    }

    @Override
    public CustomerDTO update(Long id, CustomerDTO dto) {
        log.info("Updating customer ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        
        // Update fields
        customer.setName(dto.getName());
        customer.setContactName(dto.getContactName());
        customer.setPhone(dto.getPhone());
        customer.setEmail(dto.getEmail());
        customer.setTaxCode(dto.getTaxCode());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setPaymentTerms(dto.getPaymentTerms());
        customer.setActive(dto.getActive());
        
        customer = customerRepository.save(customer);
        return mapToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getById(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        return mapToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getByCode(String code) {
        Customer customer = customerRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + code));
        return mapToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAll() {
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getActiveCustomers() {
        return customerRepository.findAllActive().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> search(String query) {
        return customerRepository.search(query).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting customer ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        
        // Soft delete - just mark as inactive
        customer.setActive(false);
        customerRepository.save(customer);
    }

    // Delivery Address operations

    @Override
    public DeliveryAddressDTO addDeliveryAddress(Long customerId, DeliveryAddressDTO dto) {
        log.info("Adding delivery address to customer ID: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        DeliveryAddress address = mapToAddressEntity(dto);
        address.setCustomer(customer);
        
        // If this is the first address or marked as default, set it as default
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            clearDefaultAddresses(customerId);
            address.setIsDefault(true);
        } else if (deliveryAddressRepository.findByCustomerId(customerId).isEmpty()) {
            address.setIsDefault(true);
        }
        
        address = deliveryAddressRepository.save(address);
        return mapToAddressDTO(address);
    }

    @Override
    public DeliveryAddressDTO updateDeliveryAddress(Long addressId, DeliveryAddressDTO dto) {
        log.info("Updating delivery address ID: {}", addressId);
        
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found with ID: " + addressId));
        
        address.setAddressName(dto.getAddressName());
        address.setRecipientName(dto.getRecipientName());
        address.setPhone(dto.getPhone());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setNotes(dto.getNotes());
        
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            clearDefaultAddresses(address.getCustomer().getId());
            address.setIsDefault(true);
        }
        
        address = deliveryAddressRepository.save(address);
        return mapToAddressDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryAddressDTO> getDeliveryAddresses(Long customerId) {
        return deliveryAddressRepository.findByCustomerIdOrdered(customerId).stream()
            .map(this::mapToAddressDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteDeliveryAddress(Long addressId) {
        log.info("Deleting delivery address ID: {}", addressId);
        
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found with ID: " + addressId));
        
        deliveryAddressRepository.delete(address);
    }

    @Override
    public void setDefaultAddress(Long customerId, Long addressId) {
        log.info("Setting default address {} for customer {}", addressId, customerId);
        
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found with ID: " + addressId));
        
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("Address does not belong to the specified customer");
        }
        
        clearDefaultAddresses(customerId);
        address.setIsDefault(true);
        deliveryAddressRepository.save(address);
    }

    // Helper methods

    private void clearDefaultAddresses(Long customerId) {
        List<DeliveryAddress> addresses = deliveryAddressRepository.findByCustomerId(customerId);
        for (DeliveryAddress addr : addresses) {
            addr.setIsDefault(false);
            deliveryAddressRepository.save(addr);
        }
    }

    private Customer mapToEntity(CustomerDTO dto) {
        return Customer.builder()
            .code(dto.getCode())
            .name(dto.getName())
            .contactName(dto.getContactName())
            .phone(dto.getPhone())
            .email(dto.getEmail())
            .taxCode(dto.getTaxCode())
            .creditLimit(dto.getCreditLimit())
            .paymentTerms(dto.getPaymentTerms())
            .active(dto.getActive() != null ? dto.getActive() : true)
            .build();
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = CustomerDTO.builder()
            .id(customer.getId())
            .code(customer.getCode())
            .name(customer.getName())
            .contactName(customer.getContactName())
            .phone(customer.getPhone())
            .email(customer.getEmail())
            .taxCode(customer.getTaxCode())
            .creditLimit(customer.getCreditLimit())
            .currentBalance(customer.getCurrentBalance())
            .paymentTerms(customer.getPaymentTerms())
            .active(customer.getActive())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
        dto.computeFields();
        return dto;
    }

    private DeliveryAddress mapToAddressEntity(DeliveryAddressDTO dto) {
        return DeliveryAddress.builder()
            .addressName(dto.getAddressName())
            .recipientName(dto.getRecipientName())
            .phone(dto.getPhone())
            .addressLine1(dto.getAddressLine1())
            .addressLine2(dto.getAddressLine2())
            .city(dto.getCity())
            .state(dto.getState())
            .postalCode(dto.getPostalCode())
            .country(dto.getCountry() != null ? dto.getCountry() : "Vietnam")
            .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
            .notes(dto.getNotes())
            .build();
    }

    private DeliveryAddressDTO mapToAddressDTO(DeliveryAddress address) {
        DeliveryAddressDTO dto = DeliveryAddressDTO.builder()
            .id(address.getId())
            .customerId(address.getCustomer().getId())
            .customerName(address.getCustomer().getName())
            .addressName(address.getAddressName())
            .recipientName(address.getRecipientName())
            .phone(address.getPhone())
            .addressLine1(address.getAddressLine1())
            .addressLine2(address.getAddressLine2())
            .city(address.getCity())
            .state(address.getState())
            .postalCode(address.getPostalCode())
            .country(address.getCountry())
            .isDefault(address.getIsDefault())
            .notes(address.getNotes())
            .build();
        dto.computeFields();
        return dto;
    }
}
