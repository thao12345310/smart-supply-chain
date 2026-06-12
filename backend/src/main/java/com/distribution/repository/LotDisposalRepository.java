package com.distribution.repository;

import com.distribution.model.LotDisposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotDisposalRepository extends JpaRepository<LotDisposal, Long> {

    List<LotDisposal> findAllByOrderByDisposedAtDesc();

    List<LotDisposal> findByWarehouseIdOrderByDisposedAtDesc(Long warehouseId);
}
