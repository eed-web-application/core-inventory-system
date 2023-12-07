package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryClassDTO(
        @Schema(description = "The unique identifier of the inventory class")
        String id,

        @Schema(description = "The name of the inventory class")
        String name,

        @Schema(description = "The description of the inventory class")
        String description,

        @Schema(description = "The type to which the inventory class belongs")
        InventoryClassTypeDTO type,

        @Schema(description = "The list of attributes that can be used to specialize the inventory class")
        List<InventoryClassAttributeDTO> attributes
) {
}
