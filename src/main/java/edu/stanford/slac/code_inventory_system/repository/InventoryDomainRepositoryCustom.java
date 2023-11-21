package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.Tag;

public interface InventoryDomainRepositoryCustom {
    /**
     * Ensure the creation of the tag for a domain, in an atomic way
     * if more than one thread try to create the same tag, only one will be
     * created
     * @param id the id of the domain
     * @param newTag the new tag to create
     * @return the id of the new created tag
     */
    String ensureTag(String id, Tag newTag);
}