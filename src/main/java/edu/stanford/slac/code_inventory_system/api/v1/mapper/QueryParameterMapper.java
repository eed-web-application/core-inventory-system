package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.code_inventory_system.api.v1.dto.QueryParameterDTO;
import edu.stanford.slac.code_inventory_system.model.QueryParameter;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public abstract class QueryParameterMapper {

    public abstract QueryParameter fromDTO(QueryParameterDTO queryParameterDTO);
}