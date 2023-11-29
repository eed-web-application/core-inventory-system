package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.ad.eed.baselib.utility.StringUtilities;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryElementMapper;
import edu.stanford.slac.code_inventory_system.exception.*;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.Tag;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryDomainRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;
import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;
import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;

import java.util.List;
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
        String domainNormalizedName = normalizeStringWithReplace(
                newInventoryDomainDTO.name(),
                " ",
                "-"
        );
        // check if exists
        assertion(
                InventoryDomainAlreadyExists.
                        domainAlreadyExistsByName()
                        .errorCode(-1)
                        .domainName(domainNormalizedName)
                        .build(),
                () -> !inventoryDomainRepository.existsByNameIs(domainNormalizedName)
        );

        // name normalization
        var newlyCreatedDomain = wrapCatch(
                () -> inventoryDomainRepository.save(
                        inventoryElementMapper.toModel(
                                newInventoryDomainDTO
                                        .toBuilder()
                                        .name(
                                                domainNormalizedName
                                        )
                                        .build()
                        )
                ),
                -2
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
     * Update the domain applying all integrity check
     * all the tag will be manage in this way:
     * 1) tag witout id: will be created into the domain,
     * 2) tag with id will be checked the information and in case updated
     * 3) tag stored on the database but not in this instance will be deleted
     *
     * @param inventoryDomainDTO the domain to update
     */
    public void update(InventoryDomainDTO inventoryDomainDTO) {
        InventoryDomain inventoryToUpdate = inventoryElementMapper.toModel(inventoryDomainDTO);

        // get the inventory saved on database for work on tag
        InventoryDomain savedDomain = wrapCatch(
                ()->inventoryDomainRepository.findById(inventoryDomainDTO.id()),
                -1
        ).orElseThrow(
                ()->InventoryDomainNotFound.domainNotFoundById()
                        .errorCode(-2)
                        .id(inventoryToUpdate.getId())
                        .build()
        );
        // update the tag
        updateTags(inventoryToUpdate.getTags(), savedDomain.getTags());
    }

    private void updateTags(List<Tag> updatedTag, List<Tag> storedTag) {

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
        if (newInventoryElementDTO == null) return null;
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
                () -> newInventoryElementDTO.classId() != null,
                () -> !newInventoryElementDTO.classId().isEmpty(),
                () -> newInventoryElementDTO.domainId() != null,
                () -> !newInventoryElementDTO.domainId().isEmpty()
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
