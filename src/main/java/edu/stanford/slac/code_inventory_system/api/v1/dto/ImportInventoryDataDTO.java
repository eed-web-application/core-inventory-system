package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Define an list of classes domina and element for import a full structure")
public class ImportInventoryDataDTO {
    @JsonProperty("class-list")
    List<NewInventoryClassDTO> classList;
    @JsonProperty("domain-list")
    List<NewInventoryDomainDTO> domainList;
    @JsonProperty("element-list")
    List<InventoryElementWithDomain> elementList;

    @Data
    @NoArgsConstructor
    static
    public class InventoryElementWithDomain {
        String domainId;
        NewInventoryElementDTO element;
    }
}
