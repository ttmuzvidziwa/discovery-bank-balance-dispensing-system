package za.co.discovery.tao.muzvidziwa.api.controller.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.discovery.tao.muzvidziwa.api.controller.BankController;
import za.co.discovery.tao.muzvidziwa.domain.exception.BankServiceException;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;
import za.co.discovery.tao.muzvidziwa.domain.service.BankService;
import za.co.discovery.tao.muzvidziwa.domain.util.GeneralUtils;
import za.co.discovery.tao.muzvidziwa.domain.util.LoggerUtils;

import java.math.BigDecimal;

import static za.co.discovery.tao.muzvidziwa.api.constant.UriConstants.GET_FOREX_ACCOUNT_BALANCE_URL;
import static za.co.discovery.tao.muzvidziwa.api.constant.UriConstants.GET_TRANSACTIONAL_ACCOUNT_BALANCE_URL;
import static za.co.discovery.tao.muzvidziwa.api.constant.UriConstants.POST_WITHDRAWAL_URL;
import static za.co.discovery.tao.muzvidziwa.domain.constant.Sources.BANK_CONTROLLER;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Bank Balance and Dispensing System", description = "Handles client operations including transactional balances, forex balances, and withdrawals")
public class BankControllerImpl implements BankController {
    private final BankService bankService;

    /**
     * This endpoint retrieves the transactional balance for a given client ID.
     * The Client can view all transactional accounts with the available balances on each account
     *
     * @param clientId The ID of the client for whom the balance is being requested
     * @return ResponseEntity {@link AtmResponse} containing the balance information or an error message
     */
    @Override
    @GetMapping(GET_TRANSACTIONAL_ACCOUNT_BALANCE_URL)
    public ResponseEntity<Object> getTransactionalBalance(final Integer clientId) {
        final String traceId = GeneralUtils.generateUniqueId();
        LoggerUtils.logInfo(traceId, BANK_CONTROLLER, "Received request to get transactional balance for client ID: {}", clientId);

        try {
            final AtmResponse response = bankService.getTransactionalClientAccountBalances(traceId, clientId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BankServiceException ex) {
            log.error("Error retrieving transactional balance for client ID {}: {}", clientId, ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a list of forex accounts converted to ZAR balances for a specific client.
     *
     * @param clientId The ID of the client for whom the forex account balances are being requested.
     * @return ResponseEntity containing {@link AtmResponse} with the forex balances or an error message.
     */
    @Override
    @GetMapping(GET_FOREX_ACCOUNT_BALANCE_URL)
    public ResponseEntity<Object> getForexAccountBalance(final Integer clientId) {
        final String traceId = GeneralUtils.generateUniqueId();
        LoggerUtils.logInfo(traceId, BANK_CONTROLLER, "Received request to get forex account balance for client ID: {}", clientId);

        try {
            final AtmResponse response = bankService.getForexAccountBalances(traceId, clientId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (final BankServiceException ex) {
            log.error("Error retrieving forex account balance for client ID {}: {}", clientId, ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Performs a withdrawal operation for a client from a specific ATM.
     *
     * @param clientId       The ID of the client performing the withdrawal.
     * @param atmId          The ID of the ATM from which the withdrawal is being made.
     * @param accountNumber  The account number from which the funds will be withdrawn.
     * @param requiredAmount The amount to be withdrawn.
     * @return ResponseEntity containing {@link AtmResponse} with the result of the withdrawal or an error message.
     */
    @Override
    @PostMapping(POST_WITHDRAWAL_URL)
    public ResponseEntity<Object> postWithdrawal(final Integer clientId, final Integer atmId, final String accountNumber, final BigDecimal requiredAmount) {
        final String traceId = GeneralUtils.generateUniqueId();
        LoggerUtils.logInfo(traceId, BANK_CONTROLLER, "Received request to perform a withdrawal for client ID: {}, account number: {}", clientId, accountNumber);

        try {
            final AtmResponse response = bankService.postWithdrawal(traceId, clientId, atmId, accountNumber, requiredAmount);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (final BankServiceException ex) {
            log.error("Error performing withdrawal for client ID {}: {}", clientId, ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Exception ex) {
            log.error("Unexpected error during withdrawal for client ID {}: {}", clientId, ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
