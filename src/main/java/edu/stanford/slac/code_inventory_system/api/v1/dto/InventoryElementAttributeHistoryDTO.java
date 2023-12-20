package edu.stanford.slac.code_inventory_system.api.v1.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents the history of an attribute for an inventory element.
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InventoryElementAttributeHistoryDTO {
    @Parameter(description = "the unique id of history element")
    String id;
    @Parameter(description = "the relative inventory domain id which the attribute belong")
    String inventoryDomainId;
    @Parameter(description = "the relative inventory element id which the attribute belong")
    String inventoryElementId;
    @Parameter(description = "the old value")
    InventoryElementAttributeValueDTO value;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Parameter(description = "the date when the value has been update")
    private LocalDateTime createdDate;
    @Parameter(description = "the user that updated this value")
    private String createdBy;
}
