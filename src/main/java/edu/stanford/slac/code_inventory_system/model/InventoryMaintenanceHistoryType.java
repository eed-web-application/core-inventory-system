package edu.stanford.slac.code_inventory_system.model;

/**
 * Type of history records
 */
public enum InventoryMaintenanceHistoryType {
    /**
     * Define a history record recorded locally, in this case
     * the description contains the event recorded
     */
    Local,
    /**
     * Define history record using external method
     * in this case the description field is used to encode the remote
     * information for find the record
     */
    Remote
}
