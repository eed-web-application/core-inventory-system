package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassAttributeDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassAttributeTypeDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
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

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryClassServiceTest {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    InventoryClassService inventoryClassService;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryClass.class);
    }

    @Test
    public void testCreateNewClass() {
        var newInventoryClassDTO = NewInventoryClassDTO
                .builder()
                .name("new class")
                .attributes(
                        List.of(
                                InventoryClassAttributeDTO
                                        .builder()
                                        .name("security level")
                                        .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                        .mandatory(true)
                                        .type(InventoryClassAttributeTypeDTO.String)
                                        .build()
                        )
                )
                .build();
        String newInventoryClassId = assertDoesNotThrow(
                () -> inventoryClassService.createNew(newInventoryClassDTO)
        );

        var foundInventoryDTO = assertDoesNotThrow(
                () -> inventoryClassService.findById(newInventoryClassId, false)
        );

        // check if the name has been normalized
        assertThat(foundInventoryDTO)
                .hasFieldOrPropertyWithValue(
                        "name", "new-class"
                );

        // check if the attribute name has been normalized
        assertThat(foundInventoryDTO.attributes())
                .hasSize(1)
                .contains(
                        InventoryClassAttributeDTO
                                .builder()
                                .name("security-level")
                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                .mandatory(true)
                                .type(InventoryClassAttributeTypeDTO.String)
                                .build()
                );
    }

    @Test
    public void testFailingSearchingNonExistingClassId() {
        InventoryClassNotFound notFoundException = assertThrows(
                InventoryClassNotFound.class,
                () -> inventoryClassService.findById("bad i", false)
        );
        assertThat(notFoundException.getErrorCode())
                .isEqualTo(-2);
    }

    @Test
    public void testInheritanceForSubclass() {
        var newRootClassId = assertDoesNotThrow(
                () -> inventoryClassService.createNew(NewInventoryClassDTO
                        .builder()
                        .name("new base class")
                        .attributes(
                                of(
                                        InventoryClassAttributeDTO
                                                .builder()
                                                .name("1")
                                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeTypeDTO.String)
                                                .build()
                                )
                        )
                        .build()
                )
        );
        assertThat(newRootClassId).isNotNull();
        //middle class
        var newMiddleClassIdOne = assertDoesNotThrow(
                () -> inventoryClassService.createNew(NewInventoryClassDTO
                        .builder()
                        .name("new middle class")
                        .extendsClass(of(newRootClassId))
                        .attributes(
                                of(
                                        InventoryClassAttributeDTO
                                                .builder()
                                                .name("2")
                                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeTypeDTO.String)
                                                .build(),
                                        InventoryClassAttributeDTO
                                                .builder()
                                                .name("3")
                                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeTypeDTO.String)
                                                .build()
                                )
                        )
                        .build()
                )
        );
        assertThat(newMiddleClassIdOne).isNotNull();

        //middle class
        var newMiddleClassIdTwo = assertDoesNotThrow(
                () -> inventoryClassService.createNew(NewInventoryClassDTO
                        .builder()
                        .name("new middle class")
                        .extendsClass(of(newRootClassId))
                        .attributes(
                                of(
                                        InventoryClassAttributeDTO
                                                .builder()
                                                .name("3")
                                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeTypeDTO.String)
                                                .build(),
                                        InventoryClassAttributeDTO
                                                .builder()
                                                .name("4")
                                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeTypeDTO.String)
                                                .build()
                                )
                        )
                        .build()
                )
        );
        assertThat(newMiddleClassIdTwo).isNotNull();

        var newFinalClassId = assertDoesNotThrow(
                () -> inventoryClassService.createNew(NewInventoryClassDTO
                        .builder()
                        .name("new base class")
                        .extendsClass(of(newMiddleClassIdTwo, newMiddleClassIdOne))
                        .attributes(
                                of(
                                        InventoryClassAttributeDTO
                                                .builder()
                                                .name("5")
                                                .description("Indicate what is the security level of the building choosing from [green, yellow, red]")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeTypeDTO.String)
                                                .build()
                                )
                        )
                        .build()
                )
        );
        assertThat(newFinalClassId).isNotNull();

        var fullInventoryClass = assertDoesNotThrow(
                ()->inventoryClassService.findById(newFinalClassId, true)
        );

        assertThat(fullInventoryClass.attributes())
                .hasSize(5)
                .extracting(InventoryClassAttributeDTO::name)
                .contains("1","2","3","4","5");
        assertThat(fullInventoryClass.extendsClass())
                .hasSize(3)
                .contains(newRootClassId, newMiddleClassIdOne, newMiddleClassIdTwo);
    }
}
