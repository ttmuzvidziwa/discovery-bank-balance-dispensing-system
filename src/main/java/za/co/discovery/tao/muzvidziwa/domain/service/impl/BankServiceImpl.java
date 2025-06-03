package za.co.discovery.tao.muzvidziwa.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import za.co.discovery.tao.muzvidziwa.domain.constant.StatusCodeReason;
import za.co.discovery.tao.muzvidziwa.domain.exception.BankServiceException;
import za.co.discovery.tao.muzvidziwa.domain.model.cache.CurrencyConversionCache;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.AtmAllocationUpdateDto;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.AtmStateDto;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.AtmAllocation;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Client;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientAccount;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.AccountDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ClientDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.DenominationDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ResultDto;
import za.co.discovery.tao.muzvidziwa.domain.service.BankService;
import za.co.discovery.tao.muzvidziwa.domain.util.GeneralUtils;
import za.co.discovery.tao.muzvidziwa.domain.util.LoggerUtils;
import za.co.discovery.tao.muzvidziwa.repository.AtmAllocationRepository;
import za.co.discovery.tao.muzvidziwa.repository.AtmRepository;
import za.co.discovery.tao.muzvidziwa.repository.ClientAccountRepository;
import za.co.discovery.tao.muzvidziwa.repository.ClientRepository;
import za.co.discovery.tao.muzvidziwa.repository.CreditCardLimitRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static za.co.discovery.tao.muzvidziwa.domain.constant.Sources.BANK_SERVICE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final ClientAccountRepository clientAccountRepository;
    private final AtmRepository atmRepository;
    private final AtmAllocationRepository atmAllocationRepository;
    private final ClientRepository clientRepository;
    private final CreditCardLimitRepository creditCardLimitRepository;

    private final CurrencyConversionCache currencyConversionCache;

    @Value("${application-config.overdraft-limit}")
    private String overdraftLimit;

    @Override
    public AtmResponse getTransactionalClientAccountBalances(final String traceId, final Integer clientId) throws Exception {
        LoggerUtils.logInfo(traceId, BANK_SERVICE, "Retrieving transactional balances for client ID: {}", clientId);

        final AtmResponse atmResponse = new AtmResponse();
        if (!GeneralUtils.isPositiveInteger(clientId)) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "Invalid client ID: {}", clientId);
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.INVALID_CLIENT_IDENTIFIER_SCR));
            return atmResponse;
        }

        /* It was determined that it is possible for a CLIENT to have no transactional (also any) CLIENT_ACCOUNTS associated
        *  with their profile necessitating the need to pull the CLIENT's details separately. */
        final Optional<Client> clientDetails = clientRepository.findByClientId(clientId);
        if (clientDetails.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No client found with ID: {}", clientId);
            // It was determined that the system should return a NO_ACCOUNT_TO_DISPLAY_SCR status code reason
            // It was determined that the accounts should be set to an empty list to avoid NPEs in downstream systems, and follow the principle of least surprise
            atmResponse.setClient(new ClientDto());
            atmResponse.setAccounts(List.of());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }
        atmResponse.setClient(prepareClientDto(clientDetails.get()));

        final Optional<List<ClientAccount>> clientAccountList = clientAccountRepository.findTransactionalClientAccountsByClientId(clientId.longValue());

        if (clientAccountList.isEmpty() || clientAccountList.get().isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No accounts found for client ID: {}", clientId);
            // It was determined that the system should return a NO_ACCOUNT_TO_DISPLAY_SCR status code reason
            // It was determined that the accounts should be set to an empty list to avoid NPEs in downstream systems, and follow the principle of least surprise
            atmResponse.setAccounts(List.of());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }

        // It was determined that the accounts should be sorted by balance in descending order
        final List<AccountDto> sortedAccounts = sortClientAccountsByBalanceDescending(clientAccountList.get(), traceId);
        if (sortedAccounts.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No valid transactional accounts found for client ID: {}", clientId);
            atmResponse.setAccounts(List.of());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }
        atmResponse.setAccounts(sortedAccounts);
        atmResponse.setResult(prepareResultDto(true, StatusCodeReason.DISPLAY_TRANSACTIONAL_ACCOUNTS_SCR));

        return atmResponse;
    }

    @Override
    public AtmResponse getForexAccountBalances(final String traceId, final Integer clientId) throws Exception {
        LoggerUtils.logInfo(traceId, BANK_SERVICE, "Retrieving forex balances for client ID: {}", clientId);

        final AtmResponse atmResponse = new AtmResponse();
        if (!GeneralUtils.isPositiveInteger(clientId)) {
            LoggerUtils.logError(traceId, BANK_SERVICE, "Invalid client ID: {}", clientId);
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.INVALID_CLIENT_IDENTIFIER_SCR));
            return atmResponse;
        }

        final Optional<Client> clientDetails = clientRepository.findByClientId(clientId);
        if (clientDetails.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No client found with ID: {}", clientId);
            atmResponse.setClient(new ClientDto());
            atmResponse.setAccounts(List.of());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }
        atmResponse.setClient(prepareClientDto(clientDetails.get()));

        final Optional<List<ClientAccount>> clientAccountList = clientAccountRepository.findClientAccountsByClientIdAndAccountType(clientId.longValue(), "CFCA");

        if (clientAccountList.isEmpty() || clientAccountList.get().isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No accounts found for client ID: {}", clientId);
            atmResponse.setAccounts(List.of());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }

        final List<AccountDto> sortedAccounts = sortClientAccountsByZarBalanceAscending(clientAccountList.get(), traceId);
        if (sortedAccounts.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No valid CFC accounts found for client ID: {}", clientId);
            atmResponse.setAccounts(List.of());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }

        atmResponse.setAccounts(sortedAccounts);
        atmResponse.setResult(prepareResultDto(true, StatusCodeReason.DISPLAY_FOREIGN_CURRENCY_ACCOUNT_SCR));

        return atmResponse;
    }

    @Override
    public AtmResponse postWithdrawal(final String traceId, final Integer clientId, Integer atmId, final String accountNumber, final BigDecimal withdrawalAmount) throws Exception {
        LoggerUtils.logInfo(traceId, BANK_SERVICE, "Processing withdrawal for client ID: {}, account number: {}, amount: {}", clientId, accountNumber, withdrawalAmount);

        final AtmResponse atmResponse = new AtmResponse();
        if (!GeneralUtils.isPositiveInteger(clientId)) {
            LoggerUtils.logError(traceId, BANK_SERVICE, "Invalid client ID: {}", clientId);
            atmResponse.setClient(new ClientDto());
            atmResponse.setAccount(new AccountDto());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.INVALID_CLIENT_IDENTIFIER_SCR));
            return atmResponse;
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            LoggerUtils.logError(traceId, BANK_SERVICE, "Invalid account number: {}", accountNumber);
            atmResponse.setClient(new ClientDto());
            atmResponse.setAccount(new AccountDto());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.INVALID_CLIENT_ACCOUTN_NUMBER_SCR));
            return atmResponse;
        }
        if (!GeneralUtils.isPositiveBigDecimal(withdrawalAmount)) {
            LoggerUtils.logError(traceId, BANK_SERVICE, "Invalid withdrawal amount: {}", withdrawalAmount);
            atmResponse.setClient(new ClientDto());
            atmResponse.setAccount(new AccountDto());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.INVALID_WITHDRAWAL_AMOUNT_SCR));
            return atmResponse;
        }

        /* Establish if the ATM exist higher in the logic to stop executing the remainder of the logic since the withdrawal
        *  will not be possible without an ATM identified*/
        if (!atmRepository.atmExistsByAtmId(atmId.longValue())) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No ATM found with ID: {}", atmId);
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.ATM_NOT_FOUND_UNFUNDED_SCR));
            return atmResponse;
        }

        final Optional<Client> clientDetails = clientRepository.findByClientId(clientId);
        if (clientDetails.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No client found with ID: {}", clientId);
            atmResponse.setClient(new ClientDto());
            atmResponse.setAccount(new AccountDto());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }
        atmResponse.setClient(prepareClientDto(clientDetails.get()));

        final Optional<ClientAccount> clientAccountOptional = clientAccountRepository.findClientAccountByClientIdAndAccountNumber(clientId.longValue(), accountNumber);
        if (clientAccountOptional.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No account found for client ID: {}, account number: {}", clientId, accountNumber);
            atmResponse.setAccount(new AccountDto());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }

        ClientAccount clientAccount = clientAccountOptional.get();

        // TODO Tao: Remember that CHQ accounts have an overdraft facility of R10 000.00, therefore using the displayBalance is not correct but should instead use the accountLimit
        if (clientAccount.getDisplayBalance() != null) {
            /* Establish if the client's accoutn is cheque account, if it is, add the overdraft limit to the display balance before checking if the
            *  requested withdrawal amount is available */
            BigDecimal availableBalance;
            if (clientAccount.getAccountType() != null && clientAccount.getAccountType().getAccountTypeCode().equalsIgnoreCase("CHQ")) {
                availableBalance = GeneralUtils.parseBigDecimal(overdraftLimit).add(clientAccount.getDisplayBalance());
            } else {
                availableBalance = clientAccount.getDisplayBalance();
            }
            if (availableBalance.compareTo(withdrawalAmount) < 0) {
                LoggerUtils.logDebug(traceId, BANK_SERVICE, "Insufficient funds for account number: {}, balance: {}, requested amount: {}", accountNumber, clientAccount.getDisplayBalance(), withdrawalAmount);
                atmResponse.setAccount(sortClientAccountsByBalanceDescending(List.of(clientAccount), traceId).get(0));
                atmResponse.setResult(prepareResultDto(false, StatusCodeReason.INSUFFICIENT_FUNDS_SCR));
                return atmResponse;
            }
        } else {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "Account number: {} has no display balance.", accountNumber);
            atmResponse.setAccount(new AccountDto());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.NO_ACCOUNT_TO_DSPLAY_SCR));
            return atmResponse;
        }

        // Withdrawal logic
        final Optional<List<AtmAllocation>> atmAllocationList = atmAllocationRepository.findAtmAllocationByAtmId(atmId.longValue());

        if (atmAllocationList.isEmpty() || atmAllocationList.get().isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No ATM_ID {} is not registered or has no allocation.", atmId);
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.ATM_NOT_FOUND_UNFUNDED_SCR));
            return atmResponse;
        }

        final AtmStateDto atmStateDto = prepareDenominationMap(atmAllocationList.get(), traceId);
        List<DenominationDto> dispensedDenominations;
        try {
            dispensedDenominations = dispenseCash(atmStateDto, withdrawalAmount, traceId);
        } catch (final BankServiceException bSE) {
            atmResponse.setAccount(sortClientAccountsByBalanceDescending(List.of(clientAccount), traceId).get(0));
            atmResponse.setDenomination(List.of());
            atmResponse.setResult(prepareResultDto(false, 400, bSE.getMessage()));
            return atmResponse;
        }

        try {
            atmAllocationRepository.updateDenominationCounts(atmId.longValue(), atmStateDto.getAtmAllocationUpdateList());
            clientAccountRepository.updateClientAccountByAccountNumber(clientId, accountNumber, clientAccount.getDisplayBalance().subtract(withdrawalAmount));
        } catch (final java.lang.Exception e) {
            LoggerUtils.logError(traceId, BANK_SERVICE, "Error updating ATM allocation or client account: {}", e.getMessage());
            atmResponse.setResult(prepareResultDto(false, StatusCodeReason.GENERAL_ERROR_SCR));
            return atmResponse;
        }

        /* It was determined that a withdrawal is successful if:
            1 . the ATM_ALLOCATION for the ATM was updated successfully, the system can proceed to update the CLIENT's displayBalance.
            2.  the CLIENT_ACCOUNT balance for the CLIENT was updated successfully, there is therefor
                no reason to query the CLIENT_ACCOUNT table again to fetch the displayBalance but rather to update it in memory
                for `some` performance gains.
            therefor the system can proceed to prepare and the response payload to the ATM terminal */
        clientAccount.setDisplayBalance(clientAccount.getDisplayBalance().subtract(withdrawalAmount));

        atmResponse.setAccount(sortClientAccountsByBalanceDescending(List.of(clientAccount), traceId).get(0));
        atmResponse.setDenomination(dispensedDenominations);
        atmResponse.setResult(prepareResultDto(true, StatusCodeReason.WITHDRAWAL_SUCCESSFUL_SCR));

        return atmResponse;
    }

    /**
     * This method prepares a {@link ClientDto} from a {@link ClientAccount} object.
     * It extracts relevant information such as client ID, title, name, and surname.
     *
     * @param client The {@link Client} object containing the client's personal information
     * @return A {@link ClientDto} containing the client's personal details
     */
    private ClientDto prepareClientDto(final Client client) {
        ClientDto clientDto = new ClientDto();
        if (client != null) {
            clientDto = new ClientDto();

            clientDto.setId(client.getClientId() != null ? GeneralUtils.parseIntToLong(client.getClientId()) : null);
            clientDto.setTitle(client.getTitle() != null ? client.getTitle() : null);
            clientDto.setName(client.getName() != null ? client.getName() : null);
            clientDto.setSurname(client.getSurname() != null ? client.getSurname() : null);
        }

        return clientDto;
    }

    /**
     * This method prepares an {@link AccountDto} from a {@link ClientAccount} object.
     * It extracts relevant information such as account number, type, currency, balance, and conversion rate.
     *
     * @param clientAccount The {@link ClientAccount} object to be converted
     * @param traceId Unique identifier for logging
     * @return An {@link AccountDto} containing the account details
     */
    private AccountDto prepareClientAccountDto(final ClientAccount clientAccount, final String traceId) {
        AccountDto clientAccountDto = new AccountDto();
        if (clientAccount != null) {
            // It was determined that should the data for an account not be complete or be inconsistent, the system will
            // treat this account as invalid and return a null object
            if (clientAccount.getClientAccountNumber() == null
                    || clientAccount.getClientAccountNumber().isBlank()
                    || GeneralUtils.parseStringToLong(clientAccount.getClientAccountNumber()) == null) {
                return null;
            }
            clientAccountDto.setAccountNumber(GeneralUtils.parseStringToLong(clientAccount.getClientAccountNumber()));

            if (clientAccount.getAccountType() == null || clientAccount.getAccountType().getAccountTypeCode() == null) {
                return null;
            }
            clientAccountDto.setTypeCode(clientAccount.getAccountType().getAccountTypeCode());

            if (clientAccount.getAccountType().getDescription() == null) {
                return null;
            }
            clientAccountDto.setAccountTypeDescription(clientAccount.getAccountType().getDescription());

            if (clientAccount.getCurrency() == null || clientAccount.getCurrency().getCurrencyCode() == null) {
                return null;
            }
            clientAccountDto.setCurrencyCode(clientAccount.getCurrency().getCurrencyCode());

            // TODO Tao: You will need to remove the call to parse BigDecimal when the ConversionRateDto.conversionRate is changed to BigDecimal
            if (currencyConversionCache.getCurrencyConversionRate(clientAccount.getCurrency().getCurrencyCode(), traceId) == null
                    || currencyConversionCache.getCurrencyConversionRate(clientAccount.getCurrency().getCurrencyCode(), traceId).getConversionRate() == null
                    || GeneralUtils.parseBigDecimal(
                            currencyConversionCache.getCurrencyConversionRate(clientAccount.getCurrency().getCurrencyCode(), traceId).getConversionRate()) == null) {
                return null;
            }
            clientAccountDto.setConversionRate(
                    GeneralUtils.parseBigDecimal(
                            currencyConversionCache.getCurrencyConversionRate(clientAccount.getCurrency().getCurrencyCode(), traceId).getConversionRate()).setScale(3, RoundingMode.HALF_UP));

            /* It has been determined that the ZAR balance is:
             *  1. for ZAR balances, is going to be the same as the display balance,
             *  2. for forex balances, is going to be a product (by CONVERSION_INDICATOR) of the going CURRENCY_CONVERSION_RATE rate
             *     of the currency in question */
            if (clientAccount.getDisplayBalance() == null) {
                return null;
            }
            if (clientAccount.getCurrency().getCurrencyCode().equalsIgnoreCase("ZAR")) {
                clientAccountDto.setBalance(clientAccount.getDisplayBalance().setScale(3, RoundingMode.HALF_UP));
                clientAccountDto.setZarBalance(clientAccount.getDisplayBalance().setScale(3, RoundingMode.HALF_UP));
            } else {
                clientAccountDto.setCcyBalance(clientAccount.getDisplayBalance().setScale(3, RoundingMode.HALF_UP));
                BigDecimal convertedZarBalance = null;
                switch (currencyConversionCache.getCurrencyConversionRate(clientAccount.getCurrency().getCurrencyCode(), traceId).getConversionIndicator()) {
                    case ("/") -> convertedZarBalance = clientAccount.getDisplayBalance()
                            .divide(
                                    GeneralUtils.parseBigDecimal(
                                            currencyConversionCache.getCurrencyConversionRate(
                                                    clientAccount.getCurrency().getCurrencyCode(), traceId).getConversionRate()),
                                    3, RoundingMode.HALF_UP);
                    case ("*") -> convertedZarBalance = clientAccount.getDisplayBalance()
                            .multiply(
                                    GeneralUtils.parseBigDecimal(
                                            currencyConversionCache.getCurrencyConversionRate(
                                                    clientAccount.getCurrency().getCurrencyCode(), traceId).getConversionRate()));
                }

                clientAccountDto.setZarBalance(convertedZarBalance != null ? convertedZarBalance.setScale(3, RoundingMode.HALF_UP) : null);
            }

            // It has been determined that all cheque accounts have an overdraft facility of R10 000.00
            // An assumption has been made that the overdraft limit is only applicable to ZAR accounts
            if (clientAccount.getAccountType() == null || clientAccount.getAccountType().getAccountTypeCode() == null) {
                return null;
            }
            if (clientAccount.getAccountType().getAccountTypeCode().equalsIgnoreCase("CHQ")
                    && clientAccount.getCurrency().getCurrencyCode().equalsIgnoreCase("ZAR")) {
                // The accountLimit is the sum of the display balance and the overdraft facility
                // i.e., if the display balance is R-500.00, the account limit is R10 000.00 + (- R500.00) = R9 500.00
                final BigDecimal accountLimit = GeneralUtils.parseBigDecimal(overdraftLimit)
                        .add(clientAccount.getDisplayBalance() != null ? clientAccount.getDisplayBalance() : BigDecimal.ZERO);

                clientAccountDto.setAccountLimit(accountLimit.setScale(3, RoundingMode.HALF_UP));
            } else if (clientAccount.getAccountType().getAccountTypeCode().equalsIgnoreCase("CCRD")
                    && clientAccount.getCurrency().getCurrencyCode().equalsIgnoreCase("ZAR")) {
                // For credit card accounts (CCRD), the account limit is pulled from the CREDIT_CARD_LIMIT table and set to the account limit field
                final Optional<BigDecimal> creditCardLimit = creditCardLimitRepository.findCreditCardLimitByClientAccountNumber(clientAccount.getClientAccountNumber());
                if (creditCardLimit.isEmpty()) {
                    LoggerUtils.logDebug(traceId, BANK_SERVICE, "No credit card limit found for account number: {}", clientAccount.getClientAccountNumber());
                    return null;
                }

                // It has been assumed that the zarBalance would depict the amount the client has used off of their credit card limit
                clientAccountDto.setZarBalance(clientAccount.getDisplayBalance().subtract(creditCardLimit.get()).setScale(3, RoundingMode.HALF_UP));
                clientAccountDto.setAccountLimit(creditCardLimit.get().setScale(3, RoundingMode.HALF_UP));
            } else if (clientAccount.getAccountType().getAccountTypeCode().equalsIgnoreCase("CFCA")) {
                // For forex accounts (CFCA) accounts, the account limit is set to the ccyBalance
                clientAccountDto.setAccountLimit(clientAccountDto.getCcyBalance().setScale(3, RoundingMode.HALF_UP));
            } else {
                // For loan and savings accounts, the account limit is set to the display balance
                clientAccountDto.setAccountLimit(clientAccountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
            }
        }

        return clientAccountDto;
    }

    /**
     * This method sorts the client accounts by balance in descending order.
     * If the balance is null, it is treated as zero for sorting purposes.
     *
     * @param clientAccountList List of {@link ClientAccount} objects to be sorted
     * @param traceId Unique identifier for logging
     * @return List of {@link AccountDto} sorted by balance in descending order
     */
    private List<AccountDto> sortClientAccountsByBalanceDescending(final List<ClientAccount> clientAccountList, final String traceId) {
        if (clientAccountList == null || clientAccountList.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No client accounts to sort.");
            return List.of();
        }

        final List<AccountDto> sortedAccountDtoList = new ArrayList<>();
        for (ClientAccount clientAccount : clientAccountList) {
            final AccountDto accountDto = prepareClientAccountDto(clientAccount, traceId);

            if (accountDto == null) {
                LoggerUtils.logDebug(traceId, BANK_SERVICE, "Skipping account with null or incomplete data: {}", clientAccount);
                continue;
            }

            // Find the correct position to insert based on balance (descending)
            // An assumption was made that if the balance is null, it should be treated as zero
            int insertIndex = 0;
            while (insertIndex < sortedAccountDtoList.size()) {
                BigDecimal currentBalance = sortedAccountDtoList.get(insertIndex).getBalance();
                BigDecimal newBalance = accountDto.getBalance() != null ? accountDto.getBalance() : BigDecimal.ZERO;
                BigDecimal compareBalance = currentBalance != null ? currentBalance : BigDecimal.ZERO;

                if (newBalance.compareTo(compareBalance) >= 0) {
                    break;
                }
                insertIndex++;
            }
            sortedAccountDtoList.add(insertIndex, accountDto);
        }

        return sortedAccountDtoList;
    }

    /**
     * This method sorts the client accounts by ZAR balance in ascending order.
     * If the ZAR balance is null, it is treated as zero for sorting purposes.
     *
     * @param clientAccountList List of {@link ClientAccount} objects to be sorted
     * @param traceId           Unique identifier for logging
     * @return List of {@link AccountDto} sorted by ZAR balance in ascending order
     */
    private List<AccountDto> sortClientAccountsByZarBalanceAscending(final List<ClientAccount> clientAccountList, final String traceId) {
        if (clientAccountList == null || clientAccountList.isEmpty()) {
            LoggerUtils.logDebug(traceId, BANK_SERVICE, "No client accounts to sort.");
            return List.of();
        }

        final List<AccountDto> sortedAccountDtoList = new ArrayList<>();
        for (ClientAccount clientAccount : clientAccountList) {
            final AccountDto accountDto = prepareClientAccountDto(clientAccount, traceId);

            if (accountDto == null) {
                LoggerUtils.logDebug(traceId, BANK_SERVICE, "Skipping account with null or incomplete data: {}", clientAccount);
                continue;
            }

            // Find the correct position to insert based on zarBalance (ascending)
            // An assumption was made that if the balance is null, it should be treated as zero
            int insertIndex = 0;
            while (insertIndex < sortedAccountDtoList.size()) {
                BigDecimal currentZarBalance = sortedAccountDtoList.get(insertIndex).getZarBalance();
                BigDecimal newZarBalance = accountDto.getZarBalance() != null ? accountDto.getZarBalance() : BigDecimal.ZERO;
                BigDecimal compareZarBalance = currentZarBalance != null ? currentZarBalance : BigDecimal.ZERO;

                if (newZarBalance.compareTo(compareZarBalance) < 0) {
                    break;
                }
                insertIndex++;
            }
            sortedAccountDtoList.add(insertIndex, accountDto);
        }

        return sortedAccountDtoList;
    }

    private ResultDto prepareResultDto(final boolean success, final StatusCodeReason codeReason) {
        ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(success);
        resultDto.setStatusCode(codeReason.statusCode);
        resultDto.setStatusReason(codeReason.statusReason);

        return resultDto;
    }

    private ResultDto prepareResultDto(final boolean success, final int statusCode, final String statusReason) {
        ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(success);
        resultDto.setStatusCode(statusCode);
        resultDto.setStatusReason(statusReason);

        return resultDto;
    }

    private AtmStateDto prepareDenominationMap(final List<AtmAllocation> atmAllocationList, final String traceId) {
        final AtmStateDto atmStateDto = new AtmStateDto();

        BigDecimal atmTotalAmount = BigDecimal.ZERO;
        final Map<BigDecimal, DenominationDto> denominationMap = new HashMap<>();

        for (AtmAllocation atmAllocation : atmAllocationList) {
            if (atmAllocation.getDenomination() != null
                    && atmAllocation.getDenomination().getDenominationValue() != null
                    && atmAllocation.getDenomination().getDenominationId() != null) {

                final long denominationId = GeneralUtils.parseIntToLong(atmAllocation.getDenomination().getDenominationId());

                /* The ATM can only dispense bank-notes, so Denominations that are not bank-notes (e.g., coins) are ignored
                 *       even if they are in the ATM_ALLOCATION list <*/
                if (atmAllocation.getDenomination().getDenominationType() != null
                        && atmAllocation.getDenomination().getDenominationType().getDenominationTypeCode() != null
                        && atmAllocation.getDenomination().getDenominationType().getDenominationTypeCode().equalsIgnoreCase("C")) {
                    continue;
                }

                final BigDecimal denomination = atmAllocation.getDenomination().getDenominationValue();

                // An assumption has been made that if the count is null, it should be treated as zero
                final int count = atmAllocation.getCount() != null ? atmAllocation.getCount() : 0;

                if (denominationMap.containsKey(denomination)) {
                    // In the event that the denomination already exists, increment the count
                    // In keeping with the previous assumption, if the count is null, it should be treated as zero
                    final DenominationDto existingDenomination = denominationMap.get(denomination);
                    final int existingCount = existingDenomination.getCount() != null ? existingDenomination.getCount() : 0;
                    existingDenomination.setCount(existingCount + count);

                    denominationMap.put(denomination, existingDenomination);
                    atmTotalAmount = atmTotalAmount.add(denomination.multiply(BigDecimal.valueOf(existingDenomination.getCount())));
                } else {
                    final DenominationDto newDenomination = new DenominationDto();
                    newDenomination.setDenominationId(denominationId);
                    newDenomination.setDenominationValue(denomination);
                    newDenomination.setCount(count);

                    denominationMap.put(denomination, newDenomination);
                    atmTotalAmount = atmTotalAmount.add(denomination.multiply(BigDecimal.valueOf(count)));
                }
            } else {
                LoggerUtils.logDebug(traceId, BANK_SERVICE, "ATM allocation with null denomination found: {}", atmAllocation);
            }
        }

        atmStateDto.setDenominationMap(denominationMap);
        atmStateDto.setDenominationList(new ArrayList<>(denominationMap.keySet()));
        atmStateDto.setTotalAmount(atmTotalAmount);

        return atmStateDto;
    }

    /**
     * Dispenses cash from the ATM based on the requested withdrawal amount and available denominations.
     *
     * <ul>
     *   <li>1. Validates if the withdrawal amount is a multiple of the smallest denomination available in the ATM.
     *       If not, throws an exception indicating the minimum multiple that can be dispensed.</li>
     *   <li>2. Checks if the ATM has sufficient funds to fulfill the withdrawal. If not, finds and suggests the next
     *       possible lower amount that can be dispensed with the available denominations, and throws an exception with this suggestion.</li>
     *   <li>3. Uses a greedy algorithm to determine the most efficient way to dispense the requested amount using the available denominations and their counts. Further reading <a href="https://www.tutorialspoint.com/data_structures_algorithms/greedy_algorithms.htm">Tutorials Point: Greedy Algorithms</a></li>
     *   <li>4. Updates the {@link AtmStateDto}'s denomination map by deducting the number of bills or coins dispensed, and reflecting the number remaining.</li>
     *   <li>5. Prepares and returns a list of {@link DenominationDto} objects representing the denominations and counts dispensed.</li>
     * </ul>
     *
     * @param atmStateDto      The current state of the ATM {@link AtmStateDto}, including available denominations and their counts.
     * @param withdrawalAmount The amount to withdraw.
     * @param traceId          Unique identifier for logging and tracing.
     * @return List of {@link DenominationDto} representing the denominations and counts dispensed.
     * @throws BankServiceException if the withdrawal cannot be fulfilled due to denomination constraints or insufficient funds.
     */
    private List<DenominationDto> dispenseCash(AtmStateDto atmStateDto, final BigDecimal withdrawalAmount, final String traceId) throws BankServiceException {
        List<BigDecimal> denominationList = new ArrayList<>(atmStateDto.getDenominationList());
        denominationList.sort(Comparator.naturalOrder());
        BigDecimal smallestDenomination = denominationList.get(0);

        /* 1. Check if withdrawal amount is a multiple of the smallest denomination.
         *  i.e., the withdrawal amount is 15 and the smallest denomination is 10, then notify the client that the ATM
         *  will only be able to service their withdrawal in multiples of 10 */
        if (withdrawalAmount.remainder(smallestDenomination).compareTo(BigDecimal.ZERO) != 0) {
            LoggerUtils.logError(traceId, BANK_SERVICE, "Withdrawal amount {} is not a multiple of the smallest denomination {}", withdrawalAmount, smallestDenomination);
            throw new BankServiceException(
                    "ATM can only dispense cash in multiples of " + smallestDenomination.setScale(2, RoundingMode.HALF_UP)
            );
        }

        // 2. Perform a blanket check to see if the ATM has enough funds to dispense the requested amount
        if (atmStateDto.getTotalAmount().compareTo(withdrawalAmount) < 0) {
            // 2.1 If the ATM does not have enough funds, establish the next possible lower amount that can be dispensed by the ATM
            BigDecimal possibleAmount = findNextDispensableAmount(atmStateDto, withdrawalAmount);
            throw new BankServiceException(
                    "Amount not available, would you like to draw " + possibleAmount.setScale(2, RoundingMode.HALF_UP) + "?"
            );
        }

        // 3. Greedy algorithm to dispense cash
        Map<BigDecimal, DenominationDto> denominationMap = atmStateDto.getDenominationMap();
        denominationList.sort(Comparator.reverseOrder());
        BigDecimal remaining = withdrawalAmount;
        List<DenominationDto> dispensedList = new ArrayList<>();

        for (BigDecimal denomination : denominationList) {
            DenominationDto denominationDto = denominationMap.get(denomination);
            int available = denominationDto.getCount() != null ? denominationDto.getCount() : 0;
            int toDispense = remaining.divideToIntegralValue(denomination).intValue();
            int notesToDispense = Math.min(toDispense, available);

            if (notesToDispense > 0) {
                // 4. Update ATMStateDto's map to indicate the number of notes remaining after dispensing cash
                atmStateDto.addAtmAllocationUpdate(new AtmAllocationUpdateDto(denominationDto.getDenominationId(), (available - notesToDispense)));

                // 4.1 Prepare a list of dispensed denominations
                DenominationDto dispensed = new DenominationDto();
                dispensed.setDenominationId(denominationDto.getDenominationId());
                dispensed.setDenominationValue(denomination);
                dispensed.setCount(notesToDispense);
                dispensedList.add(dispensed);

                remaining = remaining.subtract(denomination.multiply(BigDecimal.valueOf(notesToDispense)));
            }
        }

        /* 5. In the event that the required amount could not be dispensed because the ATM does not have the correct denominations to
             satisfy the requested withdrawal amount, it was determined that the system should offer the next possible
             lower amount that can be dispensed in full considering the available denominations. */
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            // Find the next possible lower amount that can be dispensed
            BigDecimal possibleAmount = findNextDispensableAmount(atmStateDto, withdrawalAmount);
            if (possibleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                LoggerUtils.logError(traceId, BANK_SERVICE, "ATM cannot dispense the requested amount {} with available denominations.", withdrawalAmount);
                throw new BankServiceException("ATM cannot dispense the requested amount");
            }
            throw new BankServiceException(
                    "Amount not available, would you like to draw " + possibleAmount.setScale(2, RoundingMode.HALF_UP) + "?"
            );
        }

        return dispensedList;
    }

    /**
     * Finds the next possible lower amount that can be dispensed from the ATM, taking into consideration the available denominations and their respective counts.
     *
     * @param atmStateDto               The current state of the ATM {@link AtmStateDto}, including available denominations and their counts.
     * @param requestedWithdrawalAmount The amount requested for withdrawal.
     * @return BigDecimal amount of the next possible lower amount that can be dispensed from the ATM.
     */
    private BigDecimal findNextDispensableAmount(AtmStateDto atmStateDto, BigDecimal requestedWithdrawalAmount) {
        List<BigDecimal> denominationList = new ArrayList<>(atmStateDto.getDenominationList());
        denominationList.sort(Comparator.reverseOrder());
        Map<BigDecimal, DenominationDto> denominationMap = atmStateDto.getDenominationMap();

        // USe the smallest denomination in the ATM to start the search for the next possible lower amount
        BigDecimal amount = requestedWithdrawalAmount.subtract(denominationList.get(denominationList.size() - 1));
        while (amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remaining = amount;
            boolean possible = true;
            for (BigDecimal denom : denominationList) {
                DenominationDto denomDto = denominationMap.get(denom);
                int available = denomDto.getCount() != null ? denomDto.getCount() : 0;
                int toDispense = remaining.divideToIntegralValue(denom).intValue();
                int notesToDispense = Math.min(toDispense, available);
                remaining = remaining.subtract(denom.multiply(BigDecimal.valueOf(notesToDispense)));
            }
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                return amount;
            }
            amount = amount.subtract(BigDecimal.ONE);
        }
        return BigDecimal.ZERO;
    }
}
