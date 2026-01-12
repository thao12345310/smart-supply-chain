package com.distribution.repository;

import com.distribution.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
    
    /**
     * Find roles by name in set
     */
    Set<Role> findByNameIn(Set<String> names);
    
    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);
}
