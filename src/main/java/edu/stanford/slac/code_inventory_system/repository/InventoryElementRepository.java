package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryElementRepository extends MongoRepository<InventoryElement, String> {
    /**
     * Check if a tag is used by elements of a specific domainId
     * @param domainId the domain id
     * @param tagId the tag id to find
     * @return true if the tag is used
     */
    boolean existsByDomainIdIsAndTagsContains(String domainId, String tagId);
}