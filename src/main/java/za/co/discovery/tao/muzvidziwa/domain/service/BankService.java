package za.co.discovery.tao.muzvidziwa.domain.service;

import za.co.discovery.tao.muzvidziwa.domain.exception.BankServiceException;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;

import java.math.BigDecimal;

public interface BankService {

    /**
     * TODO: Insert a description of the method here.
     *
     * @param clientId the client ID for which to retrieve transactional balances
     * @return AtmResponse containing the client's transactional balances
     * */
    AtmResponse getTransactionalClientAccountBalances(final String traceId, final Integer clientId) throws Exception;
    AtmResponse getForexAccountBalances(final String traceId, final Integer clientId) throws Exception;
    AtmResponse postWithdrawal(final String traceId, final Integer clientId, final Integer atmId, final String accountNumber, final BigDecimal amount) throws Exception;
}
