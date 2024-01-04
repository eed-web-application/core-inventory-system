package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.utility.StringUtilities;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.*;

import static edu.stanford.slac.ad.eed.baselib.utility.StringUtilities.normalizeStringWithReplace;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
public class BuildEnvironmentForElementTest {
    @Autowired
    TestControllerHelperService testControllerHelperService;

    public BuildEnvironmentForElementTest(TestControllerHelperService testControllerHelperService) {
        this.testControllerHelperService = testControllerHelperService;
    }

    /**
     * Build a complete domain for the element test a class environment
     */
    public EnvironmentResult buildEnvironment(
            MockMvc mockMvc,
            Optional<String> userInfo,
            List<NewInventoryClassDTO> classes,
            NewInventoryDomainDTO newInventoryDomainDTO
    ) {
        EnvironmentResult result = new EnvironmentResult();
        // create class

        for (NewInventoryClassDTO classDTO :
                classes) {
            if (!classDTO.implementedByClass().isEmpty()) {
                classDTO = classDTO.toBuilder()
                        .implementedByClass(
                                getIdsFromName(classDTO.implementedByClass(), result)
                        )
                        .build();
            }
            NewInventoryClassDTO finalClassDTO = classDTO;
            var createNewClassResult = assertDoesNotThrow(
                    () -> testControllerHelperService.inventoryClassControllerCreateNew(
                            mockMvc,
                            status().isCreated(),
                            userInfo,
                            finalClassDTO
                    )
            );
            assertThat(createNewClassResult.getErrorCode()).isEqualTo(0);
            result.classIds.put(normalizeStringWithReplace(classDTO.name(), " ", "-"), createNewClassResult.getPayload());
        }

        // create domain
        var createNewDomainResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerCreateNewDomain(
                        mockMvc,
                        status().isCreated(),
                        userInfo,
                        newInventoryDomainDTO
                )
        );
        assertThat(createNewDomainResult.getErrorCode()).isEqualTo(0);
        result.domainId = createNewDomainResult.getPayload();
        return result;
    }

    private static List<String> getIdsFromName(List<String> classNames, EnvironmentResult result) {
        List<String> implementationClassIds = new ArrayList<>();
        for (String implementedByClass : classNames) {
            if (!result.classIds.containsKey(normalizeStringWithReplace(implementedByClass, " ", "-"))) {
                throw new RuntimeException("Class " + implementedByClass + " not found");
            }
            String classId = result.classIds.get(normalizeStringWithReplace(implementedByClass, " ", "-"));
            implementationClassIds.add(classId);
        }
        return implementationClassIds;
    }

    /**
     * Build environment one
     *
     * @param mockMvc
     * @param userInfo
     * @return environment build info
     */
    public EnvironmentResult buildEnvironmentOne(
            MockMvc mockMvc,
            Optional<String> userInfo) {
        return buildEnvironment(
                mockMvc,
                userInfo,
                List.of(
                        NewInventoryClassDTO
                                .builder()
                                .name("Building")
                                .description("building")
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Building Number")
                                                        .description("The number that identify the building")
                                                        .type(InventoryClassAttributeTypeDTO.String)
                                                        .build(),
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Security Level")
                                                        .description("Determinate the security level choosing form [Green, Yellow, Red]")
                                                        .type(InventoryClassAttributeTypeDTO.String)
                                                        .build()
                                        )
                                )
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Experimental Facility")
                                .description("experimental facility")
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Building NUmber")
                                                        .description("The number that identify the building")
                                                        .type(InventoryClassAttributeTypeDTO.String)
                                                        .build(),
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Security Level")
                                                        .description("Determinate the security level choosing form [Green, Yellow, Red]")
                                                        .type(InventoryClassAttributeTypeDTO.String)
                                                        .build()
                                        )
                                )
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Floor")
                                .description("simple floor")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Computing Room")
                                .description("simple floor")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Office")
                                .description("simple office")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Laboratory")
                                .description("laboratory")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Server room")
                                .description("server room")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Commercial Server")
                                .description("Commercial server")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(emptyList())
                                .permittedChildClass(emptyList())
                                .build(),
                        NewInventoryClassDTO
                                .builder()
                                .name("Server")
                                .description("Server that permit to run application")
                                .attributes(emptyList())
                                .extendsClass(emptyList())
                                .implementedByClass(List.of("Commercial Server"))
                                .permittedChildClass(emptyList())
                                .build()
                ),
                NewInventoryDomainDTO
                        .builder()
                        .name("domain-a")
                        .description("This is a test domain")
                        .tags(
                                List.of(
                                        TagDTO
                                                .builder()
                                                .name("tag-a")
                                                .build(),
                                        TagDTO
                                                .builder()
                                                .name("tag-b")
                                                .build()
                                )
                        )
                        .authorizations(emptyList())
                        .authenticationTokens(emptyList())
                        .build()
        );
    }

    public static class EnvironmentResult {
        public String domainId;

        public Map<String, String> classIds = new HashMap<>();
    }
}
