package edu.stanford.slac.code_inventory_system.service.utility;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;

@FunctionalInterface
public interface ExceptionForNotFoundResource {
    ControllerLogicException getException(IdNameInterface notFoundResource);
}
