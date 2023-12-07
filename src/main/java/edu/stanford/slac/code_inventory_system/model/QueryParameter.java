package edu.stanford.slac.code_inventory_system.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParameter {
    @Builder.Default
    List<String> domainId = emptyList();
    @Builder.Default
    String anchorID = null;
    @Builder.Default
    Integer contextSize = 0;
    @Builder.Default
    Integer limit = 0;
    @Builder.Default
    String search = null;
    @Builder.Default
    List<String> tags  = emptyList();
    @Builder.Default
    private Boolean requireAllTags = false;
}
