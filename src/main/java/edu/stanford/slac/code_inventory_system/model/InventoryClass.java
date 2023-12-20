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

import lombok.*;
import org.springframework.data.annotation.*;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * An InventoryClass defines the different classes of items and cables in your system, specifying their attributes and connectivity rules.
 * {
 *   "_id": "serverClass",
 *   "name": "Server",
 *   "attributes": {
 *     {name:"CPU", "mandatory": true, "type": "string", "unit": ""},
 *     {name:"RAM", "mandatory": true, "type": "string", "unit": "GB"},
 *     {name:""PowerSupply", ""mandatory": false, "type": "string", "unit": "Watts"},
 *     {name:""Storage", "mandatory": true, "type": "string", "unit": "TB"}
 *   },
 *   "connectableClasses": ["powerSupplyClass", "networkDeviceClass"]
 * }
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InventoryClass {
    @Id
    String id;
    /**
     * define the name of the inventory element
     */
    String name;
    /**
     * The description of the class
     */
    String description;
    /**
     * The list of Ids of class that are extended by this
     */
    @Builder.Default
    List<String> extendsClass = Collections.emptyList();
    /**
     * Define the ids of all the class that this
     * class can be a parent of
     */
    @Builder.Default
    List<String> permittedChildClass = Collections.emptyList();
    /**
     * The list of classes that can be used to implement the given class.
     */
    @Builder.Default
    List<String> implementedByClass = Collections.emptyList();
    /**
     * Define the list for that can be used to specialize the element
     */
    @Builder.Default
    List<InventoryClassAttribute> attributes = Collections.emptyList();
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
