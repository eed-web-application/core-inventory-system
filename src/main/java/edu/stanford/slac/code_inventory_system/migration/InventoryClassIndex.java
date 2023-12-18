package edu.stanford.slac.code_inventory_system.migration;


import edu.stanford.slac.ad.eed.base_mongodb_lib.utility.MongoDDLOps;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@AllArgsConstructor
@ChangeUnit(id = "inventory-class-index", order = "1001", author = "bisegni")
public class InventoryClassIndex {
    private final MongoTemplate mongoTemplate;

    @Execution
    public void changeSet() {
        ensureIndex();
    }


    @RollbackExecution
    public void rollback() {

    }

    /**
     * Ensure base index
     */
    private void ensureIndex() {
        // class name
        MongoDDLOps.createIndex(
                InventoryClass.class,
                mongoTemplate,
                new Index().on(
                                "name",
                                Sort.Direction.ASC
                        )
                        .named("name")
                        .sparse()
        );
        // class extends id list
        MongoDDLOps.createIndex(
                InventoryClass.class,
                mongoTemplate,
                new Index().on(
                                "extendsClass",
                                Sort.Direction.ASC
                        )
                        .named("extendsClass")
                        .sparse()
        );
        // child class id
        MongoDDLOps.createIndex(
                InventoryClass.class,
                mongoTemplate,
                new Index().on(
                                "childClass",
                                Sort.Direction.ASC
                        )
                        .named("permittedChildClass")
                        .sparse()
        );
    }
}
