package edu.stanford.slac.code_inventory_system.model;

import edu.stanford.slac.ad.eed.baselib.model.AuthenticationToken;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Define a space where the inventory live, different domains
 *  contain different inventory item classes are shared by domain
 */
@Data
@Builder
@ToString
@AllArgsConstructor
public class InventoryDomain {
    @Id
    private String id;
    /**
     * The name of the domain
     */
    private String name;
    /**
     * The description of the domain
     */
    private String description;
    /**
     * The list of the tags that can be used for all
     * inventory elements of this domain
     */
    @Builder.Default
    private List<Tag> tags = emptyList();
    @CreatedDate
    private LocalDateTime createdDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @Version
    private Long version;
}
