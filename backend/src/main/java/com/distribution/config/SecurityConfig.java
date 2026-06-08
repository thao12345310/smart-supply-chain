package com.distribution.config;

import com.distribution.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for the Distribution Management System
 * 
 * Role-Based Access Control (RBAC):
 * - ADMIN: Full system access
 * - PURCHASE_MANAGER: Manage purchases + approve POs
 * - PURCHASE_STAFF: Create and manage purchase orders
 * - SALES_MANAGER: Manage sales + approve SOs
 * - SALES_STAFF: Create and manage sales orders
 * - WAREHOUSE_STAFF: Manage goods receipt/issue and inventory (cannot see unapproved orders)
 * - DELIVERY_ADMIN: Manage delivery plans and assign shippers
 * - SHIPPER: Handle assigned delivery trips only
 * - ACCOUNTANT: Approve orders (PO/SO) and view financial data
 * 
 * Key Rules:
 * 1. Approval actions require MANAGER, ACCOUNTANT, or ADMIN role
 * 2. Warehouse staff cannot see unapproved orders
 * 3. Shippers only see their assigned delivery trips
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ==================== Public Endpoints ====================
                // Swagger UI and API docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                
                // Health check
                .requestMatchers("/actuator/**").permitAll()
                
                // Authentication endpoints
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                
                // ==================== Admin Only ====================
                .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/roles/**").hasRole("ADMIN")
                
                // ==================== Purchase Order Endpoints ====================
                // View: authenticated users (except warehouse staff for unapproved - handled at service level)
                .requestMatchers(HttpMethod.GET, "/api/purchase-orders/**").authenticated()
                
                // Create/Update: Purchase Staff, Purchase Manager, Admin
                .requestMatchers(HttpMethod.POST, "/api/purchase-orders")
                    .hasAnyRole("PURCHASE_STAFF", "PURCHASE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/**")
                    .hasAnyRole("PURCHASE_STAFF", "PURCHASE_MANAGER", "ADMIN")
                
                // Approve/Reject PO: Purchase Manager, Accountant, Admin
                .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/approve")
                    .hasAnyRole("PURCHASE_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/reject")
                    .hasAnyRole("PURCHASE_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/purchase-orders/*/approval")
                    .hasAnyRole("PURCHASE_MANAGER", "ACCOUNTANT", "ADMIN")
                
                // Delete PO: Purchase Manager, Admin
                .requestMatchers(HttpMethod.DELETE, "/api/purchase-orders/**")
                    .hasAnyRole("PURCHASE_MANAGER", "ADMIN")
                
                // ==================== Sales Order Endpoints ====================
                // View: authenticated users (except warehouse staff for unapproved - handled at service level)
                .requestMatchers(HttpMethod.GET, "/api/sales-orders/**").authenticated()
                
                // Create/Update: Sales Staff, Sales Manager, Admin
                .requestMatchers(HttpMethod.POST, "/api/sales-orders")
                    .hasAnyRole("SALES_STAFF", "SALES_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/sales-orders/**")
                    .hasAnyRole("SALES_STAFF", "SALES_MANAGER", "ADMIN")
                
                // Approve/Reject SO: Sales Manager, Accountant, Admin
                .requestMatchers(HttpMethod.PUT, "/api/sales-orders/*/approve")
                    .hasAnyRole("SALES_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/sales-orders/*/reject")
                    .hasAnyRole("SALES_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/sales-orders/*/approval")
                    .hasAnyRole("SALES_MANAGER", "ACCOUNTANT", "ADMIN")
                
                // Delete SO: Sales Manager, Admin
                .requestMatchers(HttpMethod.DELETE, "/api/sales-orders/**")
                    .hasAnyRole("SALES_MANAGER", "ADMIN")
                
                // ==================== Goods Receipt Endpoints ====================
                // View: Warehouse Staff, Purchase staff/manager, Admin
                .requestMatchers(HttpMethod.GET, "/api/goods-receipts/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "PURCHASE_STAFF", "PURCHASE_MANAGER", "ADMIN")
                
                // Create/Update/Confirm: Warehouse Staff, Admin
                .requestMatchers(HttpMethod.POST, "/api/goods-receipts")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/goods-receipts/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/goods-receipts/*/confirm")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                
                // Delete: Warehouse Staff, Admin
                .requestMatchers(HttpMethod.DELETE, "/api/goods-receipts/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                
                // ==================== Goods Issue Endpoints ====================
                // View: Warehouse Staff, Sales staff/manager, Admin
                .requestMatchers(HttpMethod.GET, "/api/goods-issues/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "SALES_STAFF", "SALES_MANAGER", "ADMIN")
                
                // Create/Update/Confirm: Warehouse Staff, Admin
                .requestMatchers(HttpMethod.POST, "/api/goods-issues")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/goods-issues/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/goods-issues/*/confirm")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                
                // Delete: Warehouse Staff, Admin
                .requestMatchers(HttpMethod.DELETE, "/api/goods-issues/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                
                // ==================== Inventory Endpoints ====================
                // View: all authenticated users
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").authenticated()
                
                // Modify: Warehouse Staff, Admin
                .requestMatchers(HttpMethod.POST, "/api/inventory/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/inventory/**")
                    .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")

                // ==================== Delivery Plan Endpoints ====================
                // View all plans: Delivery Admin, Admin
                .requestMatchers(HttpMethod.GET, "/api/delivery-plans")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN", "SHIPPER")
                .requestMatchers(HttpMethod.GET, "/api/delivery-plans/**")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN", "SHIPPER")
                
                // Create/Update/Delete plans: Delivery Admin, Admin
                .requestMatchers(HttpMethod.POST, "/api/delivery-plans/**")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/delivery-plans/**")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/delivery-plans/**")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN")
                
                // ==================== Delivery Trip Routes (Shipper Access) ====================
                // Shipper can view their assigned trips - handled at service level
                .requestMatchers(HttpMethod.GET, "/api/delivery-trips/**")
                    .hasAnyRole("SHIPPER", "DELIVERY_ADMIN", "ADMIN")
                
                // Shipper can update delivery status on assigned trips
                .requestMatchers(HttpMethod.PUT, "/api/delivery-trips/*/status")
                    .hasAnyRole("SHIPPER", "DELIVERY_ADMIN", "ADMIN")
                
                // Create/Delete trips: Delivery Admin, Admin only
                .requestMatchers(HttpMethod.POST, "/api/delivery-trips/**")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/delivery-trips/**")
                    .hasAnyRole("DELIVERY_ADMIN", "ADMIN")
                
                // ==================== Customer Endpoints ====================
                .requestMatchers(HttpMethod.GET, "/api/customers/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/customers")
                    .hasAnyRole("SALES_STAFF", "SALES_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/customers/**")
                    .hasAnyRole("SALES_STAFF", "SALES_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/customers/**")
                    .hasAnyRole("SALES_MANAGER", "ADMIN")
                
                // ==================== Supplier Endpoints ====================
                .requestMatchers(HttpMethod.GET, "/api/suppliers/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/suppliers")
                    .hasAnyRole("PURCHASE_STAFF", "PURCHASE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/suppliers/**")
                    .hasAnyRole("PURCHASE_STAFF", "PURCHASE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**")
                    .hasAnyRole("PURCHASE_MANAGER", "ADMIN")
                
                // ==================== Product/Warehouse Endpoints ====================
                .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/warehouses/**").authenticated()
                .requestMatchers("/api/products/**").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                .requestMatchers("/api/warehouses/**").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                
                // ==================== Invoice Endpoints ====================
                .requestMatchers(HttpMethod.GET, "/api/sales-invoices/**")
                    .hasAnyRole("SALES_STAFF", "SALES_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/sales-invoices/**")
                    .hasAnyRole("SALES_STAFF", "SALES_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/sales-invoices/**")
                    .hasAnyRole("SALES_MANAGER", "ACCOUNTANT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/sales-invoices/**")
                    .hasAnyRole("ACCOUNTANT", "ADMIN")
                
                // ==================== Default Rule ====================
                .anyRequest().authenticated()
            )
            // Add JWT filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
