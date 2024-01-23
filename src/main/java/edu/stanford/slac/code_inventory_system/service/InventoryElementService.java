/*
 * Copyright (c) 2023, The Board of Trustees of the Leland Stanford Junior University,
 * through SLAC National Accelerator Laboratory. This file is part of code-inventory-system. It is subject
 * to the license terms in the LICENSE.txt file found in the top-level directory of this distribution
 * and at: https://confluence.slac.stanford.edu/display/ppareg/LICENSE.html.
 * No part of code-inventory-system, including this file, may be copied, modified, propagated, or distributed
 * except according to the terms contained in the LICENSE.txt file.
 *
 */

package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.NewAuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.mapper.AuthMapper;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryElementMapper;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.QueryParameterMapper;
import edu.stanford.slac.code_inventory_system.exception.*;
import edu.stanford.slac.code_inventory_system.model.*;
import edu.stanford.slac.code_inventory_system.model.value.AbstractValue;
import edu.stanford.slac.code_inventory_system.repository.InventoryDomainRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementAttributeHistoryRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.any;
import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;
import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;
import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;
import static edu.stanford.slac.code_inventory_system.service.utility.IdValueObjectUtil.updateResource;

/**
 * HIgh level API for the management of the inventory domain and item
 */
@Log4j2
@Service
@Validated
@AllArgsConstructor
public class InventoryElementService {
    AuthMapper authMapper;
    AuthService authService;
    QueryParameterMapper queryParameterMapper;
    InventoryElementMapper inventoryElementMapper;
    InventoryClassService inventoryClassService;
    InventoryDomainRepository inventoryDomainRepository;
    InventoryElementRepository inventoryElementRepository;
    InventoryElementAttributeHistoryRepository inventoryElementAttributeHistoryRepository;

    /**
     * Create new inventory domain, after the name normalization
     *
     * @param newInventoryDomainDTO the inventory domain
     * @return return the id of the newly create inventory domain
     */
    @Transactional
    public String createNew(@Valid NewInventoryDomainDTO newInventoryDomainDTO) {
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

        // create tag id
        var inventoryDomainToSave = inventoryElementMapper.toModel(
                newInventoryDomainDTO
                        .toBuilder()
                        .name(
                                domainNormalizedName
                        )
                        .build()
        );
        inventoryDomainToSave.getTags().forEach(
                tag -> tag.toBuilder().id(UUID.randomUUID().toString()).build()
        );

        // name normalization
        var newlyCreatedDomain = wrapCatch(
                () -> inventoryDomainRepository.save(
                        inventoryDomainToSave
                ),
                -2
        );

        // update authorization for the domain
        if (newInventoryDomainDTO.authorizations() != null) {
            manageAuthorizationForDomain(newlyCreatedDomain, newInventoryDomainDTO.authorizations());
        }

        log.info("User '{}' created a new inventory domain: '{}'", newlyCreatedDomain.getCreatedBy(), newlyCreatedDomain);
        return newlyCreatedDomain.getId();
    }


    /**
     * Update the domain applying all integrity check
     * all the tag will be manage in this way:
     * 1) tag without id: will be created into the domain,
     * 2) tag with id will be checked the information and in case updated
     * 3) tag stored on the database but not in this instance will be deleted
     *
     * @param updateDomainDTO the domain to update
     */
    @Transactional
    public void update(String domainId, @Valid UpdateDomainDTO updateDomainDTO) {
        // get the inventory saved on database for work on tag and lock
        InventoryDomain savedDomain = wrapCatch(
                () -> inventoryDomainRepository.findById(domainId),
                -1
        ).orElseThrow(
                () -> InventoryDomainNotFound.domainNotFoundById()
                        .errorCode(-2)
                        .id(domainId)
                        .build()
        );

        // keep for check the update
        List<Tag> storedTags = savedDomain.getTags();
        inventoryElementMapper.updateModel(
                savedDomain,
                updateDomainDTO
        );

        // update the tag
        updateResource(
                // these are the updated tags
                savedDomain.getTags(),
                // these are the stored tags on database
                storedTags,
                (tagId) -> inventoryElementRepository.existsByDomainIdIsAndTagsContains(
                        savedDomain.getId(),
                        tagId
                ),
                (notFoundTag) -> TagNotFound.tagNotFound()
                        .errorCode(-3)
                        .tag((Tag) notFoundTag)
                        .build(),
                (inUseTag) -> TagInUse.tagInUse()
                        .errorCode(-3)
                        .tag((Tag) inUseTag)
                        .build()
        );

        // update authorization for the domain
        if (updateDomainDTO.authorizations() != null) {
            manageAuthorizationForDomain(savedDomain, updateDomainDTO.authorizations());
        }

        // update the domain
        var updateInventoryElement = wrapCatch(
                () -> inventoryDomainRepository.save(savedDomain),
                -4
        );

        log.info("User '{}' update the inventory domain '{}' ", updateInventoryElement.getCreatedBy(), updateInventoryElement.getName());
    }

    /**
     * Manages the authorization for domain
     *
     * @param domain         the domain for which we need to manage the authorization
     * @param authorizations the list on the new authorization old and new
     */
    private void manageAuthorizationForDomain(InventoryDomain domain, @Valid List<AuthorizationDTO> authorizations) {
        String domainAuthorizationResource = "/cis/domain/%s".formatted(domain.getId());
        List<AuthorizationDTO> allAuthorizationForDomain = new ArrayList<>(authService.findByResourceIs(domainAuthorizationResource));
        for (AuthorizationDTO authorization :
                authorizations) {
            if (authorization.id() == null) {
                var newAuth = NewAuthorizationDTO
                        .builder()
                        .owner(authorization.owner())
                        .authorizationType(authorization.authorizationType())
                        .ownerType(authorization.ownerType())
                        .resource(domainAuthorizationResource)
                        .build();
                String newId = authService.addNewAuthorization(
                        newAuth
                );
                log.info("New authorization id {} created for domain {} with values {}", newId, domain.getName(), newAuth);
            } else {
                // remove authorization not more needed
                boolean removed = allAuthorizationForDomain.removeIf(
                        auth -> auth.id().compareToIgnoreCase(authorization.id()) == 0
                );
                // if the authorization has been removed from the allAuthorizationForDomain list
                // means that it should not be removed
                assertion(
                        AuthorizationNotFound.authorizationNotFound()
                                .errorCode(-1)
                                .authId(authorization.id())
                                .build(),
                        () -> removed
                );
            }
        }

        // all the authorization that are still present on the allAuthorizationForDomain list
        // needs to be removed
        allAuthorizationForDomain.forEach(
                authToRemove -> {
                    authService.deleteAuthorizationById(authToRemove.id());
                    log.info("Removed authorization id {} from domain {} with values {}", authToRemove.id(), domain.getName(), authToRemove);
                }
        );
    }

    /**
     * Return the full domain
     */
    public InventoryDomainDTO getInventoryDomainById(String domainId) {
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
     * Return all the domain
     *
     * @return the complete list of the domain
     */
    public List<InventoryDomainSummaryDTO> findAllDomain() {
        return wrapCatch(
                () -> inventoryDomainRepository.findAll(),
                -1
        ).stream().map(
                inventoryElementMapper::toSummaryDTO
        ).toList();
    }

    /**
     * Create a new inventory element
     * An element is created for a specific InventoryClass. The class
     * give the rule for checking that all mandatory attribute are set and all
     * attribute belong to the right class
     *
     * @param newInventoryElementDTO is the new inventory item to create
     */
    public String createNew(String domainId, @Valid NewInventoryElementDTO newInventoryElementDTO) {
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
                () -> domainId != null,
                () -> !domainId.isEmpty()
        );
        // convert element to model
        var inventoryElementToSave = inventoryElementMapper.toModel(
                domainId,
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

        var inventoryDomainFound = wrapCatch(
                () -> inventoryDomainRepository.findById(domainId),
                -1
        ).orElseThrow(
                () -> InventoryElementNotFound
                        .elementNotFoundById()
                        .errorCode(-2)
                        .id(domainId)
                        .build()
        );

        // check for class id
        assertion(
                InventoryClassNotFound
                        .classNotFoundById()
                        .errorCode(-3)
                        .id(inventoryElementToSave.getDomainId())
                        .build(),
                () -> inventoryClassService.existsById(inventoryElementToSave.getClassId())
        );

        // checks for tag id existence
        if (!inventoryElementToSave.getTags().isEmpty()) {
            assertion(
                    TagNotFound.tagNotFoundAny()
                            .errorCode(-4)
                            .build(),
                    () -> inventoryDomainRepository.existsByIdAndAllTags(
                            inventoryElementToSave.getDomainId(),
                            inventoryElementToSave.getTags()
                    )
            );
        }

        if (inventoryElementToSave.getParentId() != null) {
            // check if parent exists and belong to the same domain
            InventoryElement parentElement = wrapCatch(
                    () -> inventoryElementRepository.findById(inventoryElementToSave.getParentId()),
                    -5
            ).orElseThrow(
                    () -> InventoryElementNotFound
                            .elementNotFoundById()
                            .errorCode(-5)
                            .id(inventoryElementToSave.getParentId())
                            .build()
            );
            // fault-tolerant check, this should never happen
            assertion(
                    InventoryDomainParentElementMismatch
                            .domainMismatch()
                            .errorCode(-6)
                            .parentElement(parentElement.getFullTreePath())
                            .actualDomain(inventoryElementToSave.getDomainId())
                            .build(),
                    () -> inventoryElementRepository.existsById(inventoryElementToSave.getParentId())
            );

            // check if this element can be a child for the parent
            // inventoryElementToSave as child
            InventoryClassDTO parentClass = inventoryClassService.findById(
                    parentElement.getClassId(),
                    false
            );

            //check for permission to be a child of the parent
            assertion(
                    ControllerLogicException.builder()
                            .errorCode(-8)
                            .errorMessage("Parent class cannot permit to have this kind of element as child")
                            .build(),
                    () -> any(
                            // if is empty element from any class can be a child
                            () -> parentClass.permittedChildClass() == null || parentClass.permittedChildClass().isEmpty(),
                            // the class id of the child need to mach one within the list
                            () -> parentClass.permittedChildClass().stream().filter(c -> c.compareTo(inventoryElementToSave.getClassId()) == 0).count() == 1
                    )
            );

            inventoryElementToSave.setParentId(inventoryElementToSave.getParentId());
            if (parentElement.getFullTreePath() != null) {
                inventoryElementToSave.setFullTreePath(
                        "%s/%s".formatted(
                                parentElement.getFullTreePath(),
                                inventoryElementToSave.getParentId()
                        )
                );
            } else {
                inventoryElementToSave.setFullTreePath(
                        "/%s".formatted(
                                inventoryElementToSave.getParentId()
                        )
                );
            }

        }

        // save new element
        var newlyCreatedElement = wrapCatch(
                () -> inventoryElementRepository.save(
                        inventoryElementToSave
                ),
                -7
        );
        log.info("User '{}' created new inventory element '{}[{}]' ", newlyCreatedElement.getCreatedBy(), newlyCreatedElement.getName(), inventoryDomainFound.getName());
        return newlyCreatedElement.getId();
    }

    /**
     * Creates a new implementation element for a specified inventory element.
     *
     * @param domainId                 The ID of the domain where the element belongs
     * @param elementId                The ID of the inventory element
     * @param newImplementationElement The new implementation element to be created
     * @return The ID of the newly created implementation element
     * @throws InventoryElementNotFound If the inventory element with the specified ID does not exist
     * @throws ControllerLogicException If the new implementation element does not belong to an authorized implementable class for the inventory element
     */
    @Transactional
    public String createNewImplementation(@NotNull String domainId, @NotNull String elementId, @NotNull NewInventoryElementDTO newImplementationElement) {
        // fetch the elements to implements
        var inventoryElementToImplements = wrapCatch(
                () -> inventoryElementRepository.findById(elementId),
                -1
        ).orElseThrow(
                () -> InventoryElementNotFound
                        .elementNotFoundById()
                        .errorCode(-2)
                        .id(elementId)
                        .build()
        );

        // fetch the class of the implemented item to check if new
        // implementation element can implement it
        var inventoryClassOfImplementedElement = wrapCatch(
                () -> inventoryClassService.findById(
                        inventoryElementToImplements.getClassId(),
                        true),
                -3
        );

        // assert on the class id of new implementation element
        // should be contained into the implementedByClass of the
        // class of the implemented item
        assertion(
                ControllerLogicException
                        .builder()
                        .errorCode(-4)
                        .errorMessage("The implementation item doesn't belong to an authorized implementable class for item:" + elementId)
                        .errorDomain("ImplementationElementService::createNewImplementation")
                        .build(),
                () -> inventoryClassOfImplementedElement
                        .implementedByClass()
                        .stream()
                        .anyMatch(e -> e.compareTo(newImplementationElement.classId()) == 0)
        );

        // create new element
        String newImplementationElementId = createNew(
                domainId,
                newImplementationElement
                        .toBuilder()
                        // the parent id of the implementation element
                        // is the implement element id
                        .parentId(elementId)
                        .build()
        );

        // set the newly created item as implementation
        inventoryElementToImplements.setImplementedBy(newImplementationElementId);

        // update the implemented element
        var savedImplementedItem = wrapCatch(
                () -> inventoryElementRepository.save(inventoryElementToImplements),
                -5
        );
        log.info(
                "User '{}' created new implementation for '{}'",
                savedImplementedItem.getLastModifiedBy(),
                savedImplementedItem.getName()
        );
        return newImplementationElementId;
    }

    /**
     * Fetches the implementation history of an inventory element.
     *
     * @param domainId  the ID of the domain
     * @param elementId the ID of the inventory element
     * @return a list of InventoryElementSummaryDTO objects representing the implementation history
     */
    public List<InventoryElementSummaryDTO> findAllImplementationForDomainAndElementIds(String domainId, String elementId) {
        // fetch the class for all implementation kind
        var foundElement = wrapCatch(
                () -> inventoryElementRepository.findById(elementId),
                1
        ).orElseThrow(
                () -> InventoryElementNotFound.elementNotFoundById()
                        .errorCode(-2)
                        .id(elementId)
                        .build()
        );

        var classOfFoundElement = wrapCatch(
                () -> inventoryClassService.findById(foundElement.getClassId(), true),
                -3
        );

        // find all the history
        List<InventoryElement> foundImplementationHistory = wrapCatch(
                () -> inventoryElementRepository.findAllByDomainIdIsAndParentIdIsAndClassIdIn(
                        domainId,
                        elementId,
                        classOfFoundElement.implementedByClass()
                ),
                -4
        );
        return foundImplementationHistory.stream()
                .map(inventoryElementMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update the inventory element
     *
     * @param domainId                  the domain id where the element belong
     * @param elementId                 the element unique identifier
     * @param updateInventoryElementDTO the information updatable
     */
    @Transactional
    public void update(String domainId, String elementId, @Valid UpdateInventoryElementDTO updateInventoryElementDTO) {
        if (updateInventoryElementDTO == null) return;

        var inventoryDomainFound = wrapCatch(
                () -> inventoryDomainRepository.findById(domainId),
                -1
        ).orElseThrow(
                () -> InventoryElementNotFound
                        .elementNotFoundById()
                        .errorCode(-2)
                        .id(domainId)
                        .build()
        );

        var inventoryElementToUpdate = wrapCatch(
                () -> inventoryElementRepository.findById(elementId),
                -1
        ).orElseThrow(
                () -> InventoryElementNotFound
                        .elementNotFoundById()
                        .errorCode(-2)
                        .id(elementId)
                        .build()
        );

        // check for domain match
        assertion(
                ControllerLogicException
                        .builder()
                        .errorCode(-3)
                        .errorMessage("Domain mismatch for this element")
                        .build(),
                () -> Objects.equals(inventoryElementToUpdate.getDomainId(), domainId)
        );

        // keep away this for check what are the modified attribute to
        // update the history
        List<AbstractValue> oldAttribute = inventoryElementToUpdate.getAttributes();

        // update the model
        inventoryElementMapper.updateModel(
                inventoryElementToUpdate,
                updateInventoryElementDTO
        );

        // update attribute value history
        updateAttributeHistory(domainId, elementId, oldAttribute, inventoryElementToUpdate.getAttributes());

        // checks for tag id existence
        if (!inventoryElementToUpdate.getTags().isEmpty()) {
            assertion(
                    TagNotFound.tagNotFoundAny()
                            .errorCode(-4)
                            .build(),
                    () -> inventoryDomainRepository.existsByIdAndAllTags(
                            inventoryElementToUpdate.getDomainId(),
                            inventoryElementToUpdate.getTags()
                    )
            );
        }

        // save element
        var updatedInventoryElement = wrapCatch(
                () -> inventoryElementRepository.save(inventoryElementToUpdate),
                -5
        );
        log.info("User '{}' updated the inventory element '{}[{}]' ", updatedInventoryElement.getCreatedBy(), updatedInventoryElement.getName(), inventoryDomainFound.getName());
    }

    /**
     * Updates the attribute history of a specified element in a specific domain.
     *
     * @param domainId      the ID of the domain where the element belongs
     * @param elementId     the ID of the element whose attribute history needs to be updated
     * @param oldAttribute  the previous value of the attribute
     * @param newAttributes the updated values of the attribute
     */
    private void updateAttributeHistory(
            String domainId,
            String elementId,
            @NotNull List<AbstractValue> oldAttribute,
            @NotNull List<AbstractValue> newAttributes
    ) {
        for (AbstractValue oldValue : oldAttribute) {
            if (newAttributes.stream()
                    .noneMatch(a -> a.equals(oldValue))
            ) {
                // the oldValue has been updated or removed
                var archivedChangedValue = wrapCatch(
                        () -> inventoryElementAttributeHistoryRepository.save(
                                InventoryElementAttributeHistory.builder()
                                        .inventoryDomainId(domainId)
                                        .inventoryElementId(elementId)
                                        .value(oldValue)
                                        .build()
                        ),
                        -1
                );
                log.info("Attribute {} has be modified by {}", archivedChangedValue.getValue().getName(), archivedChangedValue.getCreatedBy());
            }
        }
    }

    /**
     * Return the full inventory domain
     *
     * @param domainId  the domain id
     * @param elementId the element id
     * @return the full inventory element
     */
    public InventoryElementDTO getInventoryElementByDomainIdAndElementId(String domainId, String elementId) {
        // check if domain exists
        assertion(
                InventoryDomainNotFound.domainNotFoundById()
                        .errorCode(-1)
                        .id(domainId)
                        .build(),
                () -> inventoryDomainRepository.existsById(domainId)
        );
        return inventoryElementRepository.findById(elementId).map(
                inventoryElementMapper::toDTO
        ).orElseThrow(
                () -> InventoryElementNotFound.elementNotFoundById()
                        .errorCode(-2)
                        .id(elementId)
                        .build()
        );
    }

    /**
     * Return all the child of an item
     *
     * @param domainId  the domain id
     * @param elementId the element id root for the child
     * @return the list of the summary of all the children
     */
    public List<InventoryElementSummaryDTO> findAllChildrenByDomainIdAndElementId(String domainId, String elementId) {
        // check if domain exists
        assertion(
                InventoryDomainNotFound.domainNotFoundById()
                        .errorCode(-1)
                        .id(domainId)
                        .build(),
                () -> inventoryDomainRepository.existsById(domainId)
        );
        return inventoryElementRepository.findAllByDomainIdIsAndParentIdIs(
                        domainId,
                        elementId)
                .stream()
                .map(
                        inventoryElementMapper::toSummaryDTO
                ).toList();
    }

    /**
     * Perform the search operation on all inventory element
     *
     * @param queryParameterDTO the query information
     * @return the list of found element
     */
    public List<InventoryElementSummaryDTO> findAllElements(@Valid QueryParameterDTO queryParameterDTO) {
        List<InventoryElement> found = wrapCatch(
                () -> inventoryElementRepository.searchAll(
                        queryParameterMapper.fromDTO(
                                queryParameterDTO
                        )
                ),
                -1
        );
        return found.stream().map(inventoryElementMapper::toSummaryDTO).toList();
    }

    /**
     * Finds the history of a specific attribute for a given domain, element, and attribute name.
     *
     * @param domainId  the ID of the inventory domain
     * @param elementId the ID of the inventory element
     * @return a list of InventoryElementAttributeHistory objects representing the attribute history
     */
    public List<InventoryElementAttributeHistoryDTO> findAllAttributeHistory(
            String domainId,
            String elementId
    ) {
        var foundAttributeHistory = wrapCatch(
                () -> inventoryElementAttributeHistoryRepository.findAllByInventoryDomainIdIsAndInventoryElementIdIs(
                        domainId, elementId),
                -1
        );
        return foundAttributeHistory
                .stream()
                .map(history -> inventoryElementMapper.toDTO(history))
                .toList();
    }

    /**
     * Finds three paths based on the given domainId, elementId, and direction.
     *
     * @param domainId  the ID of the domain
     * @param elementId the ID of the element
     * @param threePathType    specify the type of path to return
     */
    public List<InventoryElementSummaryDTO> findThreePath(String domainId, String elementId, ThreePathType threePathType) {
        List<InventoryElement> inventoryElements = new ArrayList<>();
        assertion(
                InventoryDomainNotFound.domainNotFoundById()
                        .errorCode(-1)
                        .id(domainId)
                        .build(),
                () -> inventoryDomainRepository.existsById(domainId)
        );

        InventoryElement targetElement = wrapCatch(
                        () -> inventoryElementRepository.findById(elementId),
                        -2
                ).orElseThrow(
                        () -> InventoryElementNotFound
                                .elementNotFoundById()
                                .errorCode(-3)
                                .id(elementId)
                                .build()
                );

        switch (threePathType) {
            case Upward -> {
                inventoryElements.add(targetElement);
                inventoryElements.addAll(
                        wrapCatch(
                                () -> inventoryElementRepository.findPathToRoot(domainId, elementId),
                                -4
                        )
                );
            }
            case Downward -> {
                inventoryElements.add(targetElement);
                inventoryElements.addAll(
                        wrapCatch(
                                () -> inventoryElementRepository.findIdPathToLeaf(domainId, elementId),
                                -4
                        )
                );
            }
            case Full -> {
                // add upward path
                inventoryElements.addAll(
                        wrapCatch(
                                () -> inventoryElementRepository.findPathToRoot(domainId, elementId),
                                -4
                        )
                );
                Collections.reverse(inventoryElements);
                // add current element
                inventoryElements.add(targetElement);
                inventoryElements.addAll(
                        wrapCatch(
                                () -> inventoryElementRepository.findIdPathToLeaf(domainId, elementId),
                                -4
                        )
                );
            }
        }

        return inventoryElements
                .stream()
                .map(inventoryElementMapper::toSummaryDTO)
                .toList();
    }
}
