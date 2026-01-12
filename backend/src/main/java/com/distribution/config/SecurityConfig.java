package com.distribution.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for the Distribution Management System
 * 
 * Role-Based Access Control:
 * - ROLE_ADMIN: Full access to all operations
 * - ROLE_PURCHASING_STAFF: Create/Update/View Purchase Orders
 * - ROLE_PURCHASING_MANAGER: All Purchasing Staff permissions + Approve/Reject POs
 * - ROLE_ACCOUNTANT: Approve/Reject POs + View accounting data
 * - ROLE_WAREHOUSE_STAFF: Create/Confirm Goods Receipts, View Inventory
 * 
 * Note: Currently configured for development with permitAll.
 * Uncomment the role-based rules for production.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger UI endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                
                // Health check
                .requestMatchers("/actuator/**").permitAll()
                
                // ========== Development Mode: Allow All ==========
                // Comment these out and uncomment the role-based rules below for production
                .requestMatchers("/api/**").permitAll()
                
                // ========== Production Mode: Role-Based Access ==========
                // Uncomment these for production use with JWT authentication
                
                // Purchase Order endpoints
                // .requestMatchers(HttpMethod.GET, "/api/purchase-orders/**").authenticated()
                // .requestMatchers(HttpMethod.POST, "/api/purchase-orders").hasAnyRole("PURCHASING_STAFF", "PURCHASING_MANAGER", "ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/approve").hasAnyRole("PURCHASING_MANAGER", "ACCOUNTANT", "ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/reject").hasAnyRole("PURCHASING_MANAGER", "ACCOUNTANT", "ADMIN")
                // .requestMatchers(HttpMethod.POST, "/api/purchase-orders/*/approval").hasAnyRole("PURCHASING_MANAGER", "ACCOUNTANT", "ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/**").hasAnyRole("PURCHASING_STAFF", "PURCHASING_MANAGER", "ADMIN")
                // .requestMatchers(HttpMethod.DELETE, "/api/purchase-orders/**").hasAnyRole("PURCHASING_MANAGER", "ADMIN")
                
                // Goods Receipt endpoints
                // .requestMatchers(HttpMethod.GET, "/api/goods-receipts/**").authenticated()
                // .requestMatchers(HttpMethod.POST, "/api/goods-receipts").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/api/goods-receipts/*/confirm").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/api/goods-receipts/**").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                // .requestMatchers(HttpMethod.DELETE, "/api/goods-receipts/**").hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
                
                // Inventory endpoints - read-only for most users
                // .requestMatchers(HttpMethod.GET, "/api/inventory/**").authenticated()
                
                // Other endpoints
                // .requestMatchers("/api/suppliers/**").hasAnyRole("PURCHASING_STAFF", "PURCHASING_MANAGER", "ADMIN")
                // .requestMatchers("/api/products/**").authenticated()
                // .requestMatchers("/api/warehouses/**").authenticated()
                
                .anyRequest().permitAll()
            );
            // Add JWT filter for production
            // .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
