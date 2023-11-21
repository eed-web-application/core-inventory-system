package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryClassRepository  extends MongoRepository<InventoryClass, String> {
}