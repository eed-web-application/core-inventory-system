package edu.stanford.slac.code_inventory_system.model;


import lombok.*;

/**
 * Define an attribute to enrich the class attribute({@link InventoryClass#attributes}) list
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InventoryClassAttribute {
    /**
     * Is the name of the attribute
     */
    String name;
    /**
     * Is the description of what the attribute is used for
     */
    String description;
    /**
     * Define if the attribute is mandatory or no
     */
    Boolean mandatory;
    /**
     * Define the type of the attribute for the {@link InventoryClassAttributeType}
     * enum
     */
    InventoryClassAttributeType type;
    /**
     * Specify the type of the unit that represent the attribute
     */
    String unit;
}
