package edu.stanford.slac.code_inventory_system.api.v1.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.auth.JWTHelper;
import edu.stanford.slac.ad.eed.baselib.config.AppProperties;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.code_inventory_system.api.v1.dto.InventoryClassDTO;
import edu.stanford.slac.code_inventory_system.api.v1.dto.NewInventoryClassDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
        var getBuilder = post(
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
                new TypeReference<>() {},
                mockMvc,
                resultMatcher,
                userInfo,
                getBuilder
        );
    }

    public ApiResultResponse<InventoryClassDTO> inventoryClassControllerFindById(MockMvc mockMvc, ResultMatcher resultMatcher, Optional<String> userInfo, String id) throws Exception {
        var getBuilder = get(
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
                getBuilder
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
        Optional<ControllerLogicException> someException = Optional.ofNullable((ControllerLogicException) result.getResolvedException());
        if (someException.isPresent()) {
            throw someException.get();
        }
        return new ObjectMapper().readValue(result.getResponse().getContentAsString(),typeRef);
    }

}
