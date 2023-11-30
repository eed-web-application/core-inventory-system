package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Class has not been found")
public class InventoryElementAttributeNotForClass extends ControllerLogicException {
    @Builder(builderMethodName = "ieaNotForClassName")
    public InventoryElementAttributeNotForClass(Integer errorCode, String attributeName, String className) {
        super(errorCode,
                String.format("The inventory attribute '%s' cannot be assigned because not in class '%s'", attributeName, className),
                getAllMethodInCall()
        );
    }
}
