package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryElementAttributeHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryElementAttributeHistoryRepository extends MongoRepository<InventoryElementAttributeHistory, String> {
    List<InventoryElementAttributeHistory> findAllByInventoryDomainIdIsAndInventoryElementIdIs(String inventoryDomainId, String inventoryElementId);
}