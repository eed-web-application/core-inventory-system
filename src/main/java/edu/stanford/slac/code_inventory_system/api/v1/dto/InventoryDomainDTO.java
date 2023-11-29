package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.stanford.slac.code_inventory_system.model.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Describe the full inventory domain information")
public record InventoryDomainDTO(
        @Schema(description = "The unique domain id")
        String id,
        @Schema(description = "the name of the domain")
        String name,
        @Schema(description = "The description of the domain")
        String description,
        @Schema(description = "The list of the tags that can be used for all inventory elements of this domain")
        List<Tag> tags
) {

}
