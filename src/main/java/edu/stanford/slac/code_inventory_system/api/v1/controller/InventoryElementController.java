package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationTypeDTO;
import edu.stanford.slac.ad.eed.baselib.exception.NotAuthorized;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.service.InventoryElementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.any;
import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;

@Validated
@RestController()
@RequestMapping("/v1/inventory")
@AllArgsConstructor
@Schema(description = "Set of api for the inventory element management")
public class InventoryElementController {
    private final AuthService authService;
    private final InventoryElementService inventoryElementService;

    @PostMapping(
            path = "/domain",
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
            path = "/domain/{domainId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory domain")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<InventoryDomainDTO> findDomainById(
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
                // should be a reader to get the domain information
                () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                        authentication,
                        AuthorizationTypeDTO.Read,
                        "/cis/domain/%s".formatted(domainId))
        );
        return ApiResultResponse.of(
                inventoryElementService.getFullDomain(domainId)
        );
    }

    @GetMapping(
            path = "/domain",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory domain")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<List<InventoryDomainSummaryDTO>> findAllDomain(
            Authentication authentication
    ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::createNewDomain")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication)
        );
        return ApiResultResponse.of(
                inventoryElementService.findAllDomain()
        );
    }

    @PutMapping(
            path = "/domain/{domainId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Return a full inventory domain")
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
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or an admin  for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Admin,
                                "/cis/domain/%s".formatted(domainId))
                )

        );
        // update the domain
        inventoryElementService.update(domainId, updateDomainDTO);
        return ApiResultResponse.of(
                true
        );
    }



    @PostMapping(
            path = "/domain/{domainId}/element",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory element")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResultResponse<String> createNewElement(
            Authentication authentication,
            @PathVariable(name = "domainId") String domainId,
            @Valid @RequestBody NewInventoryElementDTO newInventoryElementDTO
            ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::createNewDomain")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or a writer for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Write,
                                "/cis/domain/%s".formatted(domainId))
                )
        );
        return ApiResultResponse.of(
                inventoryElementService.createNew(domainId, newInventoryElementDTO)
        );
    }

    @PutMapping(
            path = "/domain/{domainId}/element/{elementId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Update an inventory element")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<Boolean> updateElement(
            Authentication authentication,
            @PathVariable(name = "domainId") String domainId,
            @PathVariable(name = "elementId") String elementId,
            @Valid @RequestBody UpdateInventoryElementDTO updateInventoryElementDTO
    ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::createNewDomain")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or a writer for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Write,
                                "/cis/domain/%s".formatted(domainId))
                )
        );
        // update inventory element
        inventoryElementService.update(
                domainId,
                elementId,
                updateInventoryElementDTO
        );
        return ApiResultResponse.of(true);
    }

    @GetMapping(
            path = "/domain/{domainId}/element/{elementId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Update an inventory element")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<InventoryElementDTO> findElementById(
            Authentication authentication,
            @PathVariable(name = "domainId") String domainId,
            @PathVariable(name = "elementId") String elementId
    ){
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::getElement")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or a writer for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Read,
                                "/cis/domain/%s".formatted(domainId))
                )
        );
        return ApiResultResponse.of(
                inventoryElementService.getFullElement(domainId, elementId)
        );
    }


    @GetMapping(
            path = "/domain/{domainId}/element/{elementId}/attributes/history",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Return all history for the element attributes")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<List<InventoryElementAttributeHistoryDTO>> findAllAttributeHistory(
            Authentication authentication,
            @Parameter(name = "domainId", description = "The domain id that own the element")
            @PathVariable(value = "domainId") String domainId,
            @Parameter(name = "elementId", description = "The element id that own the attribute")
            @PathVariable(value = "elementId") String elementId
    ){
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::findAllElements")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or a writer for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Read,
                                "/cis/domain/%s".formatted(domainId))
                )
        );
        return ApiResultResponse.of(
                inventoryElementService.findAllAttributeHistory(domainId, elementId)
        );
    }

    @GetMapping(
            path = "/domain/{domainId}/element/{elementId}/children",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Update an inventory element")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<List<InventoryElementSummaryDTO>> findChildrenByRootId(
            Authentication authentication,
            @PathVariable(name = "domainId") String domainId,
            @PathVariable(name = "elementId") String elementId
    ){
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::getElement")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or a writer for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Read,
                                "/cis/domain/%s".formatted(domainId))
                )
        );
        return ApiResultResponse.of(
                inventoryElementService.getAllChildren(domainId, elementId)
        );
    }

    @GetMapping(
            path = "/domain/{domainId}/element",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Update an inventory element")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<List<InventoryElementSummaryDTO>> findAllElements(
            Authentication authentication,
            @Parameter(name = "anchorId", description = "Is the domain id where  the search is applyed")
            @PathVariable(name = "domainId") String domainId,
            @Parameter(name = "anchorId", description = "Is the id of an entry from where start the search")
            @RequestParam("anchorId") Optional<String> anchorId,
            @Parameter(name = "contextSize", description = "Include this number of entries before the startDate (used for highlighting entries)")
            @RequestParam("contextSize") Optional<Integer> contextSize,
            @Parameter(name = "limit", description = "Limit the number the number of entries after the start date.")
            @RequestParam(value = "limit") Optional<Integer> limit,
            @Parameter(name = "search", description = "Typical search functionality")
            @RequestParam("search") Optional<String> search,
            @Parameter(name = "tags", description = "Only include entries that use one of these tags")
            @RequestParam("tags") Optional<List<String>> tags,
            @Parameter(name = "requireAllTags", description = "Require that all entries found includes all the tags")
            @RequestParam(value = "requireAllTags", defaultValue = "false") Optional<Boolean> requireAllTags
    ){
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryElementController::findAllElements")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                ()->any(
                        // should be root  for update the domain
                        () -> authService.checkForRoot(authentication),
                        // or a writer for update the domain
                        () -> authService.checkAuthorizationForOwnerAuthTypeAndResourcePrefix(
                                authentication,
                                // only admin can update the domain
                                AuthorizationTypeDTO.Read,
                                "/cis/domain/%s".formatted(domainId))
                )
        );
        return ApiResultResponse.of(
                inventoryElementService.findAllElements(
                        QueryParameterDTO
                                .builder()
                                .domainId(List.of(domainId))
                                .anchorID(anchorId.orElse(null))
                                .contextSize(contextSize.orElse(0))
                                .limit(limit.orElse(10))
                                .search(search.orElse(null))
                                .tags(tags.orElse(Collections.emptyList()))
                                .requireAllTags(requireAllTags.orElse(false))
                                .build()
                )
        );
    }
}
