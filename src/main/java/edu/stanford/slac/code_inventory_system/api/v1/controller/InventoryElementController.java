package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationTypeDTO;
import edu.stanford.slac.ad.eed.baselib.exception.NotAuthorized;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.UpdateDomainDTO;
import edu.stanford.slac.code_inventory_system.service.InventoryClassService;
import edu.stanford.slac.code_inventory_system.service.InventoryElementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;

@RestController()
@RequestMapping("/v1/inventory/element")
@AllArgsConstructor
@Schema(description = "Set of api for the inventory element management")
public class InventoryElementController {
    private final AuthService authService;
    private final InventoryElementService inventoryElementService;

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory domain")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResultResponse<String> createNewDomain(
            Authentication authentication,
            @Valid @RequestBody NewInventoryDomainDTO inventoryDomainDTO
    ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::createNewDomain")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                // should be root for create new domain
                () -> authService.checkForRoot(authentication)
        );
        return ApiResultResponse.of(
                inventoryElementService.createNew(inventoryDomainDTO)
        );
    }

    @GetMapping(
            path = "/{domainId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory domain")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<InventoryDomainDTO> getFullDomain(
            Authentication authentication,
            @PathVariable(name = "domainId") String domainId
    ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::createNewDomain")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                // should be root for create new domain
                () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                        authentication,
                        AuthorizationTypeDTO.Read,
                        "/cis/domain/%s".formatted(domainId))
        );
        return ApiResultResponse.of(
                inventoryElementService.getFullDomain(domainId)
        );
    }

    @PutMapping(
            path = "/{domainId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory domain")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<Boolean> updateDomain(
            Authentication authentication,
            @PathVariable(name = "domainId") String domainId,
            @Valid @RequestBody UpdateDomainDTO updateDomainDTO
            ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::createNewDomain")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                // should be root for create new domain
                () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                        authentication,
                        // only admin can update the domain
                        AuthorizationTypeDTO.Admin,
                        "/cis/domain/%s".formatted(domainId))
        );
        // update the domain
        inventoryElementService.update(domainId, updateDomainDTO);
        return ApiResultResponse.of(
                true
        );
    }
}
