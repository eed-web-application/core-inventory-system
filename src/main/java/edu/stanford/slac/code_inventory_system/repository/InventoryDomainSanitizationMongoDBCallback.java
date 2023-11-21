package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.ad.eed.baselib.utility.StringUtilities;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;

@Log4j2
@Component
@AllArgsConstructor
public class InventoryDomainSanitizationMongoDBCallback implements BeforeConvertCallback<InventoryDomain> {
    @Override
    @NonNull
    public InventoryDomain onBeforeConvert(@NonNull InventoryDomain domain, @NonNull String collection) {
        domain.setName(
                normalizeStringWithReplace(
                        domain.getName(),
                        " ",
                        "-"
                )
        );
        return domain;
    }
}
