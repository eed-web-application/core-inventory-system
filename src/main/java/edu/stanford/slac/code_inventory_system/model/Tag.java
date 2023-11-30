package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.code_inventory_system.service.utility.IdNameInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Define a tag
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements IdNameInterface {
    @Id
    private String id;
    private String name;
}
