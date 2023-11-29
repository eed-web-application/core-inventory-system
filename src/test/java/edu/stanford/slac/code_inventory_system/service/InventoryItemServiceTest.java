package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.exception.InventoryElementNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
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

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryItemServiceTest {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    TestUtilityService testUtilityService;
    @Autowired
    InventoryClassService inventoryClassService;
    @Autowired
    InventoryElementService inventoryElementService;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryClass.class);
        mongoTemplate.remove(new Query(), InventoryDomain.class);
        mongoTemplate.remove(new Query(), InventoryElement.class);
    }

    @Test
    public void saveDomainOK() {
        String newDomainId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );
        assertThat(newDomainId)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    public void getDomainOK() {
        String newDomainId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );
        assertThat(newDomainId)
                .isNotNull()
                .isNotEmpty();

        var fullDomain = assertDoesNotThrow(
                () -> inventoryElementService.getFullDomain(newDomainId)
        );

        assertThat(fullDomain).isNotNull()
                .extracting(
                        InventoryDomainDTO::name
                ).isEqualTo(
                        "new-domain"
                );
    }

    @Test
    public void createElementFailsWithNotMandatoryData() {
        ControllerLogicException checkExceptionNoName = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .build()
                )
        );
        assertThat(checkExceptionNoName.getErrorCode()).isEqualTo(-1);
        checkExceptionNoName = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("name")
                                .build()
                )
        );
        assertThat(checkExceptionNoName.getErrorCode()).isEqualTo(-1);
        checkExceptionNoName = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("name")
                                .classId("cid")
                                .build()
                )
        );
        assertThat(checkExceptionNoName.getErrorCode()).isEqualTo(-1);
        checkExceptionNoName = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("name")
                                .domainId("did")
                                .build()
                )
        );
        assertThat(checkExceptionNoName.getErrorCode()).isEqualTo(-1);
    }

    @Test
    public void createNewElementOK() {
        String newClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("class a")
                                .type(InventoryClassTypeDTO.Building)
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Building Number")
                                                        .mandatory(true)
                                                        .type(InventoryClassAttributeTypeDTO.Number)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        String newDomainId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );

        String newElementId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control")
                                .description("Main control system building")
                                .classId(newClassID)
                                .domainId(newDomainId)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(newElementId).isNotNull().isNotEmpty();
    }

    @Test
    public void createNewElementWithParentOK() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
                                .type(InventoryClassTypeDTO.Building)
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Building Number")
                                                        .mandatory(true)
                                                        .type(InventoryClassAttributeTypeDTO.Number)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        String newRoomClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("room class a")
                                .type(InventoryClassTypeDTO.Building)
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Room Number")
                                                        .mandatory(true)
                                                        .type(InventoryClassAttributeTypeDTO.Number)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        String newDomainId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );

        String newRootElementId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .domainId(newDomainId)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(newRootElementId).isNotNull().isNotEmpty();

        String newParentElementId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("Room Control 1")
                                .description("Main control system building")
                                .classId(newRoomClassID)
                                .domainId(newDomainId)
                                .parentId(newRootElementId)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValue
                                                        .builder()
                                                        .name("room-number")
                                                        .value("101")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(newParentElementId).isNotNull().isNotEmpty();
    }

    @Test
    public void createNewElementWithParentFailWithBadParentOK() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
                                .type(InventoryClassTypeDTO.Building)
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Building Number")
                                                        .mandatory(true)
                                                        .type(InventoryClassAttributeTypeDTO.Number)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        String newDomainId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );

        InventoryElementNotFound parentNotFoundException = assertThrows(
                InventoryElementNotFound.class,
                () -> inventoryElementService.createNew(
                        NewInventoryElementDTO
                                .builder()
                                .name("Room Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .domainId(newDomainId)
                                .parentId("bad parent id")
                                .build()
                )
        );
        assertThat(parentNotFoundException.getErrorCode()).isEqualTo(-4);
    }
}
