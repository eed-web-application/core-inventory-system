package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassSummaryDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.UpdateInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryClassMapper;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
                ()->inventoryClassRepository.findById(id),
                -1,
                "InventoryClassService::createNew"
        ).orElseThrow(
                ()->InventoryClassNotFound.classNotFoundById()
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
    public InventoryClassDTO findById(String id) {
        var optionalInventoryClass = wrapCatch(
                () -> inventoryClassRepository.findById(
                        id
                ),
                -1,
                "InventoryClassService::findById"
        );
        return optionalInventoryClass
                .map(inventoryClassMapper::toDTO)
                .orElseThrow(
                        () -> InventoryClassNotFound
                                .classNotFoundById()
                                .errorCode(-2)
                                .id(id)
                                .build()
                );
    }


    /**
     * Return all the found class
     *
     * @return a list of all the class
     */
    public List<InventoryClassSummaryDTO> findAll() {
        var allClass = wrapCatch(
                () -> inventoryClassRepository.findAll(),
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
