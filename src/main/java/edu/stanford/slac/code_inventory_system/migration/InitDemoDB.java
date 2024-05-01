package edu.stanford.slac.code_inventory_system.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.api.v1.dto.ImportInventoryDataDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryDomainDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryElementDTO;
import edu.stanford.slac.code_inventory_system.service.InventoryClassService;
import edu.stanford.slac.code_inventory_system.service.InventoryElementService;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static edu.stanford.slac.code_inventory_system.exception.Utility.wrapCatch;

@Log4j2
@AllArgsConstructor
@Profile("init-demo-database")
@ChangeUnit(id = "init-demo-database", order = "0", author = "bisegni")
public class InitDemoDB {
    private final InventoryClassService inventoryClassService;
    private final InventoryElementService inventoryElementService;

    @Execution
    public void changeSet() {
        // create object mapper instance
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        // read file and convert to List of InventoryClassDTO
        ClassPathResource importDemoFile = new ClassPathResource("demo-structure.yml");
        try (InputStream is = importDemoFile.getInputStream()) {
            ImportInventoryDataDTO inventoryData = mapper.readValue(is, ImportInventoryDataDTO.class);
            //build environment form file
            buildEnvironment(inventoryData);
        } catch (IOException e) {
            log.error("Unable to load demo-structure.yml due to ", e);
        }
    }


    @RollbackExecution
    public void rollback() {

    }

    /**
     * Builds the environment by creating new inventory classes and a new inventory domain.
     *
     * @param inventoryDataDTO the inventory data containing the list of classes, domains, and elements to create
     * @return a map containing the names of the created classes and domain as keys and the corresponding IDs as values
     */
    private Map<String, String> buildEnvironment(
            ImportInventoryDataDTO inventoryDataDTO
    ) {
        Map<String, String> resultInfo = new HashMap<>();
        // create class
        for (NewInventoryClassDTO classDTO :
                inventoryDataDTO.getClassList()) {
            var createNewClassResult = wrapCatch(
                    () -> inventoryClassService.createNew(
                            classDTO
                    ),
                    -1
            );
            resultInfo.put(
                    "class_" + classDTO.name().toLowerCase(),
                    createNewClassResult
            );
            log.info("[DEMO database] Created inventory class id: {}", createNewClassResult);
        }

        for (
                NewInventoryDomainDTO newInventoryDomainDTO :
                inventoryDataDTO.getDomainList()
        ) {
            var createDomainResult = wrapCatch(
                    () -> inventoryElementService.createNew(newInventoryDomainDTO),
                    -2
            );
            resultInfo.put(
                    "domain_" + newInventoryDomainDTO.name().toLowerCase(),
                    createDomainResult
            );
            log.info("[DEMO database] Created inventory domain id: {}", createDomainResult);
        }

        for (
                ImportInventoryDataDTO.InventoryElementWithDomain newElement :
                inventoryDataDTO.getElementList()
        ) {
            NewInventoryElementDTO newInventoryElementDTO = newElement.getElement();
            if (!resultInfo.containsKey("domain_" + newElement.getDomainId().toLowerCase())) {
                throw ControllerLogicException
                        .builder()
                        .errorCode(-1)
                        .errorMessage("No domain id found for %s".formatted(newElement.getDomainId()))
                        .errorDomain("buildEnvironment")
                        .build();
            }

            if (!resultInfo.containsKey("class_" + newElement.getElement().classId().toLowerCase())) {
                throw ControllerLogicException
                        .builder()
                        .errorCode(-2)
                        .errorMessage("No class id found for %s".formatted(newElement.getElement().classId()))
                        .errorDomain("buildEnvironment")
                        .build();
            } else {
                newInventoryElementDTO = newInventoryElementDTO.toBuilder()
                        .classId(resultInfo.get("class_" + newElement.getElement().classId().toLowerCase()))
                        .build();
            }

            if (
                    newElement.getElement().parentId() != null &&
                            !resultInfo.containsKey("element_" + newElement.getElement().parentId().toLowerCase())) {
                throw ControllerLogicException
                        .builder()
                        .errorCode(-2)
                        .errorMessage("No parent id found for %s".formatted(newElement.getElement().parentId()))
                        .errorDomain("buildEnvironment")
                        .build();
            } else if (newElement.getElement().parentId() != null) {
                newInventoryElementDTO = newInventoryElementDTO.toBuilder()
                        .parentId
                                (
                                        resultInfo.get("element_" + newElement.getElement().parentId().toLowerCase())
                                )
                        .classId(
                                resultInfo.get("class_" + newElement.getElement().classId().toLowerCase())
                        ).build();
            }

            NewInventoryElementDTO finalNewInventoryElementDTO = newInventoryElementDTO;
            var createElementResult = wrapCatch(
                    () -> inventoryElementService.createNew(
                            resultInfo.get("domain_" + newElement.getDomainId().toLowerCase()),
                            finalNewInventoryElementDTO
                    ),
                    -2
            );
            resultInfo.put(
                    "element_" + newElement.getElement().name().toLowerCase(),
                    createElementResult
            );
            log.info("[DEMO database] Created inventory domain id: {}", newInventoryElementDTO);
        }

        return resultInfo;
    }
}