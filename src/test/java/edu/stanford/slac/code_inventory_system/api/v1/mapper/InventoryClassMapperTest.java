package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassAttributeDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassAttributeTypeDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassTypeDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttribute;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttributeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class InventoryClassMapperTest {
    @Autowired
    InventoryClassMapper inventoryClassMapper;

    @Test
    public void convertNewInventoryClassToModel() {
        var newInventoryDTO = NewInventoryClassDTO
                .builder()
                .name("new class")
                .type(InventoryClassTypeDTO.Building)
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
        InventoryClass newInventoryClassModel = inventoryClassMapper.toModel(
                newInventoryDTO
        );
        assertThat(newInventoryClassModel).isNotNull();
        assertThat(newInventoryClassModel.getName()).isEqualTo(newInventoryDTO.name());
        assertThat(newInventoryClassModel.getType().name()).isEqualTo(newInventoryDTO.type().name());

        assertThat(
                newInventoryClassModel.getAttributes().size()
        ).isEqualTo(newInventoryDTO.attributes().size());

        assertThat(
                newInventoryClassModel.getAttributes()
        ).extracting(InventoryClassAttribute::getName).contains("security level");
        assertThat(
                newInventoryClassModel.getAttributes()
        ).extracting(InventoryClassAttribute::getMandatory).contains(true);
        assertThat(
                newInventoryClassModel.getAttributes()
        ).extracting(InventoryClassAttribute::getType).contains(InventoryClassAttributeType.String);
    }
}
