package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryClassRepository  extends MongoRepository<InventoryClass, String> {
    /**
     * Searches for InventoryClass objects whose name contains the specified search string, ignoring case.
     *
     * @param search the search string to match against the name property
     * @return a List of InventoryClass objects whose name contains the search string
     */
    List<InventoryClass> findAllByNameContainsIgnoreCase(String search);
}