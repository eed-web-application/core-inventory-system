package edu.stanford.slac.code_inventory_system.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.model.Tag;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.code_inventory_system.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Tag in use")
public class TagInUse extends ControllerLogicException {
    @Builder(builderMethodName = "tagInUse")
    public TagInUse(Integer errorCode, Tag tag) {
        super(errorCode,
                String.format("The tag '%s' with id '%s' is in use", tag.getName(), tag.getId()),
                getAllMethodInCall()
        );
    }
}
