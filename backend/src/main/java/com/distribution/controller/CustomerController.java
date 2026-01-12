package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.CustomerDTO;
import com.distribution.dto.DeliveryAddressDTO;
import com.distribution.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer operations
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Customer", description = "Customer Management APIs")
public class CustomerController {

    private final CustomerService customerService;

    // ==================== CRUD Operations ====================

    @GetMapping
    @Operation(summary = "Get all Customers", description = "Retrieve all customers")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAll() {
        List<CustomerDTO> customers = customerService.getAll();
        return ResponseEntity.ok(ApiResponse.success(customers, "Retrieved " + customers.size() + " customers"));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active Customers", description = "Retrieve all active customers")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getActiveCustomers() {
        List<CustomerDTO> customers = customerService.getActiveCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers, "Retrieved " + customers.size() + " active customers"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Customer by ID", description = "Retrieve a specific customer")
    public ResponseEntity<ApiResponse<CustomerDTO>> getById(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        CustomerDTO customer = customerService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Customer by Code", description = "Retrieve a customer by its code")
    public ResponseEntity<ApiResponse<CustomerDTO>> getByCode(
            @Parameter(description = "Customer Code") @PathVariable String code) {
        CustomerDTO customer = customerService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Customers", description = "Search customers by name, code, or email")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> search(
            @Parameter(description = "Search query") @RequestParam String q) {
        List<CustomerDTO> customers = customerService.search(q);
        return ResponseEntity.ok(ApiResponse.success(customers, "Found " + customers.size() + " customers"));
    }

    @PostMapping
    @Operation(summary = "Create Customer", description = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerDTO>> create(
            @Valid @RequestBody CustomerDTO dto) {
        CustomerDTO created = customerService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Customer created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Customer", description = "Update an existing customer")
    public ResponseEntity<ApiResponse<CustomerDTO>> update(
            @Parameter(description = "Customer ID") @PathVariable Long id,
            @Valid @RequestBody CustomerDTO dto) {
        CustomerDTO updated = customerService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Customer", description = "Deactivate a customer (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deactivated successfully"));
    }

    // ==================== Delivery Address Operations ====================

    @GetMapping("/{customerId}/addresses")
    @Operation(summary = "Get Delivery Addresses", description = "Get all delivery addresses for a customer")
    public ResponseEntity<ApiResponse<List<DeliveryAddressDTO>>> getDeliveryAddresses(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        List<DeliveryAddressDTO> addresses = customerService.getDeliveryAddresses(customerId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping("/{customerId}/addresses")
    @Operation(summary = "Add Delivery Address", description = "Add a new delivery address to a customer")
    public ResponseEntity<ApiResponse<DeliveryAddressDTO>> addDeliveryAddress(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Valid @RequestBody DeliveryAddressDTO dto) {
        DeliveryAddressDTO created = customerService.addDeliveryAddress(customerId, dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Delivery address added successfully"));
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update Delivery Address", description = "Update an existing delivery address")
    public ResponseEntity<ApiResponse<DeliveryAddressDTO>> updateDeliveryAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody DeliveryAddressDTO dto) {
        DeliveryAddressDTO updated = customerService.updateDeliveryAddress(addressId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Delivery address updated successfully"));
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete Delivery Address", description = "Delete a delivery address")
    public ResponseEntity<ApiResponse<Void>> deleteDeliveryAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        customerService.deleteDeliveryAddress(addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Delivery address deleted successfully"));
    }

    @PutMapping("/{customerId}/addresses/{addressId}/default")
    @Operation(summary = "Set Default Address", description = "Set a delivery address as the default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        customerService.setDefaultAddress(customerId, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Default address updated successfully"));
    }
}
