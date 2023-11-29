package edu.stanford.slac.code_inventory_system.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Define the type of an attribute class")
public enum InventoryClassAttributeTypeDTO {
    String,
    Number,
    Double,
    Boolean,
    Date,
    DateTime
}
