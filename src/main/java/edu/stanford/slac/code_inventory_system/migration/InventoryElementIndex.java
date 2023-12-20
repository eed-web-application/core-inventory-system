package edu.stanford.slac.code_inventory_system.migration;


import edu.stanford.slac.ad.eed.base_mongodb_lib.utility.MongoDDLOps;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@AllArgsConstructor
@ChangeUnit(id = "inventory-element-index", order = "1003", author = "bisegni")
public class InventoryElementIndex {
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
                InventoryElement.class,
                mongoTemplate,
                new Index().on(
                                "name",
                                Sort.Direction.ASC
                        )
                        .named("name")
                        .sparse()
        );

        MongoDDLOps.createIndex(
                InventoryElement.class,
                mongoTemplate,
                new Index().on(
                                "domainId",
                                Sort.Direction.ASC
                        )
                        .on(
                                "name",
                                Sort.Direction.ASC
                        )
                        .unique()
                        .named("domain-id-name-unique")
                        .sparse()
        );

        MongoDDLOps.createIndex(
                InventoryElement.class,
                mongoTemplate,
                new Index().on(
                                "classId",
                                Sort.Direction.ASC
                        )
                        .named("classId")
                        .sparse()
        );

        MongoDDLOps.createIndex(
                InventoryElement.class,
                mongoTemplate,
                new Index().on(
                                "tags.id",
                                Sort.Direction.ASC
                        )
                        .named("tags-id")
                        .sparse()
        );

        MongoDDLOps.createIndex(
                InventoryElement.class,
                mongoTemplate,
                new Index().on(
                                "createdDate",
                                Sort.Direction.DESC
                        )
                        .named("createdDate")
                        .sparse()
        );

        MongoDDLOps.createIndex(
                InventoryElement.class,
                mongoTemplate,
                new Index().on(
                                "lastModifiedDate",
                                Sort.Direction.DESC
                        )
                        .named("lastModifiedDate")
                        .sparse()
        );
    }
}
