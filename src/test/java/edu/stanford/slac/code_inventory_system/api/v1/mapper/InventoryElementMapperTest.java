package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryElementAttributeValueDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryElementDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.exception.InventoryElementAttributeNotForClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttribute;
import edu.stanford.slac.code_inventory_system.model.InventoryClassAttributeType;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.value.*;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryElementMapperTest {
    @MockBean
    InventoryClassRepository inventoryClassRepository;
    @Autowired
    InventoryElementMapper inventoryElementMapper;

    @Test
    public void testNotFoundElement() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.Number)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        var assertForNotFoundAttributeName = assertThrows(
                InventoryElementAttributeNotForClass.class,
                () -> inventoryElementMapper.toModel(
                        "bad_domain_id",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("wrong name")
                                                        .value(String.valueOf(Long.MAX_VALUE))
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(assertForNotFoundAttributeName).isNotNull();
        assertThat(assertForNotFoundAttributeName.getErrorCode()).isEqualTo(-3);
    }

    @Test
    public void testNumberValueInElementAttribute() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.Number)
                                                        .build(),
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-2")
                                                        .type(InventoryClassAttributeType.Number)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        InventoryElement invElem = assertDoesNotThrow(
                () -> inventoryElementMapper.toModel(
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(String.valueOf(Long.MAX_VALUE))
                                                        .build(),
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-2")
                                                        .value(String.valueOf(Long.MIN_VALUE))
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElem.getAttributes())
                .hasSize(2);
        assertThat(invElem.getAttributes().get(0))
                .isOfAnyClassIn(NumberValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-1");
        assertThat(((NumberValue) invElem.getAttributes().get(0)).getValue()).isEqualTo(Long.MAX_VALUE);
        assertThat(invElem.getAttributes().get(1))
                .isOfAnyClassIn(NumberValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-2");
        assertThat(((NumberValue) invElem.getAttributes().get(1)).getValue()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    public void testDoubleValueInElementAttribute() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.Double)
                                                        .build(),
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-2")
                                                        .type(InventoryClassAttributeType.Double)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        InventoryElement invElem = assertDoesNotThrow(
                () -> inventoryElementMapper.toModel(
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(String.valueOf(Double.MAX_VALUE))
                                                        .build(),
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-2")
                                                        .value(String.valueOf(Double.MIN_VALUE))
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElem.getAttributes())
                .hasSize(2);
        assertThat(invElem.getAttributes().get(0))
                .isOfAnyClassIn(DoubleValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-1");
        assertThat(((DoubleValue) invElem.getAttributes().get(0)).getValue()).isEqualTo(Double.MAX_VALUE);
        assertThat(invElem.getAttributes().get(1))
                .isOfAnyClassIn(DoubleValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-2");
        assertThat(((DoubleValue) invElem.getAttributes().get(1)).getValue()).isEqualTo(Double.MIN_VALUE);
    }

    @Test
    public void testBooleanValueInElementAttribute() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.Boolean)
                                                        .build(),
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-2")
                                                        .type(InventoryClassAttributeType.Boolean)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        InventoryElement invElem = assertDoesNotThrow(
                () -> inventoryElementMapper.toModel(
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-1")
                                                        .value("True")
                                                        .build(),
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-2")
                                                        .value("0")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElem.getAttributes())
                .hasSize(2);
        assertThat(invElem.getAttributes().get(0))
                .isOfAnyClassIn(BooleanValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-1");
        assertThat(((BooleanValue) invElem.getAttributes().get(0)).getValue()).isEqualTo(true);
        assertThat(invElem.getAttributes().get(1))
                .isOfAnyClassIn(BooleanValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-2");
        assertThat(((BooleanValue) invElem.getAttributes().get(1)).getValue()).isEqualTo(false);
    }

    @Test
    public void testDateValueInElementAttribute() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.Date)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        InventoryElement invElem = assertDoesNotThrow(
                () -> inventoryElementMapper.toModel(
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-1")
                                                        .value("1900-12-31")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElem.getAttributes())
                .hasSize(1);
        assertThat(invElem.getAttributes().get(0))
                .isOfAnyClassIn(DateValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-1");
        assertThat(((DateValue) invElem.getAttributes().get(0)).getValue()).isEqualTo(
                LocalDate.of(
                        1900,
                        12,
                        31
                )
        );
    }

    @Test
    public void testDateTimeValueInElementAttribute() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.DateTime)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        InventoryElement invElem = assertDoesNotThrow(
                () -> inventoryElementMapper.toModel(
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-1")
                                                        .value("1900-12-31T00:00:00")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElem.getAttributes())
                .hasSize(1);
        assertThat(invElem.getAttributes().get(0))
                .isOfAnyClassIn(DateTimeValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-1");
        assertThat(((DateTimeValue) invElem.getAttributes().get(0)).getValue()).isEqualTo(
                LocalDateTime.of(
                        1900,
                        12,
                        31,
                        0,
                        0,
                        0
                )
        );
    }

    @Test
    public void testStringValueInElementAttribute() {
        when(inventoryClassRepository.findById(anyString())).thenReturn(
                Optional.of(
                        InventoryClass
                                .builder()
                                .attributes(
                                        List.of(
                                                InventoryClassAttribute
                                                        .builder()
                                                        .name("attr-1")
                                                        .type(InventoryClassAttributeType.String)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        InventoryElement invElem = assertDoesNotThrow(
                () -> inventoryElementMapper.toModel(
                        "did",
                        NewInventoryElementDTO
                                .builder()
                                .classId("id")
                                .attributes(
                                        List.of(
                                                InventoryElementAttributeValueDTO
                                                        .builder()
                                                        .name("attr-1")
                                                        .value("this is a string")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElem.getAttributes())
                .hasSize(1);
        assertThat(invElem.getAttributes().get(0))
                .isOfAnyClassIn(StringValue.class)
                .extracting(AbstractValue::getName).isEqualTo("attr-1");
        assertThat(((StringValue) invElem.getAttributes().get(0)).getValue()).isEqualTo(
                "this is a string"
        );
    }

    @Test
    public void testToDTOAttributeString() {
        InventoryElementDTO invElemDTO = assertDoesNotThrow(
                () -> inventoryElementMapper.toDTO(
                        InventoryElement
                                .builder()
                                .attributes(
                                        List.of(
                                                StringValue
                                                        .builder()
                                                        .name("attr-1")
                                                        .value("this is a string")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElemDTO.attributes())
                .hasSize(1);
        AssertionsForClassTypes.assertThat(invElemDTO.attributes().get(0))
                .isOfAnyClassIn(InventoryElementAttributeValueDTO.class)
                .extracting(InventoryElementAttributeValueDTO::name).isEqualTo("attr-1");
        assertThat(((InventoryElementAttributeValueDTO) invElemDTO.attributes().get(0)).value()).isEqualTo(
                "this is a string"
        );
    }

    @Test
    public void testToDTOAttributeBoolean() {
        InventoryElementDTO invElemDTO = assertDoesNotThrow(
                () -> inventoryElementMapper.toDTO(
                        InventoryElement
                                .builder()
                                .attributes(
                                        List.of(
                                                BooleanValue
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(true)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElemDTO.attributes())
                .hasSize(1);
        AssertionsForClassTypes.assertThat(invElemDTO.attributes().get(0))
                .isOfAnyClassIn(InventoryElementAttributeValueDTO.class)
                .extracting(InventoryElementAttributeValueDTO::name).isEqualTo("attr-1");
        assertThat(((InventoryElementAttributeValueDTO) invElemDTO.attributes().get(0)).value()).isEqualTo(
                "true"
        );
    }

    @Test
    public void testToDTOAttributeNumber() {
        InventoryElementDTO invElemDTO = assertDoesNotThrow(
                () -> inventoryElementMapper.toDTO(
                        InventoryElement
                                .builder()
                                .attributes(
                                        List.of(
                                                NumberValue
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(Long.MAX_VALUE)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElemDTO.attributes())
                .hasSize(1);
        AssertionsForClassTypes.assertThat(invElemDTO.attributes().get(0))
                .isOfAnyClassIn(InventoryElementAttributeValueDTO.class)
                .extracting(InventoryElementAttributeValueDTO::name).isEqualTo("attr-1");
        assertThat(((InventoryElementAttributeValueDTO) invElemDTO.attributes().get(0)).value()).isEqualTo(
                String.valueOf(Long.MAX_VALUE)
        );
    }

    @Test
    public void testToDTOAttributeDouble() {
        InventoryElementDTO invElemDTO = assertDoesNotThrow(
                () -> inventoryElementMapper.toDTO(
                        InventoryElement
                                .builder()
                                .attributes(
                                        List.of(
                                                DoubleValue
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(Double.MAX_VALUE)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElemDTO.attributes())
                .hasSize(1);
        AssertionsForClassTypes.assertThat(invElemDTO.attributes().get(0))
                .isOfAnyClassIn(InventoryElementAttributeValueDTO.class)
                .extracting(InventoryElementAttributeValueDTO::name).isEqualTo("attr-1");
        assertThat(((InventoryElementAttributeValueDTO) invElemDTO.attributes().get(0)).value()).isEqualTo(
                String.valueOf(Double.MAX_VALUE)
        );
    }

    @Test
    public void testToDTOAttributeDate() {
        InventoryElementDTO invElemDTO = assertDoesNotThrow(
                () -> inventoryElementMapper.toDTO(
                        InventoryElement
                                .builder()
                                .attributes(
                                        List.of(
                                                DateValue
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(
                                                                LocalDate.of(
                                                                        2000,
                                                                        12,
                                                                        31
                                                                )
                                                        )
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElemDTO.attributes())
                .hasSize(1);
        AssertionsForClassTypes.assertThat(invElemDTO.attributes().get(0))
                .isOfAnyClassIn(InventoryElementAttributeValueDTO.class)
                .extracting(InventoryElementAttributeValueDTO::name).isEqualTo("attr-1");
        assertThat(((InventoryElementAttributeValueDTO) invElemDTO.attributes().get(0)).value()).isEqualTo(
                "2000-12-31"
        );
    }

    @Test
    public void testToDTOAttributeDateTime() {
        InventoryElementDTO invElemDTO = assertDoesNotThrow(
                () -> inventoryElementMapper.toDTO(
                        InventoryElement
                                .builder()
                                .attributes(
                                        List.of(
                                                DateTimeValue
                                                        .builder()
                                                        .name("attr-1")
                                                        .value(
                                                                LocalDateTime.of(
                                                                        2000,
                                                                        12,
                                                                        31,
                                                                        0,
                                                                        0,
                                                                        0
                                                                )
                                                        )
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(invElemDTO.attributes())
                .hasSize(1);
        AssertionsForClassTypes.assertThat(invElemDTO.attributes().get(0))
                .isOfAnyClassIn(InventoryElementAttributeValueDTO.class)
                .extracting(InventoryElementAttributeValueDTO::name).isEqualTo("attr-1");
        assertThat(((InventoryElementAttributeValueDTO) invElemDTO.attributes().get(0)).value()).isEqualTo(
                "2000-12-31T00:00:00"
        );
    }
}
