package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.*;
import edu.stanford.slac.code_inventory_system.model.value.StringValue;
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
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryElementRepositoryTest {
    @Autowired
    private InventoryClassRepository inventoryClassRepository;
    @Autowired
    private InventoryElementRepository inventoryElementRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryElement.class);
    }

    @Test
    public void saveOk() {
        InventoryElement ie = InventoryElement
                .builder()
                .name("Control System Building")
                .classId(UUID.randomUUID().toString())
                .attributes(
                        List.of(
                                StringValue
                                        .builder()
                                        .name("alias")
                                        .value("B-34")
                                        .build(),
                                StringValue
                                        .builder()
                                        .name("building-access-manager")
                                        .value("user@domain.com")
                                        .build(),
                                StringValue
                                        .builder()
                                        .name("area-access-level")
                                        .value("green")
                                        .build()
                        )
                )
                .connectorClasses(
                        List.of(
                                ConnectorClass
                                        .builder()
                                        .count(1)
                                        .classID(UUID.randomUUID().toString())
                                        .build()
                        )
                )
                .build();

        var savedIE = inventoryElementRepository.save(
                ie
        );

        assertThat(savedIE)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .isEqualTo(ie);
    }
    @Test
    public void testThreeUpward(){
        var root = inventoryElementRepository.save(
                InventoryElement
                        .builder()
                        .domainId("domainId")
                        .name("root")
                        .build()
        );
        var child1 = inventoryElementRepository.save(
                InventoryElement
                        .builder()
                        .domainId("domainId")
                        .name("child1")
                        .parentId(root.getId())
                        .build()
        );
        var child2 = inventoryElementRepository.save(
                InventoryElement
                        .builder()
                        .name("child2")
                        .domainId("domainId")
                        .parentId(child1.getId())
                        .build()
        );
        var pathToRoot = inventoryElementRepository.findPathToRoot("domainId", child2.getId());
        assertThat(pathToRoot)
                .isNotNull()
                .hasSize(2)
                .extracting(InventoryElement::getId)
                .containsExactly(child1.getId(), root.getId());

        var pathToLeaf = inventoryElementRepository.findIdPathToLeaf("domainId", root.getId());
        assertThat(pathToLeaf)
                .isNotNull()
                .hasSize(2)
                .extracting(InventoryElement::getId)
                .containsExactly(child1.getId(), child2.getId());
    }
}
