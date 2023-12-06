package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthenticationTokenDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "DTO used for create a new inventory domain")
public record NewInventoryDomainDTO(
        @Schema(description = "The name of the new domain")
        @Pattern(regexp = "[a-z0-9\\-]*",
                message = "Name must contain numbers and character and need to contain only lowercase and dash'-' character")
        @NotBlank(message = "Name is mandatory field")
        String name,
        @Schema(description = "The description of the new domain")
        @NotBlank
        String description,
        @NotNull(message = "At least an empty list is mandatory")
        @Schema(description = "The list of the tags that can be used for all inventory elements of this domain")
        List<TagDTO> tags,
        @NotNull(message = "At least an empty list is mandatory")
        @Schema(description = "The list of the authorized user, group or authentication tokens")
        List<AuthorizationDTO> authorizations,
        @NotNull(message = "At least an empty list is mandatory")
        @Schema(description = "The list of the authentication tokens")
        List<AuthenticationTokenDTO> authenticationTokens
) {
}
