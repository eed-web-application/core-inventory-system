package edu.stanford.slac.code_inventory_system.model;

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
public class InventoryMaintenance {
    /**
     * The time stamp when the event is occurred
     */
    private LocalDateTime timestamp;

    /**
     * the type of the record
     */
    private InventoryMaintenanceHistoryType type;

    /**
     * The action that has been performed
     */
    private String action;

    /**
     * The description that in the case of a remote record
     * permit to store the minimal information for retrieve
     * the record remotely
     */
    private String description;
}
