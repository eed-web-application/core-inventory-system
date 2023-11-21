package edu.stanford.slac.code_inventory_system.model.value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.slac.code_inventory_system.model.value.AbstractValue;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("string-value")
@AllArgsConstructor
@SuperBuilder
@ToString
public class StringValue extends AbstractValue {
    private String value;
}
