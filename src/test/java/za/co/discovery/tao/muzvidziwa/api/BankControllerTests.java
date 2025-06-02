package za.co.discovery.tao.muzvidziwa.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.discovery.tao.muzvidziwa.api.controller.BankController;
import za.co.discovery.tao.muzvidziwa.api.controller.impl.BankControllerImpl;
import za.co.discovery.tao.muzvidziwa.domain.exception.BankServiceException;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;
import za.co.discovery.tao.muzvidziwa.domain.service.BankService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BankControllerImpl.class, BankController.class})
public class BankControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankService bankService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("""
            GET /queryTransactionalBalances - Receives AtmResponse
            """)
    @Test
    void testQueryTransactionalBalances__givenSUccessScenario_shouldReturnOkResponse() throws java.lang.Exception {
        AtmResponse response = new AtmResponse();
        Mockito.when(bankService.getTransactionalClientAccountBalances(anyString(), eq(1)))
                .thenReturn(response);

        mockMvc.perform(get("/queryTransactionalBalances")
                        .param("clientId", "1"))
                .andExpect(status().isOk());
    }

    @DisplayName("""
            test 'GET /queryTransactionalBalances' - Receives BankServiceException
            """)
    @Test
    void testQueryTransactionalBalances_givenBankServiceException_shouldReturnBadRequestErrorResponse() throws Exception {
        Mockito.when(bankService.getTransactionalClientAccountBalances(anyString(), eq(1)))
                .thenThrow(new BankServiceException("Service level exception"));

        mockMvc.perform(get("/queryTransactionalBalances")
                        .param("clientId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Service level exception"));
    }

    @DisplayName("""
            test 'GET /queryTransactionalBalances' - Receives BankServiceException
            """)
    @Test
    void testQueryTransactionalBalances_givenUnspecifiedException_shouldReturnInternalServerErrorResponse() throws Exception {
        Mockito.when(bankService.getTransactionalClientAccountBalances(anyString(), eq(1)))
                .thenThrow(new Exception("Unspecified error"));

        mockMvc.perform(get("/queryTransactionalBalances")
                        .param("clientId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unspecified error"));
    }

    @DisplayName("""
            GET /queryCcyBalances - Receives AtmResponse
            """)
    @Test
    void testQueryCcyBalances_givenSuccessScenario_shouldReturnOkResponse() throws Exception {
        AtmResponse response = new AtmResponse();
        Mockito.when(bankService.getForexAccountBalances(anyString(), eq(2)))
                .thenReturn(response);

        mockMvc.perform(get("/queryCcyBalances")
                        .param("clientId", "2"))
                .andExpect(status().isOk());
    }

    @DisplayName("""
            test 'GET /queryCcyBalances' - Receives BankServiceException
            """)
    @Test
    void testQueryCcyBalances_givenBankServiceException_shouldReturnBadRequestResponse() throws Exception {
        Mockito.when(bankService.getForexAccountBalances(anyString(), eq(2)))
                .thenThrow(new BankServiceException("Service level exception"));

        mockMvc.perform(get("/queryCcyBalances")
                        .param("clientId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Service level exception"));
    }

    @DisplayName("""
            test 'GET /queryCcyBalances' - Receives Exception
            """)
    @Test
    void testQueryCcyBalances_givenUnspecifiedException_shouldReturnInternalServerErrorResponse() throws Exception {
        Mockito.when(bankService.getForexAccountBalances(anyString(), eq(2)))
                .thenThrow(new Exception("Unspecified error"));

        mockMvc.perform(get("/queryCcyBalances")
                        .param("clientId", "2"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unspecified error"));
    }

    @DisplayName("""
            test 'POST /withdrawal' - Receives AtmResponse
            """)
    @Test
    void testPostWithdrawal_givenSuccessScenario_shouldReturnOkResponse() throws Exception {
        AtmResponse response = new AtmResponse();
        Mockito.when(bankService.postWithdrawal(anyString(), eq(1), eq(10), eq("123456"), eq(new BigDecimal("500"))))
                .thenReturn(response);

        mockMvc.perform(post("/withdraw")
                        .param("clientId", "1")
                        .param("atmId", "10")
                        .param("accountNumber", "123456")
                        .param("requiredAmount", "500"))
                .andExpect(status().isOk());
    }

    @DisplayName("""
            test 'POST /withdrawal' - Receives BankServiceException
            """)
    @Test
    void testPostWithdrawal_givenBankServiceException_shouldReturnBadRequestResponse() throws java.lang.Exception {
        Mockito.when(bankService.postWithdrawal(anyString(), eq(1), eq(10), eq("123456"), eq(new BigDecimal("500"))))
                .thenThrow(new BankServiceException("Service level exception"));

        mockMvc.perform(post("/withdraw")
                        .param("clientId", "1")
                        .param("atmId", "10")
                        .param("accountNumber", "123456")
                        .param("requiredAmount", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Service level exception"));
    }

    @DisplayName("""
            test 'POST /withdrawal/ - Receives Exception
            """)
    @Test
    void testPostWithdrawal_givenUnspecifiedException_shouldReturnInternalServerResponse() throws java.lang.Exception {
        Mockito.when(bankService.postWithdrawal(anyString(), eq(1), eq(10), eq("123456"), eq(new BigDecimal("500"))))
                .thenThrow(new Exception("Unspecified error"));

        mockMvc.perform(post("/withdraw")
                        .param("clientId", "1")
                        .param("atmId", "10")
                        .param("accountNumber", "123456")
                        .param("requiredAmount", "500"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unspecified error"));
    }
}