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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public abstract class InventoryElementMapper {
    @Autowired
    InventoryClassRepository inventoryClassRepository;

    public abstract InventoryDomain toModel(NewInventoryDomainDTO newInventoryDomainDTO);
    public abstract InventoryDomain toModel(InventoryDomainDTO inventoryDomainDTO);
    public abstract InventoryDomainDTO toDTO(InventoryDomain domain);

    @Mapping(target = "attributes", expression = "java(toElementAttributeWithClass(newInventoryElementDTO.classId(),newInventoryElementDTO.attributes()))")
    public abstract InventoryElement toModel(NewInventoryElementDTO newInventoryElementDTO);
    @Mapping(target = "attributes", expression = "java(toElementAttributeWithClass(inventoryElement.getAttributes()))")
    public abstract InventoryElementDTO toDTO(InventoryElement inventoryElement);

    @Named("getFollowingUp")
    public List<AbstractValue> toElementAttributeWithClass(
            String classId,
            List<InventoryElementAttributeValue> inventoryElementAttributeValues) {
        List<AbstractValue> abstractAttributeList = new ArrayList<>();
        if(inventoryElementAttributeValues==null) return abstractAttributeList;
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

            AbstractValue newAttributeValue = null;
            Class<? extends AbstractValue> valueType = attributeFound.getType().toClassType();
            if (valueType.isAssignableFrom(StringValue.class)) {
                newAttributeValue = StringValue
                        .builder()
                        .name(attributeValue.name())
                        .value(attributeValue.value())
                        .build();
            } else if (valueType.isAssignableFrom(BooleanValue.class)) {
                newAttributeValue = BooleanValue
                        .builder()
                        .name(attributeValue.name())
                        .value(Boolean.valueOf(attributeValue.value()))
                        .build();
            } else if (valueType.isAssignableFrom(NumberValue.class)) {
                newAttributeValue = NumberValue
                        .builder()
                        .name(attributeValue.name())
                        .value(Long.valueOf(attributeValue.value()))
                        .build();
            } else if (valueType.isAssignableFrom(DoubleValue.class)) {
                newAttributeValue = DoubleValue
                        .builder()
                        .name(attributeValue.name())
                        .value(Double.valueOf(attributeValue.value()))
                        .build();
            } else if (valueType.isAssignableFrom(DateValue.class)) {
                newAttributeValue = DateValue
                        .builder()
                        .name(attributeValue.name())
                        .value(LocalDate.parse(attributeValue.value(), DateTimeFormatter.ISO_LOCAL_DATE))
                        .build();
            } else if (valueType.isAssignableFrom(DateTimeValue.class)) {
                newAttributeValue = DateTimeValue
                        .builder()
                        .name(attributeValue.name())
                        .value(LocalDateTime.parse(attributeValue.value(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build();
            } else {
                throw ControllerLogicException.builder()
                        .errorCode(-4)
                        .errorMessage("Invalid attribute type")
                        .errorDomain("InventoryElementMapper::toElementAttributeWithClass")
                        .build();
            }

            abstractAttributeList.add(newAttributeValue);
        }
        return abstractAttributeList;
    }

    @Named("getFollowingUp")
    public List<InventoryElementAttributeValue> toElementAttributeWithClass(
            List<AbstractValue> inventoryElementAttributeClass) {
        List<InventoryElementAttributeValue> resultList = new ArrayList<>();
        if(inventoryElementAttributeClass==null) return resultList;
        for (AbstractValue abstractValue: inventoryElementAttributeClass) {
            InventoryElementAttributeValue newAttributeValue = null;
            Class<? extends AbstractValue> valueType = abstractValue.getClass();
            if (valueType.isAssignableFrom(StringValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((StringValue)abstractValue).getValue())
                        .build();
            } else if (valueType.isAssignableFrom(BooleanValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((BooleanValue)abstractValue).getValue().toString())
                        .build();
            } else if (valueType.isAssignableFrom(NumberValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((NumberValue)abstractValue).getValue().toString())
                        .build();
            } else if (valueType.isAssignableFrom(DoubleValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((DoubleValue)abstractValue).getValue().toString())
                        .build();
            } else if (valueType.isAssignableFrom(DateValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((DateValue)abstractValue).getValue().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build();
            } else if (valueType.isAssignableFrom(DateTimeValue.class)) {
                newAttributeValue = InventoryElementAttributeValue
                        .builder()
                        .name(abstractValue.getName())
                        .value(((DateTimeValue)abstractValue).getValue().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
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
