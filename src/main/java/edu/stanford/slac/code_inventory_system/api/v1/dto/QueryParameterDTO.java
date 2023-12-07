package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "The query parameter")
public record QueryParameterDTO(
        @Schema(description = "Is the domain ids to select to show all the enclosed element")
        List<String> domainId,
        @Schema(description = "Is the id to point to as starting point in the search")
        String anchorID,
        @Schema(description = "Include this number of element before the anchor")
        Integer contextSize,
        @Schema(description = "Limit the number of element after the anchor.")
        Integer limit,
        @Schema(description = "Typical search functionality.")
        String search,
        @Schema(description = "Only include elements that use one of these tags.")
        List<String> tags,
        @Schema(description = "Requires that all the found elements contains all the tags")
        Boolean requireAllTags
        ) {}
