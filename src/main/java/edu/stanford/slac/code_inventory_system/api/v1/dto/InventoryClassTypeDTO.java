package edu.stanford.slac.code_inventory_system.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 */
@Schema(description = "Define the type of element that can be a part of the inventory")
public enum InventoryClassTypeDTO {
    @Schema(description = "Define a type for a physical structure that is composed by one or more floor")
    Building,
    @Schema(description = "A subdivision of a building, typically occupying a single level.")
    Floor,
    @Schema(description = "A defined space within a floor, typically enclosed by walls.")
    Room,
    @Schema(description = "A physical object that can be stored, tracked, or managed within the inventory system.")
    Item,
    @Schema(description = "A software program can be stored, executed, or managed within the inventory system.")
    Software,
    @Schema(description = "A component that establishes a connection between two or more entities within the inventory system using cable.")
    Connector,
    @Schema(description = "A flexible conductor that carries signals or electrical power between two connector within the inventory system.")
    Cable
}
