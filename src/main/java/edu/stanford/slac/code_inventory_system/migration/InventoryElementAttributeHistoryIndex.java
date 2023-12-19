package edu.stanford.slac.code_inventory_system.migration;


import edu.stanford.slac.ad.eed.base_mongodb_lib.utility.MongoDDLOps;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.InventoryElementAttributeHistory;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@AllArgsConstructor
@ChangeUnit(id = "inventory-element-attribute-history-index", order = "1004", author = "bisegni")
public class InventoryElementAttributeHistoryIndex {
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
                InventoryElementAttributeHistory.class,
                mongoTemplate,
                new Index().on(
                                "value.name",
                                Sort.Direction.ASC
                        )
                        .named("ValueName")
                        .sparse()
        );

        MongoDDLOps.createIndex(
                InventoryElementAttributeHistory.class,
                mongoTemplate,
                new Index().on(
                                "inventoryElementId",
                                Sort.Direction.ASC
                        )
                        .named("inventoryElementId")
                        .sparse()
        );
        MongoDDLOps.createIndex(
                InventoryElementAttributeHistory.class,
                mongoTemplate,
                new Index().on(
                                "createdDate",
                                Sort.Direction.DESC
                        )
                        .named("createdDate")
                        .sparse()
        );
    }
}
