package edu.stanford.slac.code_inventory_system.repository;

import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.Tag;
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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryDomainRepositoryTest {
    @Autowired
    InventoryDomainRepository inventoryDomainRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryDomain.class);
    }

    @Test
    public void createDomainOk() {
        InventoryDomain newDomain = InventoryDomain.builder()
                .name("domain-a")
                .description("domain a description")
                .build();
        var newSavedDomain = inventoryDomainRepository.save(newDomain);

        assertThat(newSavedDomain)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .isEqualTo(newDomain);
    }

    @Test
    public void createDomainCheckNormalizationNameOk() {
        InventoryDomain newDomain = InventoryDomain.builder()
                .name("ĎomaÏn á")
                .description("domain a description")
                .build();
        var newSavedDomain = inventoryDomainRepository.save(newDomain);

        assertThat(newSavedDomain)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .isEqualTo(newDomain);
        assertThat(newSavedDomain.getName())
                .isEqualTo("domain-a");
    }

    @Test
    public void ensureTagOk() {
        // create new domain
        Set<String> createdIdSet = new HashSet<>();
        var newDomain = inventoryDomainRepository.save(
                InventoryDomain.builder()
                        .name("domain-a")
                        .description("domain a description")
                        .build()
        );

        // try to create tag in a concurrent way
        try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {
            for(int idx = 0; idx < 100; idx++) {
                executorService.execute(() -> {
                    String newId = inventoryDomainRepository.ensureTag(
                            newDomain.getId(),
                            Tag
                                    .builder()
                                    .name("tag-a")
                                    .build()
                    );
                    synchronized (createdIdSet) {
                        createdIdSet.add(newId);
                    }
                });
            }
        }
        // at this point when should have created only one tag
        assertThat(createdIdSet).hasSize(1);
    }

    @Test
    public void ensureTagNameNormalizationOk() {
        // create domain
        var newDomain = inventoryDomainRepository.save(
                InventoryDomain.builder()
                        .name("domain-a")
                        .description("domain a description")
                        .build()
        );

        String newIdTagA = inventoryDomainRepository.ensureTag(
                newDomain.getId(),
                Tag
                        .builder()
                        .name("tag-a")
                        .build()
        );
        String newIdTagB = inventoryDomainRepository.ensureTag(
                newDomain.getId(),
                Tag
                        .builder()
                        .name("tag-b")
                        .build()
        );

        //find tag by his id
        var foundTag = inventoryDomainRepository.findTagById(newDomain.getId(), newIdTagB);

        assertThat(foundTag)
                .isNotNull()
                .extracting(Tag::getId)
                .isEqualTo(newIdTagB);

        // check the exists api if works
        var existsTag = inventoryDomainRepository.existsTagById(newDomain.getId(), newIdTagB);
        assertThat(existsTag)
                .isTrue();
    }

    @Test
    public void checkExistanceOfWrongTagIdReturnFalse() {
        // create domain
        var newDomain = inventoryDomainRepository.save(
                InventoryDomain.builder()
                        .name("domain-a")
                        .description("domain a description")
                        .build()
        );

        String newIdTagA = inventoryDomainRepository.ensureTag(
                newDomain.getId(),
                Tag
                        .builder()
                        .name("tag-a")
                        .build()
        );


        //find tag by his id
        var foundTag = inventoryDomainRepository.findTagById(newDomain.getId(), "bad id");

        assertThat(foundTag)
                .isNull();

        // check the exists api if works
        var existsTag = inventoryDomainRepository.existsTagById(newDomain.getId(), "bad id");
        assertThat(existsTag)
                .isFalse();
    }
}
