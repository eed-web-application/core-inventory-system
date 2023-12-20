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
public record UpdateInventoryElementDTO(
        @Schema(description = "The description for the specific element of a class type")
        String description,
        @Schema(description = "The list of tag id that describe the element")
        List<String> tags,
        @Schema(description = "The values for the element attributes(will be checked against those defined in the class)")
        List<InventoryElementAttributeValueDTO> attributes){}