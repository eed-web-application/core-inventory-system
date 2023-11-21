package edu.stanford.slac.code_inventory_system.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * Define a space where the inventory live, different domains
 *  contain different inventory item classes are shared by domain
 */
@Data
@Builder
@AllArgsConstructor
public class InventoryDomain {
    @Id
    private String id;

    /**
     * The name of the domain
     */
    private String name;

    /**
     * The description of the domain
     */
    private String description;

    /**
     * The list of the tags that can be used for all
     * inventory elements of this domain
     */
    private List<Tag> tags;
}
