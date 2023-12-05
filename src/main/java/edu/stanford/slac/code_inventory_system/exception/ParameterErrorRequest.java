package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error with request paramter")
public class ParameterErrorRequest extends ControllerLogicException {
    @Builder(builderMethodName = "parameterErrorRequest")
    public ParameterErrorRequest(Integer errorCode, String errors) {
        super(
                errorCode,
                errors,
                "Request bing err"
        );
    }
}