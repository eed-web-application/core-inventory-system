package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.Tag;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryDomainRepository extends MongoRepository<InventoryDomain, String>, InventoryDomainRepositoryCustom{

    /**
     * Return the single tag from the list of tags that belong to a domain
     * @param id the unique id of the domain
     * @param tagId i hte gat id to return
     * @return the full tag object found
     */
    @Aggregation(pipeline = {
            "{ $match: { 'id': ?0 } }",
            "{ $unwind: '$tags' }",
            "{ $match: { 'tags._id': ?1 } }",
            "{ $project: { 'tags': 1, '_id': 0 } }",
            "{ $replaceRoot: { newRoot: '$tags' } }",
    })
    Optional<Tag> findTagById(String id, String tagId);

    /**
     * check if the tag exist into the domain
     * @param id the domain id
     * @param tagId the id of the tag
     * @return true if the tag exists
     */
    @Aggregation(pipeline = {
            "{ $match: { 'id': ?0 } }",
            "{ $project: { 'tagExists': { $anyElementTrue: { $map: { input: '$tags', as: 'tag', in: { $eq: ['$$tag._id', ?1] } } } }, '_id': 0 } }"
    })
    boolean existsTagById(String id, String tagId);

    /**
     * Check if exists a domain with a specific name
     * @param domainName the domain name
     * @return true if a domain with that name has been found
     */
    boolean existsByNameIs(String domainName);

    @Query(value = "{ 'id':?0, 'tags._id': { $all: ?1 } }", exists = true)
    boolean existsByIdAndAllTags(String id, List<String> tags);
}