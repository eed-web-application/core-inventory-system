package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClassType;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface InventoryClassRepository  extends MongoRepository<InventoryClass, String> {
    /**
     * Return all the classes tha belong to one or more type in input
     * @param classTypes a list of the requested class types
     * @return the list of found class
     */
    List<InventoryClass> findAllByTypeIn(List<InventoryClassType> classTypes);

    /**
     * Return the list of the class type values from all the stored classes
     * @return the list of all class type stored
     */
    @Aggregation(pipeline = {
            "{ $group: { _id: '$type' } }",
            "{ $project: { _id: 0, type: '$_id' } }"
    })
    List<InventoryClassType> findDistinctTypes();
}