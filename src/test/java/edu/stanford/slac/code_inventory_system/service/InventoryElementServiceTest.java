package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.ad.eed.baselib.model.AuthenticationToken;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.api.v1.mapper.InventoryClassMapper;
import edu.stanford.slac.code_inventory_system.exception.InventoryDomainAlreadyExists;
import edu.stanford.slac.code_inventory_system.exception.InventoryElementNotFound;
import edu.stanford.slac.code_inventory_system.exception.TagNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.InventoryElementAttributeHistory;
import edu.stanford.slac.code_inventory_system.repository.InventoryElementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationOwnerTypeDTO.User;
import static edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationTypeDTO.Write;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryElementServiceTest {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    TestUtilityService testUtilityService;
    @Autowired
    InventoryClassService inventoryClassService;
    @Autowired
    InventoryElementService inventoryElementService;
    @Autowired
    InventoryClassMapper inventoryClassMapper;
    @Autowired
    @SpyBean
    InventoryElementRepository inventoryElementRepository;

    @BeforeEach
    public void cleanCollection() {
        Mockito.reset(inventoryElementRepository);
        mongoTemplate.remove(new Query(), Authorization.class);
        mongoTemplate.remove(new Query(), AuthenticationToken.class);
        mongoTemplate.remove(new Query(), InventoryClass.class);
        mongoTemplate.remove(new Query(), InventoryDomain.class);
        mongoTemplate.remove(new Query(), InventoryElement.class);
        mongoTemplate.remove(new Query(), InventoryElementAttributeHistory.class);
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
    public void saveDomainFailWithSameName() {
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
        InventoryDomainAlreadyExists exceptionForDomainWithSameName = assertThrows(
                InventoryDomainAlreadyExists.class,
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );
        assertThat(exceptionForDomainWithSameName)
                .isNotNull();
        assertThat(exceptionForDomainWithSameName.getErrorCode())
                .isEqualTo(-1);
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
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );

        assertThat(fullDomain).isNotNull()
                .extracting(
                        InventoryDomainDTO::name
                ).isEqualTo(
                        "new-domain"
                );
    }

    @Test
    public void updateDomainWithTag() {
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

        // update domain with tags
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .name("new-domain")
                                .description("Update the description")
                                .tags(
                                        List.of(
                                                TagDTO
                                                        .builder()
                                                        .name("New tag")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        var updatedDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );
        // check for tag
        assertThat(updatedDomain.description()).contains("Update the description");
        assertThat(updatedDomain.tags())
                .hasSize(1)
                .extracting(TagDTO::name)
                .contains("new-tag");

        // now update the tag
        InventoryDomainDTO finalUpdatedDomain = updatedDomain;
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .name("new-domain")
                                .description("Update the description")
                                .tags(
                                        List.of(
                                                finalUpdatedDomain.tags().get(0)
                                                        .toBuilder()
                                                        .name("Updated tag name")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        updatedDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );
        // check for tag
        assertThat(updatedDomain.description()).contains("Update the description");
        assertThat(updatedDomain.tags())
                .hasSize(1)
                .extracting(TagDTO::name)
                .contains("updated-tag-name");

        // now delete
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .name("new-domain")
                                .description("Update the description")
                                .tags(
                                        List.of(
                                        )
                                )
                                .build()
                )
        );

        updatedDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );
        // check for tag
        assertThat(updatedDomain.description()).contains("Update the description");
        assertThat(updatedDomain.tags())
                .hasSize(0);
    }

    @Test
    public void updateDomainWithAuthorization() {
        String newDomainId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        NewInventoryDomainDTO
                                .builder()
                                .name("New Domain")
                                .description("This is the description for the new domain")
                                .build()
                )
        );
        // update adding the authorization
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .name("lcls-inventory-updated")
                                .description("Inventory for the LCLS updated")
                                .tags(emptyList())
                                .authorizations(
                                        List.of(
                                                AuthorizationDTO
                                                        .builder()
                                                        .authorizationType(Write)
                                                        .owner("user2@slac.stanford.edu")
                                                        .ownerType(User)
                                                        .build(),
                                                AuthorizationDTO
                                                        .builder()
                                                        .authorizationType(Write)
                                                        .owner("user3@slac.stanford.edu")
                                                        .ownerType(User)
                                                        .build()
                                        )
                                )
                                .authenticationTokens(emptyList())
                                .build()
                )
        );

        var updatedDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );
        // check for tag
        assertThat(updatedDomain.authorizations())
                .hasSize(2);

        // update removing one of the update authorization
        InventoryDomainDTO finalUpdatedDomain = updatedDomain;
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .name("lcls-inventory-updated")
                                .description("Inventory for the LCLS updated")
                                .tags(emptyList())
                                .authorizations(
                                        List.of(
                                                finalUpdatedDomain.authorizations().get(0)
                                        )
                                )
                                .build()
                )
        );

        updatedDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );

        // check that has been removed the second authorization
        assertThat(updatedDomain.authorizations())
                .hasSize(1)
                .extracting(AuthorizationDTO::id)
                .contains(
                        updatedDomain.authorizations().get(0).id()
                );

        // update removing all the authorization
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .name("lcls-inventory-updated")
                                .description("Inventory for the LCLS updated")
                                .tags(emptyList())
                                .authorizations(emptyList())
                                .build()
                )
        );

        updatedDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );

        // check that has been removed the second authorization
        assertThat(updatedDomain.authorizations())
                .hasSize(0);
    }

    @Test
    public void createElementFailsWithNotMandatoryData() {
        ControllerLogicException checkExceptionNoName = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        null,
                        NewInventoryElementDTO
                                .builder()
                                .build()
                )
        );
        assertThat(checkExceptionNoName.getErrorCode()).isEqualTo(-1);
        checkExceptionNoName = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        null,
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
                        null,
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
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .name("name")
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control")
                                .description("Main control system building")
                                .classId(newClassID)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
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
    public void createNewImplementationElementOK() {
        String newImplClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("impl class a")
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

        String newClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("class a")
                                .implementedByClass(List.of(newImplClassID))
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Implemented item")
                                .description("Main control system building")
                                .classId(newClassID)
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(newElementId).isNotNull().isNotEmpty();

        // implements the new created elements
        String newImplementationElementId = assertDoesNotThrow(
                () -> inventoryElementService.createNewImplementation(
                        newDomainId,
                        newElementId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Implementation")
                                .description("Main control system building")
                                .classId(newImplClassID)
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(newImplementationElementId).isNotNull().isNotEmpty();

        // the implemented by id should be equals to newImplementationElementId
        var fullInventoryElement = assertDoesNotThrow(
                ()->inventoryElementService.getInventoryElementByDomainIdAndElementId(newDomainId, newElementId)
        );
        assertThat(fullInventoryElement.implementedBy()).isEqualTo(newImplementationElementId);

        // test find all implementation
        var allImplementationHistory = assertDoesNotThrow(
                ()->inventoryElementService.findAllImplementationForDomainAndElementIds(newDomainId, newElementId)
        );
        assertThat(allImplementationHistory)
                .hasSize(1)
                .extracting(InventoryElementSummaryDTO::id)
                .contains(newImplementationElementId);
    }

    @Test
    public void createNewImplementationElementFailWrongImplClassId() {
        String newImplClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("impl class a")
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

        String newClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("class a")
                                .implementedByClass(List.of(newImplClassID))
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Implemented item")
                                .description("Main control system building")
                                .classId(newClassID)
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(newElementId).isNotNull().isNotEmpty();

        // implements the new created elements
        ControllerLogicException exceptionOnWrongClassId = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNewImplementation(
                        newDomainId,
                        newElementId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Implementation")
                                .description("Main control system building")
                                .classId("wrong-id")
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(exceptionOnWrongClassId.getErrorCode()).isEqualTo(-4);
    }

    @Test
    public void createNewImplementationElementSaveOnTransactionError() {
        String newImplClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("impl class a")
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

        String newClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("class a")
                                .implementedByClass(List.of(newImplClassID))
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Implemented item")
                                .description("Main control system building")
                                .classId(newClassID)
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(newElementId).isNotNull().isNotEmpty();
        //simulate that something is gone wrong saving on database the new attribute
        Mockito.doThrow(new RuntimeException()).when(inventoryElementRepository)
                .save(any());
        // implements the new created elements
        ControllerLogicException exceptionOnWrongClassId = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNewImplementation(
                        newDomainId,
                        newElementId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Implementation")
                                .description("Main control system building")
                                .classId(newImplClassID)
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(exceptionOnWrongClassId.getErrorCode()).isEqualTo(-7);

        // get the implemented element that should not have the implementedBy populated
        var fullInventoryElement = assertDoesNotThrow(
                ()->inventoryElementService.getInventoryElementByDomainIdAndElementId(newDomainId, newElementId)
        );
        assertThat(fullInventoryElement.implementedBy()).isNull();
    }

    @Test
    public void createNewElementWithParentOK() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
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

        String newChildElementId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Room Control 1")
                                .description("Main control system building")
                                .classId(newRoomClassID)
                                .parentId(newRootElementId)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("room-number")
                                                        .value("101")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(newChildElementId).isNotNull().isNotEmpty();

        var createdChildElement = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryElementByDomainIdAndElementId(
                        newDomainId,
                        newChildElementId
                )
        );

        assertThat(createdChildElement).isNotNull();
        assertThat(createdChildElement.parentId()).isNotNull().isEqualTo(newRootElementId);
    }

    @Test
    public void createNewElementWithParentFailWithWrongChildClass() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
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

        String newFloorClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("Normal Floor")
                                .attributes(emptyList())
                                .build()
                )
        );

        // update building adding floor as child
        InventoryClassDTO inventoryClassDTOBuildingToUpdate = assertDoesNotThrow(
                ()->inventoryClassService.findById(newBuildingClassID, false)
        );
        UpdateInventoryClassDTO uicDTO =  inventoryClassMapper.toUpdate(inventoryClassDTOBuildingToUpdate);
        boolean updateResult = assertDoesNotThrow(
                ()->inventoryClassService.update(
                        newBuildingClassID,
                        uicDTO
                                .toBuilder()
                                .permittedChildClass(
                                        List.of(newFloorClassID)
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
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
        // this should fail because the room class is not a
        // possible child class
        ControllerLogicException newParentElementId = assertThrows(
                ControllerLogicException.class,
                () -> inventoryElementService.createNew(
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Room Control 1")
                                .description("Main control system building")
                                .classId(newRoomClassID)
                                .parentId(newRootElementId)
                                .attributes(emptyList())
                                .build()
                )
        );

        assertThat(newParentElementId.getErrorCode()).isEqualTo(-8);
    }

    @Test
    public void createNewElementWithParentFailWithBadParentOK() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
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
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Room Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .parentId("bad parent id")
                                .build()
                )
        );
        assertThat(parentNotFoundException.getErrorCode()).isEqualTo(-5);
    }

    @Test
    public void errorWithNoFoundTagInDomain() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
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

        TagNotFound tagNotFoundError = assertThrows(
                TagNotFound.class,
                () -> inventoryElementService.createNew(
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .tags(
                                        List.of(
                                                "bad tag id"
                                        )
                                )
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(tagNotFoundError.getErrorCode()).isEqualTo(-4);
    }

    @Test
    public void updateElementOk() {
        String newBuildingClassID = assertDoesNotThrow(
                () -> inventoryClassService.createNew(
                        NewInventoryClassDTO
                                .builder()
                                .name("building class a")
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

        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        UpdateDomainDTO
                                .builder()
                                .description("Updated description")
                                .tags(
                                        List.of(
                                                TagDTO
                                                        .builder()
                                                        .name("tag a")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        var fullDomain = assertDoesNotThrow(
                () -> inventoryElementService.getInventoryDomainById(newDomainId)
        );
        // check tag tags are presents
        assertThat(fullDomain.tags()).hasSize(1);

        var newElementId = assertDoesNotThrow(
                () -> inventoryElementService.createNew(
                        newDomainId,
                        NewInventoryElementDTO
                                .builder()
                                .name("Building Control 1")
                                .description("Main control system building")
                                .classId(newBuildingClassID)
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("34")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(newElementId)
                .isNotNull()
                .isNotEmpty();

        // update the inventory element
        assertDoesNotThrow(
                () -> inventoryElementService.update(
                        newDomainId,
                        newElementId,
                        UpdateInventoryElementDTO
                                .builder()
                                .description("updated description")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("building-number")
                                                        .value("43")
                                                        .build()
                                        )
                                )
                                .tags(
                                        List.of(
                                                fullDomain.tags().get(0).id()
                                        )
                                )
                                .build()
                )
        );
        var fullElementRead = assertDoesNotThrow(
                () ->inventoryElementService.getInventoryElementByDomainIdAndElementId(
                        newDomainId,
                        newElementId
                )
        );
        assertThat(fullElementRead).isNotNull();
        assertThat(fullElementRead.id()).isEqualTo(newElementId);
        assertThat(fullElementRead.classDTO().id()).isEqualTo(newBuildingClassID);
        assertThat(fullElementRead.domainDTO().id()).isEqualTo(newDomainId);
        assertThat(fullElementRead.description()).isEqualTo("updated description");
        assertThat(fullElementRead.tags())
                .hasSize(1)
                .extracting(TagDTO::name).contains("tag-a");

        // check the history of the attribute
        var fullAttributeHistory = assertDoesNotThrow(
                () ->inventoryElementService.findAllAttributeHistory(
                        newDomainId,
                        newElementId
                )
        );
        assertThat(fullAttributeHistory)
                .isNotNull()
                .hasSize(1)
                .extracting(
                        InventoryElementAttributeHistoryDTO::getValue
                )
                .extracting(InventoryElementAttributeValueDTO::value)
                .contains("34");
    }
}
