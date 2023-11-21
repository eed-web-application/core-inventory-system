package edu.stanford.slac.code_inventory_system.model;

import org.springframework.data.annotation.Id;

/**
 * Define a tag
 */
public class Tag {
    @Id
    private String id;
    private String name;
}
