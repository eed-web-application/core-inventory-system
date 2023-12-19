package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.model.value.AbstractValue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;

/**
 * Represents the history of an attribute for an inventory element.
 */
@Data
@Builder
@ToString
@AllArgsConstructor
public class InventoryElementAttributeHistory {
    @Id
    String id;
    @NotNull
    String inventoryDomainId;
    @NotNull
    String inventoryElementId;
    @NotNull
    AbstractValue value;
    @CreatedDate
    private LocalDateTime createdDate;
    @CreatedBy
    private String createdBy;
}
