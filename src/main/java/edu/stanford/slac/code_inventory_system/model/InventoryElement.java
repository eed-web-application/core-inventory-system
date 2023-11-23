package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.model.value.AbstractValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * represent the element that compose the inventory which is the specialization
 * of one the {@link InventoryClass class} created
 */
@Data
@Builder
@ToString
@AllArgsConstructor
public class InventoryElement {
    @Id
    private String id;
    /**
     * Is the name of the element
     */
    private String name;

    /**
     * specify the domain which the item belong
     */
    private String domainId;

    /**
     * IS the id of one of the existing class {@link InventoryClass#id name}
     */
    private String classId;

    /**
     * Is the unique id of the parend in the inventory
     */
    private String parendId;

    /**
     * represent the full three path from the root to this element that is the leaf
     */
    private String fullTreePath;

    /**
     * Teh value for the attributes
     */
    private List<AbstractValue> attributes;

    /**
     * the list of the connector class that can be used as ID
     */
    private List<ConnectorClass> connectorClasses;

    /**
     * IS the history of that element
     */
    private List<InventoryElementHistory> history;

    /**
     * Define the ids of the tag associated with the element
     */
    private List<String> tags;
}
