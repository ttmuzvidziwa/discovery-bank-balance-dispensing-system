package za.co.discovery.tao.muzvidziwa.domain.service;

import za.co.discovery.tao.muzvidziwa.domain.exception.BankServiceException;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;

import java.math.BigDecimal;

public interface BankService {

    /**
     * Retrieves the transactional account(s) balances for a specific client.
     *
     * @param traceId  a unique identifier for tracing the request
     * @param clientId the ID of the client whose transactional balances are to be retrieved
     * @return an {@link AtmResponse} containing the client's transactional account balances
     * @throws Exception if an error occurs while retrieving the balances
     */
    AtmResponse getTransactionalClientAccountBalances(final String traceId, final Integer clientId) throws Exception;

    /**
     * Retrieves the foreign exchange (forex) account balances, with converted ZAR balances for a specific client.
     *
     * @param traceId  a unique identifier for tracing the request
     * @param clientId the ID of the client whose forex account balances are to be retrieved
     * @return an {@link AtmResponse} containing the client's forex account balances
     * @throws Exception if an error occurs while retrieving the balances
     */
    AtmResponse getForexAccountBalances(final String traceId, final Integer clientId) throws Exception;

    /**
     * Processes a withdrawal request for a specific client and account at a given ATM.
     *
     * @param traceId       a unique identifier for tracing the request
     * @param clientId      the ID of the client making the withdrawal
     * @param atmId         the ID of the ATM where the withdrawal is made
     * @param accountNumber the account number from which the withdrawal is to be made
     * @param amount        the amount to withdraw
     * @return an {@link AtmResponse} containing the result of the withdrawal operation
     * @throws Exception if an error occurs during the withdrawal process
     */
    AtmResponse postWithdrawal(final String traceId, final Integer clientId, final Integer atmId, final String accountNumber, final BigDecimal amount) throws Exception;
}
