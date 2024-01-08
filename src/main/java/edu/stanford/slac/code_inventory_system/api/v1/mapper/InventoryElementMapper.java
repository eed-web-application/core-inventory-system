package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthenticationTokenDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.mapper.AuthMapper;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.ad.eed.baselib.model.AuthenticationToken;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.exception.InventoryElementAttributeNotForClass;
import edu.stanford.slac.code_inventory_system.exception.TagNotFound;
import edu.stanford.slac.code_inventory_system.model.*;
import edu.stanford.slac.code_inventory_system.model.value.*;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import edu.stanford.slac.code_inventory_system.repository.InventoryDomainRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.slac.code_inventory_system.config.AppProperties.CIS_DOMAIN_AUTH_FORMAT;
import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;
import static java.util.Collections.emptyList;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        uses = {AuthMapper.class}
)

public abstract class InventoryElementMapper {
    @Autowired
    AuthMapper authMapper;
    @Autowired
    AuthService authService;
    @Autowired
    InventoryClassMapper inventoryClassMapper;
    @Autowired
    InventoryClassRepository inventoryClassRepository;
    @Autowired
    InventoryDomainRepository inventoryDomainRepository;
    public abstract Tag toModel(TagDTO tagDTO);
    @Mapping(target = "tags", source = "newInventoryDomainDTO.tags", conditionExpression = "java(newInventoryDomainDTO.tags() != null)", nullValuePropertyMappingStrategy = IGNORE)
    public abstract InventoryDomain toModel(NewInventoryDomainDTO newInventoryDomainDTO);

    @Mapping(target = "name", source = "updateDomainDTO.name", conditionExpression = "java(updateDomainDTO.name() != null)", nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "description", source = "updateDomainDTO.description", conditionExpression = "java(updateDomainDTO.description() != null)", nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "tags", source = "updateDomainDTO.tags", conditionExpression = "java(updateDomainDTO.tags() != null)", nullValuePropertyMappingStrategy = IGNORE)
    public abstract void updateModel(@MappingTarget InventoryDomain inventoryDomain, UpdateDomainDTO updateDomainDTO);

    @Mapping(target = "tags", source = "updateInventoryElementDTO.tags", conditionExpression = "java(updateInventoryElementDTO.tags() != null)", nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "description", source = "updateInventoryElementDTO.description", conditionExpression = "java(updateInventoryElementDTO.description() != null)", nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "attributes", expression = "java(toElementAttributeWithClass(inventoryElement.getClassId(), updateInventoryElementDTO.attributes()))")
    public abstract void updateModel(@MappingTarget InventoryElement inventoryElement, UpdateInventoryElementDTO updateInventoryElementDTO);

    public abstract InventoryDomain toModel(InventoryDomainDTO inventoryDomainDTO);
    @Mapping(target = "authorizations", expression = "java(fillAuthorizationField(domain.getId()))")
    public abstract InventoryDomainDTO toDTO(InventoryDomain domain);

    public abstract InventoryDomainSummaryDTO toSummaryDTO(InventoryDomain domain);

    public abstract InventoryDomainMinimalDTO toMinimalDTO(InventoryDomain domain);

    public abstract TagDTO toDTO(Tag tag);

    @Mapping(target = "attributes", expression = "java(toElementAttributeWithClass(newInventoryElementDTO.classId(),newInventoryElementDTO.attributes()))")
    public abstract InventoryElement toModel(String domainId, NewInventoryElementDTO newInventoryElementDTO);

    @Mapping(target = "attributes", expression = "java(toElementAttributeWithString(inventoryElement.getAttributes()))")
    @Mapping(target = "tags", expression = "java(toDTOTagsFromId(inventoryElement.getDomainId(),inventoryElement.getTags()))")
    @Mapping(target = "domainDTO", expression = "java(toInventoryDomainMinimalFromId(inventoryElement.getDomainId()))")
    @Mapping(target = "classDTO", expression = "java(toInventoryClassSummaryFromId(inventoryElement.getClassId()))")
    public abstract InventoryElementDTO toDTO(InventoryElement inventoryElement);

    @Mapping(target = "tags", expression = "java(toDTOTagsFromId(inventoryElement.getDomainId(),inventoryElement.getTags()))")
    @Mapping(target = "domainDTO", expression = "java(toInventoryDomainMinimalFromId(inventoryElement.getDomainId()))")
    @Mapping(target = "classDTO", expression = "java(toInventoryClassSummaryFromId(inventoryElement.getClassId()))")
    public abstract InventoryElementSummaryDTO toSummaryDTO(InventoryElement inventoryElement);

    @Mapping(target = "value", expression = "java(getInventoryElementAttributeValueDTO(inventoryElementAttributeHistory.getValue()))")
    public abstract InventoryElementAttributeHistoryDTO toDTO(InventoryElementAttributeHistory inventoryElementAttributeHistory);

    public InventoryDomainMinimalDTO toInventoryDomainMinimalFromId(String domainId) {
        if(domainId == null) return null;
        var inventoryDomainFound = wrapCatch(
                ()->inventoryDomainRepository.findById(domainId),
                -1
        );
        return inventoryDomainFound.map(this::toMinimalDTO).orElse(null);
    }

    public InventoryClassSummaryDTO toInventoryClassSummaryFromId(String classId) {
        if(classId == null) return null;
        var inventoryClassFound = wrapCatch(
                ()->inventoryClassRepository.findById(classId),
                -1
        );
        return inventoryClassFound.map(inventoryClassMapper::toSummaryDTO).orElse(null);
    }

    /**
     * return the list of the authorization DTO from the domain id
     * @param domainId the id of the domain
     * @return the list of authorization DTO found
     */
    public List<AuthorizationDTO> fillAuthorizationField(String domainId) {
        if(domainId == null) return emptyList();
        return authService.findByResourceIs(CIS_DOMAIN_AUTH_FORMAT.formatted(domainId));
    }

    /**
     * Converts a list of AuthenticationTokenDTO objects to a list of AuthenticationToken objects.
     * If the input list is null or empty, an empty list is returned.
     *
     * @param authenticationTokenDTOS the list of AuthenticationTokenDTO objects to convert
     * @return the list of AuthenticationToken objects
     */
    public List<AuthenticationToken> toAuthenticationToken(List<AuthenticationTokenDTO> authenticationTokenDTOS) {
        if(authenticationTokenDTOS == null || authenticationTokenDTOS.isEmpty()) return emptyList();
        return authenticationTokenDTOS.stream()
                .map(
                        t->authMapper.toModelAuthenticationToken(t)
                )
                .toList();
    }

    /**
     * Converts a list of tag IDs to a list of TagDTO objects.
     *
     * @param domainId the unique ID of the domain
     * @param tagsId the list of tag IDs
     * @return the list of TagDTO objects
     * @throws TagNotFound if any of the tag IDs cannot be found in the inventoryDomainRepository
     */
    public List<TagDTO> toDTOTagsFromId(String domainId, List<String> tagsId) {
        List<TagDTO> result = new ArrayList<>();
        for (String id :
                tagsId) {
            result.add(
                    inventoryDomainRepository.findTagById(domainId, id)
                            .map(
                                    this::toDTO
                            )
                            .orElseThrow(
                                    () -> TagNotFound.tagNotFoundAny()
                                            .errorCode(-1)
                                            .build()
                            )
            );
        }
        return result;
    }

    /**
     * Converts a list of AbstractValue objects to a list of InventoryElementAttributeValue objects with string values.
     *
     * @param classId                       the id of the inventory class
     * @param inventoryElementAttributeValueDTOS the list of InventoryElementAttributeValueDTO objects to convert
     * @return the list of InventoryElementAttributeValue objects with string values
     * @throws InventoryClassNotFound       if the inventory class with the given id is not found
     * @throws InventoryElementAttributeNotForClass  if the attribute is not found in the inventory class
     * @throws ControllerLogicException     if an invalid attribute type is encountered
     */
    public List<AbstractValue> toElementAttributeWithClass(
            String classId,
            List<InventoryElementAttributeValueDTO> inventoryElementAttributeValueDTOS) {
        List<AbstractValue> abstractAttributeList = new ArrayList<>();
        if (inventoryElementAttributeValueDTOS == null) return abstractAttributeList;
        InventoryClass ic = wrapCatch(
                () -> inventoryClassRepository.findById(classId),
                -1
        ).orElseThrow(
                () -> InventoryClassNotFound
                        .classNotFoundById()
                        .id(classId)
                        .errorCode(-2)
                        .build()
        );

        // check for the all attribute and convert it
        for (var attributeValue : inventoryElementAttributeValueDTOS) {
            var attributeFound = ic.getAttributes().stream().filter(
                    attr -> attr.getName().compareToIgnoreCase(attributeValue.name()) == 0
            ).findFirst().orElseThrow(
                    () -> InventoryElementAttributeNotForClass.ieaNotForClassName()
                            .className(ic.getName())
                            .attributeName(attributeValue.name())
                            .errorCode(-3)
                            .build()
            );

            Class<? extends AbstractValue> valueType = attributeFound.getType().toClassType();
            if (valueType.isAssignableFrom(StringValue.class)) {
                abstractAttributeList.add(
                        StringValue
                                .builder()
                                .name(attributeValue.name())
                                .value(attributeValue.value())
                                .build()
                );
            } else if (valueType.isAssignableFrom(BooleanValue.class)) {
                abstractAttributeList.add(
                        BooleanValue
                                .builder()
                                .name(attributeValue.name())
                                .value(Boolean.valueOf(attributeValue.value()))
                                .build()
                );
            } else if (valueType.isAssignableFrom(NumberValue.class)) {
                abstractAttributeList.add(
                        NumberValue
                                .builder()
                                .name(attributeValue.name())
                                .value(Long.valueOf(attributeValue.value()))
                                .build()
                );
            } else if (valueType.isAssignableFrom(DoubleValue.class)) {
                abstractAttributeList.add(
                        DoubleValue
                                .builder()
                                .name(attributeValue.name())
                                .value(Double.valueOf(attributeValue.value()))
                                .build()
                );
            } else if (valueType.isAssignableFrom(DateValue.class)) {
                abstractAttributeList.add(
                        DateValue
                                .builder()
                                .name(attributeValue.name())
                                .value(LocalDate.parse(attributeValue.value(), DateTimeFormatter.ISO_LOCAL_DATE))
                                .build()
                );
            } else if (valueType.isAssignableFrom(DateTimeValue.class)) {
                abstractAttributeList.add(
                        DateTimeValue
                                .builder()
                                .name(attributeValue.name())
                                .value(LocalDateTime.parse(attributeValue.value(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .build()
                );
            } else {
                throw ControllerLogicException.builder()
                        .errorCode(-4)
                        .errorMessage("Invalid attribute type")
                        .errorDomain("InventoryElementMapper::toElementAttributeWithClass")
                        .build();
            }
        }
        return abstractAttributeList;
    }

    /**
     * Converts a list of AbstractValue objects to a list of InventoryElementAttributeValue objects with string values.
     *
     * @param inventoryElementAttributeClass the list of AbstractValue objects to convert
     * @return the list of InventoryElementAttributeValue objects with string values
     * @throws ControllerLogicException if an invalid attribute type is encountered
     */
    public List<InventoryElementAttributeValueDTO> toElementAttributeWithString(
            List<AbstractValue> inventoryElementAttributeClass) {
        List<InventoryElementAttributeValueDTO> resultList = new ArrayList<>();
        if (inventoryElementAttributeClass == null) return resultList;
        for (AbstractValue abstractValue : inventoryElementAttributeClass) {
            resultList.add(getInventoryElementAttributeValueDTO(abstractValue));
        }
        return resultList;
    }

    /**
     * Retrieves an InventoryElementAttributeValueDTO based on the given AbstractValue.
     *
     * @param abstractValue the AbstractValue to convert
     * @return the corresponding InventoryElementAttributeValueDTO
     * @throws ControllerLogicException if an invalid attribute type is encountered
     */
    protected InventoryElementAttributeValueDTO getInventoryElementAttributeValueDTO(AbstractValue abstractValue) {
        InventoryElementAttributeValueDTO newAttributeValue = null;
        Class<? extends AbstractValue> valueType = abstractValue.getClass();
        if (valueType.isAssignableFrom(StringValue.class)) {
            newAttributeValue = InventoryElementAttributeValueDTO
                    .builder()
                    .name(abstractValue.getName())
                    .value(((StringValue) abstractValue).getValue())
                    .build();
        } else if (valueType.isAssignableFrom(BooleanValue.class)) {
            newAttributeValue = InventoryElementAttributeValueDTO
                    .builder()
                    .name(abstractValue.getName())
                    .value(((BooleanValue) abstractValue).getValue().toString())
                    .build();
        } else if (valueType.isAssignableFrom(NumberValue.class)) {
            newAttributeValue = InventoryElementAttributeValueDTO
                    .builder()
                    .name(abstractValue.getName())
                    .value(((NumberValue) abstractValue).getValue().toString())
                    .build();
        } else if (valueType.isAssignableFrom(DoubleValue.class)) {
            newAttributeValue = InventoryElementAttributeValueDTO
                    .builder()
                    .name(abstractValue.getName())
                    .value(((DoubleValue) abstractValue).getValue().toString())
                    .build();
        } else if (valueType.isAssignableFrom(DateValue.class)) {
            newAttributeValue = InventoryElementAttributeValueDTO
                    .builder()
                    .name(abstractValue.getName())
                    .value(((DateValue) abstractValue).getValue().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build();
        } else if (valueType.isAssignableFrom(DateTimeValue.class)) {
            newAttributeValue = InventoryElementAttributeValueDTO
                    .builder()
                    .name(abstractValue.getName())
                    .value(((DateTimeValue) abstractValue).getValue().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        } else {
            throw ControllerLogicException.builder()
                    .errorCode(-4)
                    .errorMessage("Invalid attribute type")
                    .errorDomain("InventoryElementMapper::toElementAttributeWithClass")
                    .build();
        }
        return newAttributeValue;
    }

}
