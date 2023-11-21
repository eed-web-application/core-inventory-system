package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.model.value.DateTimeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Define a history record for an inventory element
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryElementHistory {
    private LocalDateTime timestamp;
    private InventoryElementHistoryType type;
    private String action;
    private String description;
}
