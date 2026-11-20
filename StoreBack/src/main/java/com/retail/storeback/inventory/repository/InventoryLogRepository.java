package com.retail.storeback.inventory.repository;

import com.retail.storeback.inventory.model.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, UUID> {

    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(UUID productId);

    List<InventoryLog> findByReferenceId(UUID referenceId);
}
