package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Parent domain mismatch")
public class InventoryDomainParentElementMismatch extends ControllerLogicException {
    @Builder(builderMethodName = "domainMismatch")
    public InventoryDomainParentElementMismatch(Integer errorCode, String parentElement, String actualDomain) {
        super(errorCode,
                String.format("The inventory domain of the parent '%s' mismatch with the inventory id %s", parentElement, actualDomain),
                getAllMethodInCall(2)
        );
    }
}
