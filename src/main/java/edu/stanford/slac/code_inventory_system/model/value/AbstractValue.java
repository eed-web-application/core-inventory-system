package edu.stanford.slac.code_inventory_system.model.value;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = ExtendedDataBooleanValueDTO.class, name = "bool-value"),
//        @JsonSubTypes.Type(value = ExtendedDataDateValueDTO.class, name = "date-value"),
//        @JsonSubTypes.Type(value = ExtendedDataIntegerValueDTO.class, name = "integer-value"),
//        @JsonSubTypes.Type(value = ExtendedDataLongValueDTO.class, name = "long-value"),
//        @JsonSubTypes.Type(value = ExtendedDataFloatValueDTO.class, name = "float-value"),
//        @JsonSubTypes.Type(value = ExtendedDataDoubleValueDTO.class, name = "double-value"),
//        @JsonSubTypes.Type(value = ExtendedDataStringValueDTO.class, name = "string-value"),
//})

public class AbstractValue {
    private String name;
}
