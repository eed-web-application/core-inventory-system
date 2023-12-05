package edu.stanford.slac.code_inventory_system.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.config.AppProperties;
import edu.stanford.slac.ad.eed.baselib.exception.NotAuthorized;
import edu.stanford.slac.ad.eed.baselib.model.AuthenticationToken;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.TagDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.UpdateDomainDTO;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
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
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;

import static edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationOwnerTypeDTO.User;
import static edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationTypeDTO.Write;
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
public class InventoryElementControllerTest {
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

    @BeforeAll
    public void createAuthorizationForTests() {
        mongoTemplate.remove(new Query(), Authorization.class);
        mongoTemplate.remove(new Query(), AuthenticationToken.class);
        appProperties.getRootUserList().clear();
        appProperties.getRootUserList().add("user1@slac.stanford.edu");
        authService.updateRootUser();
    }

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), InventoryClass.class);
        mongoTemplate.remove(new Query(), InventoryDomain.class);
        mongoTemplate.remove(new Query(), InventoryElement.class);
    }

    @Test
    public void createDomainCheckValidation(){
        var createDomainByExcept = assertThrows(
                MethodArgumentNotValidException.class,
                ()->testControllerHelperService.inventoryElementControllerCreateNewDomain(
                        mockMvc,
                        status().isBadRequest(),
                        Optional.of("user1@slac.stanford.edu"),
                        NewInventoryDomainDTO
                                .builder()
                                .name("This Is A Not good Name")
                                .build()
                )
        );
        assertThat(createDomainByExcept).isNotNull();
    }

    @Test
    public void createDomainAsRoot(){
        var createDomainByRootResult = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerCreateNewDomain(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        NewInventoryDomainDTO
                                .builder()
                                .name("lcls-inventory")
                                .description("Inventory for the LCLS")
                                .build()
                )
        );
        assertThat(createDomainByRootResult).isNotNull();
        assertThat(createDomainByRootResult.getPayload())
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    public void createDomainFailAsNonRoot(){
        var notAuthorized = assertThrows(
                NotAuthorized.class,
                ()->testControllerHelperService.inventoryElementControllerCreateNewDomain(
                        mockMvc,
                        status().isUnauthorized(),
                        Optional.of("user2@slac.stanford.edu"),
                        NewInventoryDomainDTO
                                .builder()
                                .name("lcls-inventory")
                                .description("Inventory for the LCLS")
                                .build()
                )
        );
        assertThat(notAuthorized).isNotNull();
    }

    @Test
    public void updateDomainAndCheck() {
        var createDomainByRootResult = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerCreateNewDomain(
                        mockMvc,
                        status().isCreated(),
                        Optional.of("user1@slac.stanford.edu"),
                        NewInventoryDomainDTO
                                .builder()
                                .name("lcls-inventory")
                                .description("Inventory for the LCLS")
                                .build()
                )
        );
        assertThat(createDomainByRootResult).isNotNull();
        assertThat(createDomainByRootResult.getPayload())
                .isNotNull()
                .isNotEmpty();

        assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerUpdateDomain(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        createDomainByRootResult.getPayload(),
                        UpdateDomainDTO
                                .builder()
                                .name("lcls-inventory-updated")
                                .description("Inventory for the LCLS updated")
                                .tags(
                                        List.of(
                                                TagDTO
                                                        .builder()
                                                        .name("tag A")
                                                        .build()
                                        )
                                )
                                .authorizations(
                                        List.of(
                                                AuthorizationDTO
                                                        .builder()
                                                        .authorizationType(Write)
                                                        .owner("user2@slac.stanford.edu")
                                                        .ownerType(User)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        var fullDomain = assertDoesNotThrow(
                ()->testControllerHelperService.inventoryElementControllerFindDomainById(
                        mockMvc,
                        status().isOk(),
                        Optional.of("user1@slac.stanford.edu"),
                        createDomainByRootResult.getPayload()
                )
        );

        assertThat(fullDomain.getErrorCode()).isEqualTo(0);
        assertThat(fullDomain.getPayload().tags()).hasSize(1);
        assertThat(fullDomain.getPayload().authorizations()).hasSize(1);
    }

}
