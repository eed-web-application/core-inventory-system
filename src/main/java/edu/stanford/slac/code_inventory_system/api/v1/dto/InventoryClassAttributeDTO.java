package edu.stanford.slac.code_inventory_system.api.v1.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 *
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Define an attribute to enrich the class attribute list")
public record InventoryClassAttributeDTO(
        @Schema(description = "The name of the attribute")
        String name,

        @Schema(description = "The description of what the attribute is used for")
        String description,

        @Schema(description = "Defines if the attribute is mandatory or not")
        Boolean mandatory,

        @Schema(description = "The type of the attribute, as defined in the AttributeTypeDTO enum")
        InventoryClassAttributeTypeDTO type,

        @Schema(description = "Specifies the type of the unit that represents the attribute")
        String unit
) {
}