package edu.stanford.slac.code_inventory_system.api.v1.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
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
public class InventoryClassControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TestControllerHelperService testControllerHelperService;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryClass.class);
    }

    @Test
    public void createAndGetNewClass() {
        var createNewClassResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryClassControllerCreateNew(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        NewInventoryClassDTO
                                .builder()
                                .name("Building 001")
                                .description("description")
                                .extendsClass(emptyList())
                                .permittedChildClass(emptyList())
                                .attributes(
                                        List.of(
                                                InventoryClassAttributeDTO
                                                        .builder()
                                                        .name("Security Level")
                                                        .description("Determinate the security level choosing form [Green, Yellow, Red]")
                                                        .type(InventoryClassAttributeTypeDTO.String)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(createNewClassResult.getErrorCode()).isEqualTo(0);
        assertThat(createNewClassResult.getPayload()).isNotNull();

        var findByIdResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryClassControllerFindById(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        createNewClassResult.getPayload()
                )
        );

        assertThat(findByIdResult.getErrorCode()).isEqualTo(0);
        assertThat(findByIdResult.getPayload()).isNotNull();
        assertThat(findByIdResult.getPayload().id()).isEqualTo(createNewClassResult.getPayload());
        assertThat(findByIdResult.getPayload().name()).isEqualTo("building-001");
        assertThat(findByIdResult.getPayload().attributes()).hasSize(1);
        assertThat(findByIdResult.getPayload().attributes().get(0).name()).isEqualTo("security-level");
        assertThat(findByIdResult.getPayload().attributes().get(0).type()).isEqualTo(InventoryClassAttributeTypeDTO.String);
    }

    @Test
    public void findClassFailsWithNonExistingId() {
        var classNotfound = assertThrows(
                InventoryClassNotFound.class,
                () -> testControllerHelperService.inventoryClassControllerFindById(
                        mockMvc,
                        status().isNotFound(),
                        Optional.of("user1@slac.stanford.edu"),
                        "bad id"
                )
        );
        assertThat(classNotfound.getErrorCode()).isEqualTo(-2);
    }

    @Test
    public void findAllClasses() {
        for (int idx = 0; idx < 50; idx++) {
            int finalIdx = idx;
            var createNewClassResult = assertDoesNotThrow(
                    () -> testControllerHelperService.inventoryClassControllerCreateNew(
                            mockMvc,
                            status().isCreated(),
                            Optional.of("user1@slac.stanford.edu"),
                            NewInventoryClassDTO
                                    .builder()
                                    .name("Building %03d".formatted(finalIdx))
                                    .description("description")
                                    .extendsClass(emptyList())
                                    .permittedChildClass(emptyList())
                                    .attributes(
                                            List.of(
                                                    InventoryClassAttributeDTO
                                                            .builder()
                                                            .name("Security Level")
                                                            .description("Determinate the security level choosing form [Green, Yellow, Red]")
                                                            .type(InventoryClassAttributeTypeDTO.String)
                                                            .build()
                                            )
                                    )
                                    .build()
                    )
            );

            assertThat(createNewClassResult.getErrorCode()).isEqualTo(0);
            assertThat(createNewClassResult.getPayload()).isNotNull();
        }

        // find all class
        var findAllClassResult = assertDoesNotThrow(
                () -> testControllerHelperService.inventoryClassControllerFindAll(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu")
                )
        );
        assertThat(findAllClassResult.getErrorCode()).isEqualTo(0);
        assertThat(findAllClassResult.getPayload()).hasSize(50);
    }
}
