package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.utility.StringUtilities;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryElementMapper;
import edu.stanford.slac.code_inventory_system.exception.InventoryDomainNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.repository.InventoryDomainRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;
import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;

/**
 * HIgh level API for the management of the inventory domain and item
 */
@Service
@AllArgsConstructor
public class InventoryElementService {
    InventoryElementMapper inventoryElementMapper;
    InventoryDomainRepository inventoryDomainRepository;
    InventoryElementRepository inventoryElementRepository;
    /**
     * Create new inventory domain, after the name normalization
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
    public InventoryDomainDTO getFullDomain(String domainId){
        var newlyCreatedDomain = wrapCatch(
                () -> inventoryDomainRepository.findById(
                        domainId
                ),
                -1
        ).orElseThrow(
                ()-> InventoryDomainNotFound
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
     * @param newInventoryElementDTO is the new inventory item to create
     */
    public void createNew(NewInventoryElementDTO newInventoryElementDTO){
        InventoryElement newElement = inventoryElementMapper.toModel(newInventoryElementDTO);
    }
}
