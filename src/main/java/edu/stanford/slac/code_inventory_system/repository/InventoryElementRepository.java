package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import org.springframework.data.mongodb.repository.Aggregation;
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
     * Retrieves a list of InventoryElements that are root of the domain
     *
     * @param domainId   the domain ID
     * @return a list of InventoryElements that are roots for the domain
     */
    List<InventoryElement> findAllByDomainIdIsAndParentIdIsNull(String domainId);

    /**
     * Retrieves a list of InventoryElements that satisfy the given domain ID, parent ID, and class ID
     *
     * @param domainId   the domain ID
     * @param elementId   the parent ID
     * @param classId    the list of class IDs
     * @return a list of InventoryElements
     */
    List<InventoryElement> findAllByDomainIdIsAndParentIdIsAndClassIdIn(String domainId, String elementId, List<String> classId);

    // Upward Path Aggregation
    @Aggregation(pipeline = {
            "{ $match: { 'id': ?1, 'domainId': ?0 } }",
            "{ $graphLookup: { from: 'inventoryElement', startWith: '$parentId', connectFromField: 'parentId', connectToField: '_id', as: 'pathToRoot', depthField: 'depth' } }",
            "{ $unwind: '$pathToRoot' }",
            "{ $project: { 'pathToRoot': 1, '_id': '0' } }",
            "{ $replaceRoot: { newRoot: '$pathToRoot' } }",
            "{ $project: { 'attributes': 0, 'connectorClasses': 0, maintenanceHistory: 0 } }",
            "{ $sort: { 'depth': 1 } }" // Sort the results by depth in ascending order
    })
    List<InventoryElement> findPathToRoot(String domainId, String startingElementId);

    // Downward Path Aggregation to get _id list
    @Aggregation(pipeline = {
            "{ $match: { 'id': ?1, 'domainId': ?0 } }",
            "{ $graphLookup: { from: 'inventoryElement', startWith: '$_id', connectFromField: '_id', connectToField: 'parentId', as: 'pathToLeaf', depthField: 'depth' } }",
            "{ $unwind: '$pathToLeaf' }",
            "{ $project: { 'pathToLeaf': 1, '_id': '0' } }",
            "{ $replaceRoot: { newRoot: '$pathToLeaf' } }",
            "{ $project: { 'attributes': 0, 'connectorClasses': 0, maintenanceHistory: 0 } }",
            "{ $sort: { 'depth': 1, 'name': 1 } }" // Sort the results by depth and name in descending order
    })
    List<InventoryElement> findIdPathToLeaf(String domainId, String startingElementId);
}