package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.model.Tag;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Tag has not been found")
public class TagNotFound extends ControllerLogicException {
    @Builder(builderMethodName = "tagNotFound", builderClassName = "TagNotFoundBuilder")
    public TagNotFound(Integer errorCode, Tag tag) {
        super(errorCode,
                String.format("The tag '%s' with id '%s' has not been found", tag.getName(), tag.getId()),
                getAllMethodInCall(2)
        );
    }

    @Builder(builderMethodName = "tagNotFoundAny", builderClassName = "TagNotFoundAnyBuilder")
    public TagNotFound(Integer errorCode) {
        super(errorCode,
                "One or more tag has not been found",
                getAllMethodInCall(2)
        );
    }
}
