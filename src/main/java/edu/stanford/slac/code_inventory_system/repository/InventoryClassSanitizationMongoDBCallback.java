package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;

@Log4j2
@Component
@AllArgsConstructor
public class InventoryClassSanitizationMongoDBCallback implements BeforeConvertCallback<InventoryClass> {
    @Override
    @NonNull
    public InventoryClass onBeforeConvert(@NonNull InventoryClass inventoryClass, @NonNull String collection) {
        // normalize the class name
        inventoryClass.setName(
                normalizeStringWithReplace(
                        inventoryClass.getName(),
                        " ",
                        "-"
                )
        );
        // normalize the attribute name
        inventoryClass.getAttributes()
                .forEach(
                        attr -> attr.setName(
                                normalizeStringWithReplace(
                                        attr.getName(),
                                        " ",
                                        "-"
                                )
                        )
                );
        log.trace(
                "Normalize inventory class: {}",
                inventoryClass
        );
        return inventoryClass;
    }
}
