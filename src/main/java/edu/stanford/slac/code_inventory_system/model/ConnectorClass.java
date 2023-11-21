package edu.stanford.slac.code_inventory_system.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Define the connection of an item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorClass {
    /**
     * Specify the maximum number of the connector
     */
    private Integer count;
    /**
     * The type of the connection represented by the class
     */
    private String classID;
}
