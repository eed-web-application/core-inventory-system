package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryClassMapper;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttribute;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;


/**
 * Defines high level api for the management of the inventory classes
 */
@Service
@AllArgsConstructor
public class InventoryClassService {
    InventoryClassMapper inventoryClassMapper;
    InventoryClassRepository inventoryClassRepository;

    /**
     * Create a new inventory class
     *
     * @param newInventoryClassDTO the new inventory class to create
     * @return the newly create class id
     */
    public String createNew(NewInventoryClassDTO newInventoryClassDTO) {
        var newInventoryClass = wrapCatch(
                () -> inventoryClassRepository.save(
                        inventoryClassMapper.toModel(
                                newInventoryClassDTO
                        )
                ),
                -1
        );
        return newInventoryClass.getId();
    }

    /**
     * Update existing inventory class
     */
    public boolean update(String id, UpdateInventoryClassDTO updateInventoryClassDTO) {
        InventoryClass icToUpdate = wrapCatch(
                () -> inventoryClassRepository.findById(id),
                -1,
                "InventoryClassService::createNew"
        ).orElseThrow(
                () -> InventoryClassNotFound.classNotFoundById()
                        .errorCode(-1)
                        .id(id)
                        .build()
        );
        inventoryClassMapper.updateModel(icToUpdate, updateInventoryClassDTO)
        ;
        var updatedInventoryClass = wrapCatch(
                () -> inventoryClassRepository.save(
                        icToUpdate

                ),
                -1,
                "InventoryClassService::createNew"
        );
        return updatedInventoryClass != null;
    }

    /**
     * Return the inventory class dto using the id
     *
     * @param id the unique identifier of the inventory class
     * @return the inventory class found
     * @throws edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException in case of error on database
     * @throws InventoryClassNotFound                                              if the inventory class will not be found
     */
    public InventoryClassDTO findById(String id, boolean resolveInheritance) {
        var inventoryClass = wrapCatch(
                () -> inventoryClassRepository.findById(
                        id
                ),
                -1,
                "InventoryClassService::findById"
        ).orElseThrow(
                () -> InventoryClassNotFound
                        .classNotFoundById()
                        .errorCode(-2)
                        .id(id)
                        .build()
        );
        if(resolveInheritance) {
            InheritedClassField inheritedClassField = InheritedClassField.builder().build();
            getInheritedField(inventoryClass.getExtendsClass(), inheritedClassField);

            // add the principal class to the sets
            inheritedClassField.extendsClass.addAll(inventoryClass.getExtendsClass());
            inheritedClassField.permittedChildClass.addAll(inventoryClass.getPermittedChildClass());
            inheritedClassField.attributes.addAll(inventoryClass.getAttributes());

            // override all found into the original class
            inventoryClass.setExtendsClass(inheritedClassField.extendsClass.stream().toList());
            inventoryClass.setPermittedChildClass(inheritedClassField.permittedChildClass.stream().toList());
            inventoryClass.setAttributes(inheritedClassField.attributes.stream().toList());
        }
        return inventoryClassMapper.toDTO(inventoryClass);

    }

    /**
     * Checks if an inventory class exists by its ID.
     *
     * @param classId the ID of the inventory class
     * @return {@code true} if the inventory class exists, {@code false} otherwise
     */
    public Boolean existsById(String classId) {
        return wrapCatch(
                () -> inventoryClassRepository.existsById(
                        classId
                ),
                -1
        );
    }

    /**
     * Represents a field of the inherited class.
     */
    @Builder
    private static class InheritedClassField {
        @Builder.Default
        Set<String> extendsClass = new HashSet<>();
        @Builder.Default
        Set<String> permittedChildClass = new HashSet<>();
        @Builder.Default
        Set<String> implementedByClass = new HashSet<>();
        @Builder.Default
        Set<InventoryClassAttribute> attributes = new HashSet<>();
    }

    /**
     * Retrieves the inherited fields from the given list of subclass IDs and populates the provided InheritedClassField object.
     *
     * @param subclassIds The list of subclass IDs from which to retrieve the inherited fields
     * @param inheritedClassField The InheritedClassField object to populate with the inherited fields
     */
    private void getInheritedField(@NonNull List<String> subclassIds, @NonNull InheritedClassField inheritedClassField) {
        if (subclassIds.isEmpty()) return;
        for (String classId : subclassIds) {
            var inventoryClass = wrapCatch(
                    () -> inventoryClassRepository.findById(
                            classId
                    ),
                    -1
            ).orElseThrow(
                    () -> InventoryClassNotFound
                            .classNotFoundById()
                            .errorCode(-2)
                            .id(classId)
                            .build()
            );

            inheritedClassField.extendsClass.addAll(inventoryClass.getExtendsClass());
            inheritedClassField.permittedChildClass.addAll(inventoryClass.getPermittedChildClass());
            inheritedClassField.implementedByClass.addAll(inventoryClass.getImplementedByClass());
            inheritedClassField.attributes.addAll(inventoryClass.getAttributes());

            if (!inventoryClass.getExtendsClass().isEmpty()) {
                getInheritedField(inventoryClass.getExtendsClass(), inheritedClassField);
            }
        }
    }

    /**
     * Return all the found class
     *
     * @return a list of all the class
     */
    public List<InventoryClassSummaryDTO> findAll(Optional<String> search) {
        var allClass = wrapCatch(
                () -> search.isPresent()? inventoryClassRepository.findAllByNameContainsIgnoreCase(search.get()):inventoryClassRepository.findAll(),
                -1,
                "InventoryClassService::findById"
        );
        return allClass.stream()
                .map(
                        inventoryClassMapper::toSummaryDTO
                )
                .toList();
    }
}
