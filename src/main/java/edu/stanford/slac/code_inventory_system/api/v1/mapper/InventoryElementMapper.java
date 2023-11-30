package edu.stanford.slac.code_inventory_system.api.v1.mapper;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import edu.stanford.slac.code_inventory_system.exception.InventoryClassNotFound;
import edu.stanford.slac.code_inventory_system.exception.InventoryElementAttributeNotForClass;
import edu.stanford.slac.code_inventory_system.model.InventoryClass;
import edu.stanford.slac.code_inventory_system.model.InventoryDomain;
import edu.stanford.slac.code_inventory_system.model.InventoryElement;
import edu.stanford.slac.code_inventory_system.model.value.*;
import edu.stanford.slac.code_inventory_system.repository.InventoryClassRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)

public abstract class InventoryElementMapper {
    @Autowired
    InventoryClassRepository inventoryClassRepository;

    public abstract InventoryDomain toModel(NewInventoryDomainDTO newInventoryDomainDTO);

    @Mapping(target = "name", source = "updateDomainDTO.name", conditionExpression = "java(updateDomainDTO.name() != null)")
    @Mapping(target = "description", source = "updateDomainDTO.description", conditionExpression = "java(updateDomainDTO.description() != null)")
    @Mapping(target = "tags", source = "updateDomainDTO.tags", conditionExpression = "java(updateDomainDTO.tags() != null)")
    public abstract void updateModel(@MappingTarget InventoryDomain inventoryDomain, UpdateDomainDTO updateDomainDTO);

    public abstract InventoryDomain toModel(InventoryDomainDTO inventoryDomainDTO);

    public abstract InventoryDomainDTO toDTO(InventoryDomain domain);

    @Mapping(target = "attributes", expression = "java(toElementAttributeWithClass(newInventoryElementDTO.classId(),newInventoryElementDTO.attributes()))")
    public abstract InventoryElement toModel(String domainId, NewInventoryElementDTO newInventoryElementDTO);

    @Mapping(target = "attributes", expression = "java(toElementAttributeWithClass(inventoryElement.getAttributes()))")
    public abstract InventoryElementDTO toDTO(InventoryElement inventoryElement);

    @Named("getFollowingUp")
    public List<AbstractValue> toElementAttributeWithClass(
            String classId,
            List<InventoryElementAttributeValue> inventoryElementAttributeValues) {
        List<AbstractValue> abstractAttributeList = new ArrayList<>();
        if (inventoryElementAttributeValues == null) return abstractAttributeList;
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
        for (var attributeValue : inventoryElementAttributeValues) {
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

    @Named("getFollowingUp")
    public List<InventoryElementAttributeValue> toElementAttributeWithClass(
            List<AbstractValue> inventoryElementAttributeClass) {
        List<InventoryElementAttributeValue> resultList = new ArrayList<>();
        if (inventoryElementAttributeClass == null) return resultList;
        for (AbstractValue abstractValue : inventoryElementAttributeClass) {
            InventoryElementAttributeValue newAttributeValue = null;
            Class<? extends AbstractValue> valueType = abstractValue.getClass();
            if (valueType.isAssignableFrom(StringValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((StringValue) abstractValue).getValue())
                        .build();
            } else if (valueType.isAssignableFrom(BooleanValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((BooleanValue) abstractValue).getValue().toString())
                        .build();
            } else if (valueType.isAssignableFrom(NumberValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((NumberValue) abstractValue).getValue().toString())
                        .build();
            } else if (valueType.isAssignableFrom(DoubleValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((DoubleValue) abstractValue).getValue().toString())
                        .build();
            } else if (valueType.isAssignableFrom(DateValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((DateValue) abstractValue).getValue().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build();
            } else if (valueType.isAssignableFrom(DateTimeValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
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
            resultList.add(newAttributeValue);
        }
        return resultList;
    }

}
