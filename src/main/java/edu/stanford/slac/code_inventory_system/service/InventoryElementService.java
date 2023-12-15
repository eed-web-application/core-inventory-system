package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.NewAuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.mapper.AuthMapper;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryElementMapper;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.QueryParameterMapper;
import edu.stanford.slac.code_inventory_system.exception.*;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.Tag;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryDomainRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
@AllArgsConstructor
public class InventoryElementService {
    AuthMapper authMapper;
    AuthService authService;
    QueryParameterMapper queryParameterMapper;
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
        var inventoryDomainToSave =  inventoryElementMapper.toModel(
                newInventoryDomainDTO
                        .toBuilder()
                        .name(
                                domainNormalizedName
                        )
                        .build()
        );
        inventoryDomainToSave.getTags().forEach(
                tag->tag.toBuilder().id(UUID.randomUUID().toString()).build()
        );

        // name normalization
        var newlyCreatedDomain = wrapCatch(
                () -> inventoryDomainRepository.save(
                        inventoryDomainToSave
                ),
                -2
        );

        // update authorization for the domain
        if(newInventoryDomainDTO.authorizations()!=null) {
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
        if(updateDomainDTO.authorizations()!=null) {
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
    private void manageAuthorizationForDomain(InventoryDomain domain, @Valid  List<AuthorizationDTO> authorizations) {
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
                () -> inventoryClassRepository.existsById(inventoryElementToSave.getClassId())
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

            // check if parent class permit to has
            // inventoryElementToSave as child
            InventoryClass parentClass = inventoryClassRepository.findById(
                    parentElement.getClassId()
            ).orElseThrow(
                    ()->InventoryClassNotFound.classNotFoundById()
                            .errorCode(-7)
                            .id(parentElement.getClassId())
                            .build()
            );

            assertion(
                    ControllerLogicException.builder()
                            .errorCode(-8)
                    .errorMessage("Parent class cannot permit to have this kind of element as child")
                            .build(),
                    ()->any(
                            // if is empty element from any class can be a child
                            ()->parentClass.getPermittedChildClass()==null || parentClass.getPermittedChildClass().isEmpty(),
                            // the class id of the child need to mach one within the list
                            ()->parentClass.getPermittedChildClass().stream().filter(c->c.compareTo(inventoryElementToSave.getClassId())==0).count()==1
                    )
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
                -7
        );
        log.info("User '{}' created new inventory element '{}[{}]' ", newlyCreatedElement.getCreatedBy(), newlyCreatedElement.getName(), inventoryDomainFound.getName());
        return newlyCreatedElement.getId();
    }

    /**
     * Update the inventory element
     *
     * @param domainId                  the domain id where the element belong
     * @param elementId                 the element unique identifier
     * @param updateInventoryElementDTO the information updatable
     */
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

        // update the model
        inventoryElementMapper.updateModel(
                inventoryElementToUpdate,
                updateInventoryElementDTO
        );

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
     * Return the full inventory domain
     *
     * @param domainId  the domain id
     * @param elementId the element id
     * @return the full inventory element
     */
    public InventoryElementDTO getFullElement(String domainId, String elementId) {
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
     * @param domainId the domain id
     * @param elementId the element id root for the child
     * @return the list of the summary of all the children
     */
    public List<InventoryElementSummaryDTO> getAllChildren(String domainId, String elementId){
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
}
