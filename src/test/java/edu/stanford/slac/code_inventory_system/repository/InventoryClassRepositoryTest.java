package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttribute;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttributeType;
import edu.stanford.slac.code_inventory_system.model.InventoryClassType;
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

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryClassRepositoryTest {
    @Autowired
    private InventoryClassRepository inventoryClassRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryClass.class);
    }

    @Test
    public void saveOk() {
        InventoryClass ic = InventoryClass
                .builder()
                .name("Laboratory Building")
                .type(InventoryClassType.Building)
                .attributes(
                        List.of(
                                InventoryClassAttribute
                                        .builder()
                                        .name("Alias")
                                        .description("Is the simple code for describe the building")
                                        .mandatory(true)
                                        .type(InventoryClassAttributeType.String)
                                        .build(),
                                InventoryClassAttribute
                                        .builder()
                                        .name("Building access manager")
                                        .description("Is the access manage user identification")
                                        .mandatory(true)
                                        .type(InventoryClassAttributeType.String)
                                        .build(),
                                InventoryClassAttribute
                                        .builder()
                                        .name("Area access level")
                                        .description("Is the access level code that identify who can access the building")
                                        .mandatory(true)
                                        .type(InventoryClassAttributeType.String)
                                        .build()
                        )
                )
                .build();
        var savedIC = inventoryClassRepository.save(
                ic
        );

        assertThat(savedIC)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .isEqualTo(ic);
    }

    @Test
    public void testDistinctTypeQuery() {
        inventoryClassRepository.save(
                InventoryClass
                        .builder()
                        .name("Laboratory Building")
                        .type(InventoryClassType.Building)
                        .attributes(
                                List.of(
                                        InventoryClassAttribute
                                                .builder()
                                                .name("Alias")
                                                .description("Is the simple code for describe the building")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeType.String)
                                                .build(),
                                        InventoryClassAttribute
                                                .builder()
                                                .name("Building access manager")
                                                .description("Is the access manage user identification")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeType.String)
                                                .build(),
                                        InventoryClassAttribute
                                                .builder()
                                                .name("Area access level")
                                                .description("Is the access level code that identify who can access the building")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeType.String)
                                                .build()
                                )
                        )
                        .build()
        );
        inventoryClassRepository.save(
                InventoryClass
                        .builder()
                        .name("Laboratory Building")
                        .type(InventoryClassType.Room)
                        .attributes(
                                List.of(
                                        InventoryClassAttribute
                                                .builder()
                                                .name("Alias")
                                                .description("Is the simple code for describe the building")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeType.String)
                                                .build(),
                                        InventoryClassAttribute
                                                .builder()
                                                .name("Building access manager")
                                                .description("Is the access manage user identification")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeType.String)
                                                .build(),
                                        InventoryClassAttribute
                                                .builder()
                                                .name("Area access level")
                                                .description("Is the access level code that identify who can access the building")
                                                .mandatory(true)
                                                .type(InventoryClassAttributeType.String)
                                                .build()
                                )
                        )
                        .build()
        );
        var distinctTypes = inventoryClassRepository.findDistinctTypes();
        assertThat(distinctTypes)
                .hasSize(2)
                .contains(
                        InventoryClassType.Building,
                        InventoryClassType.Room
                );
    }
}
