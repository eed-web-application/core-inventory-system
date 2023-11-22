package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.exception.NotAuthorized;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.service.InventoryClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;

@RestController()
@RequestMapping("/v1/inventory/class")
@AllArgsConstructor
@Schema(description = "Set of api for the inventory class management")
public class InventoryClassController {
    AuthService authService;
    InventoryClassService inventoryClassService;

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a new inventory class")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResultResponse<String> createNewClass(
            Authentication authentication,
            @RequestBody NewInventoryClassDTO inventoryClassDTO
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
    @Operation(summary = "return found inventory class")
    @ResponseStatus(HttpStatus.OK)
    public ApiResultResponse<InventoryClassDTO> finById(
            Authentication authentication,
            @PathVariable String id
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
                inventoryClassService.findById(id)
        );
    }
}
