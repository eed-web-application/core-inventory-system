package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.QueryParameter;

import java.util.List;

public interface InventoryElementRepositoryCustom {
    List<InventoryElement> searchAll(QueryParameter queryParameter);
}