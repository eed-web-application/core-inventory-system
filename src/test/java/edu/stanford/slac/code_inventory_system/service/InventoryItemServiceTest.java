package edu.stanford.slac.code_inventory_system.service;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
    public void saveDomainOK(){
        String newDomainId = assertDoesNotThrow(
                ()->inventoryElementService.createNew(
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
    public void getDomainOK(){
        String newDomainId = assertDoesNotThrow(
                ()->inventoryElementService.createNew(
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
                ()->inventoryElementService.getFullDomain(newDomainId)
        );

        assertThat(fullDomain).isNotNull()
                .extracting(
                        InventoryDomainDTO::name
                ).isEqualTo(
                        "new-domain"
                );
    }
}
