package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthenticationTokenDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Describe the full inventory domain information")
public record UpdateDomainDTO(
        @NotEmpty
        @Schema(description = "the name of the domain")
        String name,
        @NotEmpty
        @Schema(description = "The description of the domain")
        String description,
        @NotNull
        @Schema(description = "The list of the tags that can be used for all inventory elements of this domain")
        List<TagDTO> tags,
        @NotNull
        @Schema(description = "The list of the authorized user, group or authentication tokens")
        List<AuthorizationDTO> authorizations,
        @Schema(description = "The list of the authentication tokens")
        List<AuthenticationTokenDTO> authenticationTokens
) {

}
