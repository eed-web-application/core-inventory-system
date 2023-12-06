package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.config.AppProperties;
import edu.stanford.slac.ad.eed.baselib.exception.NotAuthorized;
import edu.stanford.slac.ad.eed.baselib.model.AuthenticationToken;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;

import static edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationOwnerTypeDTO.User;
import static edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationTypeDTO.Write;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryElementControllerElementTest {
    @Autowired
    AppProperties appProperties;
    @Autowired
    private AuthService authService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TestControllerHelperService testControllerHelperService;
    @Autowired
    private BuildEnvironmentForElementTest buildEnvironmentForElementTest;
    private BuildEnvironmentForElementTest.EnvironmentResult environmentBuildInfo = null;

    @BeforeAll
    public void createAuthorizationForTests() {
        mongoTemplate.remove(new Query(), Authorization.class);
        mongoTemplate.remove(new Query(), AuthenticationToken.class);
        appProperties.getRootUserList().clear();
        appProperties.getRootUserList().add("user1@slac.stanford.edu");
        authService.updateRootUser();

        // create some class
        mongoTemplate.remove(new Query(), InventoryClass.class);
        mongoTemplate.remove(new Query(), InventoryDomain.class);

        // create first environment
        environmentBuildInfo = buildEnvironmentForElementTest.buildEnvironmentOne(
                mockMvc,
                Optional.of("user1@slac.stanford.edu")
        );
    }

    @BeforeEach
    public void cleanAndPrepareDomainForTest() {
        mongoTemplate.remove(new Query(), InventoryElement.class);
    }

    @Test
    public void createElementOk() {
        var createElementResult = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerCreateNewElement(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Control System Building")
                                .description("Is the control system software engineer office and experimental lab building")
                                .classId(environmentBuildInfo.classIds.get("building"))
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build(),
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("security-level")
                                                        .value("Green")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(createElementResult.getErrorCode()).isEqualTo(0);
        assertThat(createElementResult.getPayload()).isNotNull();

        var findFullElementById = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerFindElementById(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createElementResult.getPayload()
                )
        );

        assertThat(findFullElementById.getErrorCode()).isEqualTo(0);
        assertThat(findFullElementById.getPayload()).isNotNull();
        assertThat(findFullElementById.getPayload().id()).isEqualTo(createElementResult.getPayload());
        assertThat(findFullElementById.getPayload().name()).isEqualTo("control-system-building");
    }

    @Test
    public void createElementWhitChildOk() {
        var createBuilding34Result = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerCreateNewElement(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Control System Building")
                                .description("Is the control system software engineer office and experimental lab building")
                                .classId(environmentBuildInfo.classIds.get("building"))
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build(),
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("security-level")
                                                        .value("Green")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(createBuilding34Result.getErrorCode()).isEqualTo(0);
        assertThat(createBuilding34Result.getPayload()).isNotNull();

        var createFloor1Building34Result = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerCreateNewElement(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("floor1")
                                .parentId(createBuilding34Result.getPayload())
                                .classId(environmentBuildInfo.classIds.get("floor"))
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(createFloor1Building34Result.getErrorCode()).isEqualTo(0);
        assertThat(createFloor1Building34Result.getPayload()).isNotNull();


        var findAllChildrenResult = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerFindAllChildrenByRootId(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createBuilding34Result.getPayload()
                )
        );

        assertThat(findAllChildrenResult.getErrorCode()).isEqualTo(0);
        assertThat(findAllChildrenResult.getPayload()).isNotNull();
        assertThat(findAllChildrenResult.getPayload()).hasSize(1);
        assertThat(findAllChildrenResult.getPayload().get(0).id()).isEqualTo(createFloor1Building34Result.getPayload());
    }

    @Test
    public void findAllTestSimple() {
        for(int idx = 0; idx < 5; idx++) {
            int finalIdx = idx;
            var createBuilding34Result = assertDoesNotThrow(
                    ()->testControllerHelperService.inventoryElementControllerCreateNewElement(
                            mockMvc,
                            status().isCreated(),
                            Optional.of("user1@slac.stanford.edu"),
                            environmentBuildInfo.domainId,
                            NewInventoryElementDTO
                                    .builder()
                                    .name("Building %d".formatted(finalIdx))
                                    .description("Is the control system software engineer office and experimental lab building")
                                    .classId(environmentBuildInfo.classIds.get("building"))
                                    .attributes(
                                            List.of(
                                                    InventoryElementAttributeValue
                                                            .builder()
                                                            .name("building-number")
                                                            .value(String.valueOf(finalIdx))
                                                            .build(),
                                                    InventoryElementAttributeValue
                                                            .builder()
                                                            .name("security-level")
                                                            .value("Green")
                                                            .build()
                                            )
                                    )
                                    .build()
                    )
            );

            assertThat(createBuilding34Result.getErrorCode()).isEqualTo(0);
            assertThat(createBuilding34Result.getPayload()).isNotNull();

            var createFloor1Building34Result = assertDoesNotThrow(
                    ()->testControllerHelperService.inventoryElementControllerCreateNewElement(
                            mockMvc,
                            status().isCreated(),
                            Optional.of("user1@slac.stanford.edu"),
                            environmentBuildInfo.domainId,
                            NewInventoryElementDTO
                                    .builder()
                                    .name("floor1")
                                    .parentId(createBuilding34Result.getPayload())
                                    .classId(environmentBuildInfo.classIds.get("floor"))
                                    .attributes(emptyList())
                                    .build()
                    )
            );

            assertThat(createFloor1Building34Result.getErrorCode()).isEqualTo(0);
            assertThat(createFloor1Building34Result.getPayload()).isNotNull();
        }

    }
}
