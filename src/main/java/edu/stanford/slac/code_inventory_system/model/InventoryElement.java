/*
 * Copyright (c) 2023, The Board of Trustees of the Leland Stanford Junior University,
 * through SLAC National Accelerator Laboratory. This file is part of code-inventory-system. It is subject
 * to the license terms in the LICENSE.txt file found in the top-level directory of this distribution
 * and at: https://confluence.slac.stanford.edu/display/ppareg/LICENSE.html.
 * No part of code-inventory-system, including this file, may be copied, modified, propagated, or distributed
 * except according to the terms contained in the LICENSE.txt file.
 *
 */

package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.model.value.AbstractValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

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
     * Is the unique id of the parent in the inventory, in case this is an implementation element
     * the parent id point to the implemented element
     */
    @Field(targetType = FieldType.OBJECT_ID)
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
     * The implementedBy variable in the InventoryElement class represents the implementation of the element.
     * It indicates which other InventoryElement is an implementation of the current element.
     * <p>
     * For example, if an element represents a server (e.g., "Server001"), the implementedBy variable can be used to specify
     * the server machine that is being used to implement the server element. This allows for tracking different
     * implementations of the same element over time.
     * <p>
     * The implementedBy variable is an elementId that belong to a class that is contained in {{@link InventoryClass#implementedByClass}}
     */
    @Builder.Default
    private String implementedBy = null;

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
