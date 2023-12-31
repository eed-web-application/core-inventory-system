package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttributeType;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public abstract class InventoryClassMapper {
    abstract public InventoryClass toModel(NewInventoryClassDTO newInventoryClassDTO);

    abstract public InventoryClassDTO toDTO(InventoryClass inventoryClass);

    abstract public InventoryClassSummaryDTO toSummaryDTO(InventoryClass inventoryClass);

    public abstract void updateModel(@MappingTarget InventoryClass inventoryClass, UpdateInventoryClassDTO updateInventoryClassDTO);

    public abstract UpdateInventoryClassDTO toUpdate(InventoryClassDTO inventoryClassDTO);

    /**
     * Convert the two different attribute type
     *
     * @param inventoryClassAttributeType class oriented attribute value
     * @return the simple enum oriented dto
     */
    public InventoryClassAttributeTypeDTO toDTO(InventoryClassAttributeType inventoryClassAttributeType) {
        return InventoryClassAttributeTypeDTO.valueOf(
                inventoryClassAttributeType.name()
        );
    }

    /**
     * Convert the two different attribute type
     *
     * @param inventoryClassAttributeTypeDTO the normal enum oriented
     * @return the class oriented enum
     */
    public InventoryClassAttributeType toModel(InventoryClassAttributeTypeDTO inventoryClassAttributeTypeDTO) {
        return InventoryClassAttributeType.fromName(
                inventoryClassAttributeTypeDTO.name()
        );
    }
}
