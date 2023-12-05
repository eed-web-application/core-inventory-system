package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Element has not been found")
public class AuthorizationNotFound extends ControllerLogicException {
    @Builder(builderMethodName = "authorizationNotFound")
    public AuthorizationNotFound(Integer errorCode, String authId) {
        super(errorCode,
                String.format("The authorization with id '%s' has not been found", authId),
                getAllMethodInCall()
        );
    }
}
