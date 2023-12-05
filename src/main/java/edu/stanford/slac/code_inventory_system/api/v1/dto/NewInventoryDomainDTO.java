package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "DTO used for create a new inventory domain")
public record NewInventoryDomainDTO(
        @Schema(description = "The name of the new domain")
        @Pattern(regexp = "[a-z\\-]*",
                message = "Name must not contain numbers and need to contain only lowercase and dash'-' character")
        @NotBlank(message = "Name is mandatory field")
        String name,
        @Schema(description = "The description of the new domain")
        @NotBlank
        String description
) {
}
