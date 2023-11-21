package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryElementRepository extends MongoRepository<InventoryElement, String> {
}