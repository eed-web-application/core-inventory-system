package edu.stanford.slac.code_inventory_system.model;

/**
 * Define an attribute to enrich the class attribute({@link InventoryClass#attributes}) list
 */
public class InventoryClassAttributeValue {
    /**
     * Is the name of the attribute
     */
    private String name;
    /**
     * Define if the attribute is mandatory or no
     */
    private Boolean mandatory;
    /**
     * Define the type of the attribute for the {@link AttributeType}
     * enum
     */
    private AttributeType type;
    /**
     * Specify the type of the unit that represent the attribute
     */
    private String unit;
}
