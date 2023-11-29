package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Domain already exists")
public class InventoryDomainAlreadyExists extends ControllerLogicException {
    @Builder(builderMethodName = "domainAlreadyExistsByName")
    public InventoryDomainAlreadyExists(Integer errorCode, String domainName) {
        super(errorCode,
                String.format("An inventory domain with name '%s' already exists", domainName),
                getAllMethodInCall(2)
        );
    }
}
