package edu.stanford.slac.code_inventory_system.model.value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@SuperBuilder
@ToString
@JsonTypeName("bool-value")
public class BooleanValue extends AbstractValue {
    private Boolean value;
}

