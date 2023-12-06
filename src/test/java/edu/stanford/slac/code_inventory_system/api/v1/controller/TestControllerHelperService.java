package edu.stanford.slac.code_inventory_system.api.v1.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.auth.JWTHelper;
import edu.stanford.slac.ad.eed.baselib.config.AppProperties;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.api.v1.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Service()
public class TestControllerHelperService {
    private final JWTHelper jwtHelper;
    private final AppProperties appProperties;

    public TestControllerHelperService(JWTHelper jwtHelper, AppProperties appProperties) {
        this.jwtHelper = jwtHelper;
        this.appProperties = appProperties;

    }

    public ApiResultResponse<String> inventoryClassControllerCreateNew(MockMvc mockMvc, ResultMatcher resultMatcher, Optional<String> userInfo, NewInventoryClassDTO inventoryClassDTO) throws Exception {
        var requestBuilder = post(
                "/v1/inventory/class",
                inventoryClassDTO
        )
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                        new ObjectMapper().writeValueAsString(
                                inventoryClassDTO
                        )
                );
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<InventoryClassDTO> inventoryClassControllerFindById(MockMvc mockMvc, ResultMatcher resultMatcher, Optional<String> userInfo, String id) throws Exception {
        var requestBuilder = get(
                "/v1/inventory/class/{id}",
                id
        )
                .accept(MediaType.APPLICATION_JSON);
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<List<InventoryClassSummaryDTO>> inventoryClassControllerFindAll(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            Optional<List<InventoryClassTypeDTO>> inventoryClassTypeDTOList) throws Exception {
        var requestBuilder = get(
                "/v1/inventory/class"
        )
                .accept(MediaType.APPLICATION_JSON);
        inventoryClassTypeDTOList.ifPresent(typeList -> {
            String[] typeArray = new String[typeList.size()];
            typeList.stream().map(Enum::name).toList().toArray(typeArray);
            requestBuilder.param("classTypes", typeArray);
        });
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<List<InventoryClassTypeDTO>> inventoryClassControllerFindAllType(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo) throws Exception {
        var requestBuilder = get(
                "/v1/inventory/class/types"
        )
                .accept(MediaType.APPLICATION_JSON);
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<String> inventoryElementControllerCreateNewDomain(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            NewInventoryDomainDTO inventoryDomainDTO) throws Exception {
        var requestBuilder = post("/v1/inventory/domain")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                        new ObjectMapper().writeValueAsString(
                                inventoryDomainDTO
                        )
                );
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<InventoryDomainDTO> inventoryElementControllerFindDomainById(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            String domainId) throws Exception {
        var requestBuilder = get(
                "/v1/inventory/domain/{domainId}",
                domainId
        )
                .accept(MediaType.APPLICATION_JSON);
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<List<InventoryDomainSummaryDTO>> inventoryElementControllerFindAllDomain(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo) throws Exception {
        var requestBuilder = get("/v1/inventory/domain")
                .accept(MediaType.APPLICATION_JSON);
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<String> inventoryElementControllerUpdateDomain(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            String domainId,
            UpdateDomainDTO updateDomainDTO) throws Exception {
        var requestBuilder = put("/v1/inventory/domain/{domainId}", domainId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                        new ObjectMapper().writeValueAsString(
                                updateDomainDTO
                        )
                );
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<String> inventoryElementControllerCreateNewElement(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            String domainId,
            NewInventoryElementDTO newInventoryElementDTO) throws Exception {
        var requestBuilder = post("/v1/inventory/domain/{domainId}/element",domainId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                        new ObjectMapper().writeValueAsString(
                                newInventoryElementDTO
                        )
                );
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<String> inventoryElementControllerUpdateElement(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            String domainId,
            String elementId,
            UpdateInventoryElementDTO updateInventoryElementDTO) throws Exception {
        var requestBuilder = put("/v1/inventory/domain/{domainId}/element/{elementId}",domainId, elementId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                        new ObjectMapper().writeValueAsString(
                                updateInventoryElementDTO
                        )
                );
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<InventoryElementDTO> inventoryElementControllerFindElementById(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            String domainId,
            String elementId) throws Exception {
        var requestBuilder = get("/v1/inventory/domain/{domainId}/element/{elementId}",domainId, elementId)
                .accept(MediaType.APPLICATION_JSON);
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public ApiResultResponse<List<InventoryElementSummaryDTO>> inventoryElementControllerFindAllChildrenByRootId(
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            String domainId,
            String elementId) throws Exception {
        var requestBuilder = get("/v1/inventory/domain/{domainId}/element/{elementId}/children",domainId, elementId)
                .accept(MediaType.APPLICATION_JSON);
        return executeHttpRequest(
                new TypeReference<>() {
                },
                mockMvc,
                resultMatcher,
                userInfo,
                requestBuilder
        );
    }

    public <T> ApiResultResponse<T> executeHttpRequest(
            TypeReference<ApiResultResponse<T>> typeRef,
            MockMvc mockMvc,
            ResultMatcher resultMatcher,
            Optional<String> userInfo,
            MockHttpServletRequestBuilder requestBuilder) throws Exception {
        userInfo.ifPresent(login -> requestBuilder.header(appProperties.getUserHeaderName(), jwtHelper.generateJwt(login)));

        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(resultMatcher)
                .andReturn();
        //Optional<ControllerLogicException> someException = Optional.ofNullable((ControllerLogicException) result.getResolvedException());
        if (result.getResolvedException() != null) {
            throw result.getResolvedException();
        }
        return new ObjectMapper().readValue(result.getResponse().getContentAsString(), typeRef);
    }

}
