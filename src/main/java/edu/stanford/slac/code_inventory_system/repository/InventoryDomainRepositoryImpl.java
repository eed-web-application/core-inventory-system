package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;

/**
 * IImplementation for the inventory domain repository
 * customization
 */
@Repository
@AllArgsConstructor
public class InventoryDomainRepositoryImpl implements InventoryDomainRepositoryCustom{
    MongoTemplate mongoTemplate;
    @Override
    public String ensureTag(@NotNull String id, @NonNull Tag newTag) {
        String newID = UUID.randomUUID().toString();
        newTag.setName(
                normalizeStringWithReplace(
                        newTag.getName(),
                        " ",
                        "-"
                )
        );
        newTag.setId(newID);
        Query query = new Query(
                Criteria.where("id").is(id)
                        .and("tags.name").ne(newTag.getName())
        );

        Update update = new Update()
                .addToSet("tags", newTag);

        InventoryDomain lb = mongoTemplate.findAndModify(
                query,
                update,
                InventoryDomain.class
        );
        if(lb==null || lb.getTags()==null) {
            Query queryForTagID = new Query(
                    Criteria.where("id").is(id)
            );
            queryForTagID.fields().include("tags");
            lb = mongoTemplate.findOne(
                    queryForTagID,
                    InventoryDomain.class
            );
            if(lb!=null) {
                newID = lb.getTags().stream().filter(
                                t->t.getName().compareToIgnoreCase(newTag.getName())==0
                        )
                        .findFirst()
                        .map(Tag::getId)
                        .orElse(null);
            }
        } else {
            newID = lb.getTags().stream().filter(
                            t->t.getName().compareToIgnoreCase(newTag.getName())==0
                    )
                    .findFirst()
                    .map(Tag::getId)
                    .orElse(newID);
        }
        return newID;
    }
}
