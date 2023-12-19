package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.exception.NotAuthorized;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassSummaryDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryElementAttributeHistoryDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.service.InventoryClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;

@RestController()
@RequestMapping("/v1/inventory/class")
@AllArgsConstructor
@Schema(description = "Set of api for the inventory class management")
public class InventoryClassController {
    private final AuthService authService;
    private final InventoryClassService inventoryClassService;

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory class")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResultResponse<String> createNewClass(
            Authentication authentication,
            @Valid @RequestBody NewInventoryClassDTO inventoryClassDTO
    ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryClassController::newAttachment")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication),
                // should be root
                () -> authService.checkForRoot(authentication)
        );
        return ApiResultResponse.of(
                inventoryClassService.createNew(inventoryClassDTO)
        );
    }

    @GetMapping(
            path = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Return found inventory class")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<InventoryClassDTO> finById(
            Authentication authentication,
            @PathVariable String id,
            @Parameter(name = "resolveInheritance", description = "Resolve the inheritance filling filed from the extended class")
            @RequestParam(value = "resolveInheritance", defaultValue = "false") Optional<Boolean> resolveInheritance
    ) {
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryClassController::newAttachment")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication)
        );
        return ApiResultResponse.of(
                inventoryClassService.findById(id, resolveInheritance.orElse(false))
        );
    }


    @GetMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "return the found class")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<List<InventoryClassSummaryDTO>> findAll(
            Authentication authentication,
            @Parameter(name = "search", description = "Search by filtering on cass name that contains the input string")
            @RequestParam(value = "search") Optional<String> search
            ){
        // check for auth
        assertion(
                NotAuthorized.notAuthorizedBuilder()
                        .errorCode(-1)
                        .errorDomain("InventoryClassController::findAll")
                        .build(),
                // should be authenticated
                () -> authService.checkAuthentication(authentication)
        );
        return ApiResultResponse.of(
                inventoryClassService.findAll(search)
        );
    }
}
