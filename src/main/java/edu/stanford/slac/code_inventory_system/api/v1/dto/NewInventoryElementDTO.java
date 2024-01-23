package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.stanford.slac.code_inventory_system.api.v1.validator.NullOrRegex;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

import java.util.List;
@Validated
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Is the new element to add to the inventory")
public record NewInventoryElementDTO(
        @NotNull
        @Schema(description = "Is the name of the element")
        String name,
        @NotNull
        @Schema(description = "Is the {@link InventoryClassDTO#id} of one of the existing class")
        String classId,
        @NullOrRegex(regexp = "[0-9a-zA-Z\\-]+", message = "parentID should null or an alphanumeric value")
        @Schema(description = "Is the {@link NewInventoryElementDTO#id} of one of the existing element use as parent")
        String parentId,
//        @NotNull
        @Schema(description = "The description for the specific element of a class type")
        String description,
        @Schema(description = "The list of tag that describe the element")
        List<String> tags,
        @Schema(description = "The values for the element attributes(will be checked against those defined in the class)")
        List<InventoryElementAttributeValueDTO> attributes){}