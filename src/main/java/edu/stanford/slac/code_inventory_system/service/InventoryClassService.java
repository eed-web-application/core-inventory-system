package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassSummaryDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassTypeDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryClassMapper;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.wrapCatch;

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
                -1,
                "InventoryClassService::createNew"
        );
        return newInventoryClass.getId();
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
                                .errorDomain("InventoryClassService::findById")
                                .build()
                );
    }

    /**
     * Return all the found class
     *
     * @return a list of all the class
     */
    public List<InventoryClassSummaryDTO> findAll(Optional<List<InventoryClassTypeDTO>> inventoryClassTypeDTO) {
        var allClass = wrapCatch(
                () -> inventoryClassTypeDTO.isEmpty() ?
                        inventoryClassRepository.findAll() :
                        inventoryClassRepository.findAllByTypeIn(
                                inventoryClassTypeDTO.get()
                                        .stream()
                                        .map(
                                                typeDTO -> inventoryClassMapper.toModel(typeDTO)
                                        )
                                        .toList()
                        ),
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
