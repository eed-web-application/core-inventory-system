package edu.stanford.slac.code_inventory_system.model;

/**
 * Define the type of element that can be a part of the inventory
 */
public enum InventoryClassType {
    /**
     * Define a type for a physical structure that is composed by one or more floor
     */
    Building,
    /**
     * A subdivision of a building, typically occupying a single level.
     */
    Floor,
    /**
     * A defined space within a floor, typically enclosed by walls.
     */
    Room,
    /**
     * A physical object that can be stored, tracked, or managed within the inventory system.
     */
    Item,
    /**
     * A hardware that is the implementation for an item
     */
    ItemHardware,
    /**
     * A software program that is an implementation for na item
     */
    ItemSoftware,
    /**
     * A component that establishes a connection between two or more entities within the inventory system using cable.
     */
    Connector,
    /**
     * A flexible conductor that carries signals or electrical power between two connector within the inventory system.
     */
    Cable
}
