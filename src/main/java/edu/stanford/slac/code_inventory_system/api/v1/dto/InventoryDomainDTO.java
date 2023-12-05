package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
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
        List<TagDTO> tags,
        @Schema(description = "The list of all the authorization for the domain")
        List<AuthorizationDTO> authorizations,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @Schema(description = "The creation time")
        LocalDateTime createdDate,
        @Schema(description = "The user that creates the element")
        String createdBy,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @Schema(description = "The modification time")
        LocalDateTime lastModifiedDate,
        @Schema(description = "The user that modify the element")
        String lastModifiedBy
) {

}
