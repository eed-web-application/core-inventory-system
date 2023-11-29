package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Is the new element to add to the inventory")
public record NewInventoryElementDTO(
        @Schema(description = "Is the name of the element")
        String name,
        @Schema(description = "Specify the domain which the item belong")
        String domainId,
        @Schema(description = "Is the {@link InventoryClassDTO#id} of one of the existing class")
        String classId,
        @Schema(description = "The values for the element attributes(will be checked against those defined in the class)")
        List<InventoryElementAttributeValue> attributes
) {
}
