package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.QueryParameter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@AllArgsConstructor
public class InventoryElementRepositoryImpl implements InventoryElementRepositoryCustom {
    MongoTemplate mongoTemplate;

    @Override
    public List<InventoryElement> searchAll(QueryParameter queryParameter) {
        if (queryParameter.getContextSize() != null && queryParameter.getAnchorID() == null) {
            throw ControllerLogicException.of(
                    -1,
                    "The context count cannot be used without the ancor",
                    "InventoryElementRepositoryImpl::searchAll"
            );
        }

        // all the criteria
        List<Criteria> allCriteria = new ArrayList<>();
        List<InventoryElement> elementsAfterAnchor = new ArrayList<>();
        List<InventoryElement> elementsBeforeAnchor = new ArrayList<>();

        // check for the tags
        if (!queryParameter.getTags().isEmpty()) {
            allCriteria.add(
                    queryParameter.getRequireAllTags() ?
                            Criteria.where("tags").all(
                                    queryParameter.getTags()
                            ) : Criteria.where("tags").in(
                            queryParameter.getTags()
                    )
            );
        }

        if (
                queryParameter.getContextSize() != null
                        && queryParameter.getContextSize() > 0
        ) {

            List<Criteria> localAllCriteria = allCriteria;
            localAllCriteria.add(
                    Criteria.where("id").is(queryParameter.getAnchorID())
            );
            // at this point the anchor id is nto n
            Query query = getQuery(queryParameter);
            query.addCriteria(
                    // all general criteria
                    new Criteria().andOperator(localAllCriteria)
            ).with(
                    Sort.by(
                            Sort.Direction.ASC, "fullTreePath")
            ).limit(queryParameter.getContextSize());
            elementsBeforeAnchor.addAll(
                    mongoTemplate.find(
                            query,
                            InventoryElement.class
                    )
            );
            // reverse the order
            Collections.reverse(elementsBeforeAnchor);
        }

        if (queryParameter.getLimit() != null && queryParameter.getLimit() > 0) {
            List<Criteria> localAllCriteria = allCriteria;
            Query query = getQuery(queryParameter);
            if (queryParameter.getAnchorID() != null) {
                localAllCriteria.add(
                        Criteria.where("id").is(queryParameter.getAnchorID())
                );
            }
            query.addCriteria(new Criteria().andOperator(
                            localAllCriteria
                    )
            ).with(
                    Sort.by(
                            Sort.Direction.DESC, "fullTreePath")
            ).limit(queryParameter.getLimit());
            elementsAfterAnchor.addAll(
                    mongoTemplate.find(
                            query,
                            InventoryElement.class
                    )
            );
        }

        elementsBeforeAnchor.addAll(elementsAfterAnchor);
        return elementsBeforeAnchor;
    }

    /**
     * Get the default query
     * @param queryParameter is the query parameter class
     * @return return the mongodb query
     */
    private static Query getQuery(QueryParameter queryParameter) {
        Query query;
        if (queryParameter.getSearch() != null && !queryParameter.getSearch().isEmpty()) {
            //{$text: {$search:'log' }}
            query = TextQuery.queryText(TextCriteria.forDefaultLanguage()
                    .matchingAny(queryParameter.getSearch().split(" "))
            );
        } else {
            query = new Query();
        }
        return query;
    }
}
