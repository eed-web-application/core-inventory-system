package edu.stanford.slac.code_inventory_system.service.utility;

import java.util.List;
import java.util.UUID;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;
import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;

public class IdValueObjectUtil {
    /**
     * This method try to manage a list of the updated resource with a
     * list that represent the actual resources. It creates or
     * delete resource where needed
     *
     * @param updatedList the update to be applied
     * @param actualList  the actual resources
     */
    static public List<? extends IdNameInterface> updateResource(
            List<? extends IdNameInterface> updatedList,
            List<? extends IdNameInterface> actualList,
            CheckIdInUse checkIdInUse,
            ExceptionForNotFoundResource exceptionForNotFoundResource,
            ExceptionForResourceInUse exceptionForResourceInUse) {
        //normalize tag
        updatedList.forEach(
                t -> t.setName(
                        normalizeStringWithReplace(
                                t.getName(),
                                " ",
                                "-"
                        )
                )
        );

        for (IdNameInterface updateTag :
                updatedList) {
            if (updateTag.getId() == null) {
                // generate new ID
                updateTag.setId(UUID.randomUUID().toString());
                continue;
            }
            // if we have an id it should be found or is a wrong id
            assertion(
                    () -> actualList.stream().anyMatch(
                            s -> s.getId().compareTo(
                                    updateTag.getId()
                            ) == 0
                    ),
                    exceptionForNotFoundResource.getException(updateTag)
            );
        }

        // check which tag should be removed
        for (IdNameInterface storedTag :
                actualList) {
            boolean willBeUpdated = updatedList.stream().anyMatch(
                    ut -> ut.getId() != null && ut.getId().compareTo(storedTag.getId()) == 0
            );
            if (willBeUpdated) continue;

            assertion(
                    () -> !checkIdInUse.inUse(storedTag.getId()),
                    exceptionForResourceInUse.getException(storedTag)
            );
        }

        // return the all managed resoruces
        return updatedList;
    }
}
