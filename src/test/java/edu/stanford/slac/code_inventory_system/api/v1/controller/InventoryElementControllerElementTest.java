package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.config.AppProperties;
import edu.stanford.slac.ad.eed.baselib.model.AuthenticationToken;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryElementAttributeHistoryDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryElementAttributeValueDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.UpdateInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.InventoryElementAttributeHistory;
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

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        mongoTemplate.remove(new Query(), InventoryElementAttributeHistory.class);
    }

    @Test
    public void createElementOk() {
        var createElementResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
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
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build(),
                                                InventoryElementAttributeValueDTO
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
                () -> testControllerHelperService.inventoryElementControllerFindElementById(
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
    public void updateElementOk() {
        var createElementResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
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
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build(),
                                                InventoryElementAttributeValueDTO
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

        var updatedElementResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerUpdateElement(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createElementResult.getPayload(),
                        UpdateInventoryElementDTO
                                .builder()
                                .description("Is the control system software engineer office and experimental lab building")
                                .tags(emptyList())
                                .attributes(
                                        of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("43")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(updatedElementResult.getErrorCode()).isEqualTo(0);
        assertThat(updatedElementResult.getPayload()).isTrue();

        // check history on attributes
        var elementHistoryResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerFindAttributeHistory(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createElementResult.getPayload()
                )
        );
        assertThat(elementHistoryResult).isNotNull();
        assertThat(elementHistoryResult.getPayload())
                .hasSize(2)
                .extracting(InventoryElementAttributeHistoryDTO::getValue)
                .extracting(InventoryElementAttributeValueDTO::name)
                .contains("building-number","security-level");
        assertThat(elementHistoryResult.getPayload())
                .hasSize(2)
                .extracting(InventoryElementAttributeHistoryDTO::getValue)
                .extracting(InventoryElementAttributeValueDTO::value)
                .contains("34","Green");
    }

    @Test
    public void createImplementationElementOk() {
        var createLogicalServerResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Logical Server")
                                .description("logical server")
                                .classId(environmentBuildInfo.classIds.get("server"))
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(createLogicalServerResult.getErrorCode()).isEqualTo(0);
        assertThat(createLogicalServerResult.getPayload()).isNotNull();

        // create new implementation of the logical server with real one
        var newServerImplementationId = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerNewImplementationElement(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createLogicalServerResult.getPayload(),
                        NewInventoryElementDTO
                                .builder()
                                .name("New Dell Server")
                                .description("physical server")
                                .classId(environmentBuildInfo.classIds.get("commercial-server"))
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(newServerImplementationId.getErrorCode()).isEqualTo(0);
        assertThat(newServerImplementationId.getPayload()).isNotNull();

        var fetchImplementedElementDTOResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerFindElementById(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createLogicalServerResult.getPayload()
                )
        );
        assertThat(fetchImplementedElementDTOResult.getErrorCode()).isEqualTo(0);
        assertThat(fetchImplementedElementDTOResult.getPayload()).isNotNull();
        assertThat(fetchImplementedElementDTOResult.getPayload().implementedBy()).isEqualTo(newServerImplementationId.getPayload());

        // find all implementations
        var findAllImplementationResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerFindAllImplementationHistory(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        createLogicalServerResult.getPayload()
                )
        );
        assertThat(findAllImplementationResult.getErrorCode()).isEqualTo(0);
        assertThat(findAllImplementationResult.getPayload()).hasSize(1);
        assertThat(findAllImplementationResult.getPayload().get(0).id()).isEqualTo(newServerImplementationId.getPayload());
    }

    @Test
    public void createElementWhitChildOk() {
        var createBuilding34Result = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
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
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build(),
                                                InventoryElementAttributeValueDTO
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
                () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
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
                () -> testControllerHelperService.inventoryElementControllerFindAllChildrenByRootId(
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
        for (int idx = 0; idx < 20; idx++) {
            int finalIdx = idx;
            var createBuilding34Result = assertDoesNotThrow(
                    () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
                            mockMvc,
                            status().isCreated(),
                            Optional.of("user1@slac.stanford.edu"),
                            environmentBuildInfo.domainId,
                            NewInventoryElementDTO
                                    .builder()
                                    .name(" %03d Building".formatted(finalIdx))
                                    .description("Is the control system software engineer office and experimental lab building")
                                    .classId(environmentBuildInfo.classIds.get("building"))
                                    .attributes(
                                            List.of(
                                                    InventoryElementAttributeValueDTO
                                                            .builder()
                                                            .name("building-number")
                                                            .value(String.valueOf(finalIdx))
                                                            .build(),
                                                    InventoryElementAttributeValueDTO
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
                    () -> testControllerHelperService.inventoryElementControllerCreateNewElement(
                            mockMvc,
                            status().isCreated(),
                            Optional.of("user1@slac.stanford.edu"),
                            environmentBuildInfo.domainId,
                            NewInventoryElementDTO
                                    .builder()
                                    .name("%03d floor".formatted(finalIdx))
                                    .parentId(createBuilding34Result.getPayload())
                                    .classId(environmentBuildInfo.classIds.get("floor"))
                                    .attributes(emptyList())
                                    .build()
                    )
            );

            assertThat(createFloor1Building34Result.getErrorCode()).isEqualTo(0);
            assertThat(createFloor1Building34Result.getPayload()).isNotNull();
        }

        var searchResultForward = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerFindAllElements(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        Optional.empty(),
                        Optional.of(10),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                )
        );
        assertThat(searchResultForward.getErrorCode()).isEqualTo(0);
        assertThat(searchResultForward.getPayload()).hasSize(10);

        var searchResultBackward = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryElementControllerFindAllElements(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        environmentBuildInfo.domainId,
                        Optional.of(searchResultForward.getPayload().get(9).id()),
                        Optional.of(0),
                        Optional.of(10),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                )
        );
        assertThat(searchResultBackward.getErrorCode()).isEqualTo(0);
        assertThat(searchResultBackward.getPayload()).hasSize(10);
    }
}
