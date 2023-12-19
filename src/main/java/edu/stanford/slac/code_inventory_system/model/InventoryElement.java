package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.model.value.AbstractValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;

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
     * The element description
     */
    private String description;
    /**
     * specify the domain which the item belong
     */
    private String domainId;
    /**
     * IS the id of one of the existing class {@link InventoryClass#id name}
     */
    private String classId;
    /**
     * Is the unique id of the parent in the inventory
     */
    private String parentId;
    /**
     * represent the full three path from the root to this element that is the leaf
     */
    private String fullTreePath;
    /**
     *  Indicate for which other InventoryElement this is the implementation
     *  For example an element that represent a Server001, during the year
     *  can be implemented using different server machine.
     */
    private String implementationFor;
    /**
     * Teh value for the attributes
     */
    @Builder.Default
    private List<AbstractValue> attributes = emptyList();
    /**
     * the list of the connector class that can be used as ID
     */
    @Builder.Default
    private List<ConnectorClass> connectorClasses = emptyList();
    /**
     * IS the history of that element
     */
    @Builder.Default
    private List<InventoryMaintenance> maintenanceHistory = emptyList();
    /**
     * Define the ids of the tag associated with the element
     */
    @Builder.Default
    private List<String> tags = emptyList();
    @CreatedDate
    private LocalDateTime createdDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @Version
    private Long version;
}
