package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryElementRepository extends MongoRepository<InventoryElement, String>, InventoryElementRepositoryCustom {
    /**
     * Check if a tag is used by any elements of a specific domainId
     * @param domainId the domain id
     * @param tagId the tag id to find
     * @return true if the tag is used
     */
    boolean existsByDomainIdIsAndTagsContains(String domainId, String tagId);

    /**
     * return all the children for a specific element id
     * @param domainId the domain id of the element
     * @param elementId the root element id
     * @return all the children for the root element
     */
    List<InventoryElement> findAllByDomainIdIsAndParentIdIs(String domainId, String elementId);

    /**
     * Retrieves a list of InventoryElements that satisfy the given domain ID, parent ID, and class ID
     *
     * @param domainId   the domain ID
     * @param elementId   the parent ID
     * @param classId    the list of class IDs
     * @return a list of InventoryElements
     */
    List<InventoryElement> findAllByDomainIdIsAndParentIdIsAndClassIdIn(String domainId, String elementId, List<String> classId);
}