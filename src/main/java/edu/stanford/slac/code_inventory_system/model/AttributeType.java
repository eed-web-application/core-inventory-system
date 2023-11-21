package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.model.value.*;

/**
 * Define the type of the attribute
 */
public enum AttributeType {
    String(StringValue.class),
    Number(NumberValue.class),
    Boolean(BooleanValue.class),
    Date(DateValue.class),
    DateTime(DateTimeValue.class);

    private final Class<? extends AbstractValue> valueClassType;
    AttributeType(Class<? extends AbstractValue> valueClassType) {
        this.valueClassType = valueClassType;
    }
    /**
     * Returns the class type associated to the enumeration type
     *
     * @return the class that specify the value
     */
    public Class<? extends AbstractValue> toClassType() {
        return valueClassType;
    }

    /**
     * Return a type from the attribute name
     * @param name th name of the attribute
     * @return the enumeration that matches
     */
    public static AttributeType fromName(String name) {
        for (AttributeType t : AttributeType.values()) {
            if (t.name().compareToIgnoreCase(name) == 0) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + name);
    }
}
