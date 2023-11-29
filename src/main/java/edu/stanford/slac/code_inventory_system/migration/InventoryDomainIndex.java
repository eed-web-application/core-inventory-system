package edu.stanford.slac.code_inventory_system.migration;


import edu.stanford.slac.ad.eed.base_mongodb_lib.utility.MongoDDLOps;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@AllArgsConstructor
@ChangeUnit(id = "inventory-domain-index", order = "1000", author = "bisegni")
public class InventoryDomainIndex {
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
        //entry index
        MongoDDLOps.createIndex(
                InventoryDomain.class,
                mongoTemplate,
                new Index().on(
                                "name",
                                Sort.Direction.ASC
                        )
                        .named("name")
                        .unique()
        );

        MongoDDLOps.createIndex(
                InventoryDomain.class,
                mongoTemplate,
                new Index().on(
                                "tags.id",
                                Sort.Direction.ASC
                        )
                        .named("tags-id")
                        .unique()
        );
    }
}
