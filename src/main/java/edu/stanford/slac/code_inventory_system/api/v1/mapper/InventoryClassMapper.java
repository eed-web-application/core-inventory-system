package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public abstract class InventoryClassMapper {
    abstract public InventoryClass toModel(NewInventoryClassDTO newInventoryClassDTO);

    abstract public InventoryClassDTO toDTO(InventoryClass inventoryClass);
}
