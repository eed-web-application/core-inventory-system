package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.ad.eed.baselib.utility.StringUtilities;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryElementMapper;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.exception.InventoryDomainNotFound;
import edu.stanford.slac.code_inventory_system.exception.InventoryDomainParentElementMismatch;
import edu.stanford.slac.code_inventory_system.exception.InventoryElementNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryDomainRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;
import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;
import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;

/**
 * HIgh level API for the management of the inventory domain and item
 */
@Service
@AllArgsConstructor
public class InventoryElementService {
    InventoryElementMapper inventoryElementMapper;
    InventoryClassRepository inventoryClassRepository;
    InventoryDomainRepository inventoryDomainRepository;
    InventoryElementRepository inventoryElementRepository;

    /**
     * Create new inventory domain, after the name normalization
     *
     * @param newInventoryDomainDTO the inventory domain
     * @return return the id of the newly create inventory domain
     */
    public String createNew(NewInventoryDomainDTO newInventoryDomainDTO) {
        // name normalization
        var newlyCreatedDomain = wrapCatch(
                () -> inventoryDomainRepository.save(
                        inventoryElementMapper.toModel(
                                newInventoryDomainDTO
                                        .toBuilder()
                                        .name(
                                                normalizeStringWithReplace(
                                                        newInventoryDomainDTO.name(),
                                                        " ",
                                                        "-"
                                                )
                                        )
                                        .build()
                        )
                ),
                -1
        );
        return newlyCreatedDomain.getId();
    }

    /**
     * Return the full domain
     */
    public InventoryDomainDTO getFullDomain(String domainId) {
        var newlyCreatedDomain = wrapCatch(
                () -> inventoryDomainRepository.findById(
                        domainId
                ),
                -1
        ).orElseThrow(
                () -> InventoryDomainNotFound
                        .domainNotFoundById()
                        .errorCode(-2)
                        .id(domainId)
                        .build()
        );
        return inventoryElementMapper.toDTO(newlyCreatedDomain);
    }

    /**
     * Create a new inventory element
     * An element is created for a specific InventoryClass. The class
     * give the rule for checking that all mandatory attribute are set and all
     * attribute belong to the right class
     *
     * @param newInventoryElementDTO is the new inventory item to create
     */
    public String createNew(NewInventoryElementDTO newInventoryElementDTO) {
        if(newInventoryElementDTO == null) return null;
        // check for name id
        assertion(
                ControllerLogicException
                        .builder()
                        .errorCode(-1)
                        .errorMessage("The mandatory field are missing")
                        .errorDomain("InventoryElementService::createNew(NewInventoryElementDTO)")
                        .build(),
                () -> newInventoryElementDTO.name() != null,
                () -> !newInventoryElementDTO.name().isEmpty(),
                ()-> newInventoryElementDTO.classId() !=null,
                ()-> !newInventoryElementDTO.classId().isEmpty(),
                ()-> newInventoryElementDTO.domainId() !=null,
                ()-> !newInventoryElementDTO.domainId().isEmpty()
        );
        // convert element to model
        var inventoryElementToSave = inventoryElementMapper.toModel(
                newInventoryElementDTO.toBuilder()
                        .name(
                                normalizeStringWithReplace(
                                        newInventoryElementDTO.name(),
                                        " ",
                                        "-"
                                )
                        )
                        .build()
        );



        // check for domain id
        assertion(
                InventoryDomainNotFound
                        .domainNotFoundById()
                        .errorCode(-2)
                        .id(inventoryElementToSave.getDomainId())
                        .build(),
                () -> inventoryDomainRepository.existsById(inventoryElementToSave.getDomainId())
        );

        // check for class id
        assertion(
                InventoryClassNotFound
                        .classNotFoundById()
                        .errorCode(-3)
                        .id(inventoryElementToSave.getDomainId())
                        .build(),
                () -> inventoryClassRepository.existsById(inventoryElementToSave.getClassId())
        );


        if (inventoryElementToSave.getParentId() != null) {
            // check if parent exists and belong to the same domain
            InventoryElement parentElement = wrapCatch(
                    () -> inventoryElementRepository.findById(inventoryElementToSave.getParentId()),
                    -4
            ).orElseThrow(
                    () -> InventoryElementNotFound
                            .elementNotFoundById()
                            .errorCode(-4)
                            .id(inventoryElementToSave.getParentId())
                            .build()
            );

            assertion(
                    InventoryDomainParentElementMismatch
                            .domainMismatch()
                            .errorCode(-5)
                            .parentElement(parentElement.getFullTreePath())
                            .actualDomain(inventoryElementToSave.getDomainId())
                            .build(),
                    () -> inventoryElementRepository.existsById(inventoryElementToSave.getParentId())
            );

            inventoryElementToSave.setParentId(inventoryElementToSave.getParentId());
            inventoryElementToSave.setFullTreePath(
                    "%s/%s".formatted(
                            parentElement.getFullTreePath(),
                            inventoryElementToSave.getName()
                    )
            );
        } else {
            inventoryElementToSave.setFullTreePath(
                    "/%s".formatted(
                            inventoryElementToSave.getName()
                    )
            );
        }

        // save new element
        var newlyCreatedElement = wrapCatch(
                () -> inventoryElementRepository.save(
                        inventoryElementToSave
                ),
                -6
        );
        return newlyCreatedElement.getId();
    }
}
