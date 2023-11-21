package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.AttributeType;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttribute;
import edu.stanford.slac.code_inventory_system.model.InventoryClassType;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryClassTest {
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
                                        .type(AttributeType.String)
                                        .build(),
                                InventoryClassAttribute
                                        .builder()
                                        .name("Building access manager")
                                        .description("Is the access manage user identification")
                                        .mandatory(true)
                                        .type(AttributeType.String)
                                        .build(),
                                InventoryClassAttribute
                                        .builder()
                                        .name("Area access level")
                                        .description("Is the access level code that identify who can access the building")
                                        .mandatory(true)
                                        .type(AttributeType.String)
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
}