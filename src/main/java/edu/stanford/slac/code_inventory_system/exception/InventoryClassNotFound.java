package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Class has not been found")
public class InventoryClassNotFound extends ControllerLogicException {
    @Builder(builderMethodName = "classNotFoundById")
    public InventoryClassNotFound(Integer errorCode, String id) {
        super(errorCode,
                String.format("The inventory class with id '%s' has not been found", id),
                getAllMethodInCall()
        );
    }
}
