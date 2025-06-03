package za.co.discovery.tao.muzvidziwa.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import za.co.discovery.tao.muzvidziwa.domain.model.cache.CurrencyConversionCache;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.AtmAllocationUpdateDto;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.ConversionRatesDto;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.AccountType;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Atm;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.AtmAllocation;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Client;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientAccount;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientSubType;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientType;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Currency;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CurrencyConversionRate;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Denomination;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.DenominationType;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.AccountDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ClientDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.DenominationDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ResultDto;
import za.co.discovery.tao.muzvidziwa.domain.util.GeneralUtils;
import za.co.discovery.tao.muzvidziwa.repository.AtmAllocationRepository;
import za.co.discovery.tao.muzvidziwa.repository.AtmRepository;
import za.co.discovery.tao.muzvidziwa.repository.ClientAccountRepository;
import za.co.discovery.tao.muzvidziwa.repository.ClientRepository;
import za.co.discovery.tao.muzvidziwa.repository.CreditCardLimitRepository;
import za.co.discovery.tao.muzvidziwa.repository.CreditCardLimitRepositoryTests;
import za.co.discovery.tao.muzvidziwa.repository.CurrencyConversionRepository;
import za.co.discovery.tao.muzvidziwa.repository.CurrencyConversionRepositoryTests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ActiveProfiles("test")
@SpringBootTest
public class BankServiceTests {
    public static final String TRACE_ID = GeneralUtils.generateUniqueId() + "-test";

    @Autowired
    private BankService bankService;
    @Autowired
    private CurrencyConversionCache currencyConversionCache;

    @MockBean
    private AtmAllocationRepository atmAllocationRepository;
    @MockBean
    private AtmRepository atmRepository;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private ClientAccountRepository clientAccountRepository;
    @MockBean
    private CurrencyConversionRepository currencyConversionRepository;
    @MockBean
    private CreditCardLimitRepository creditCardLimitRepository;

    @BeforeEach
    public void setUp() {
        // Initialize the currency conversion cache with predefined conversion rates
        currencyConversionCache.clearCache(TRACE_ID);
        currencyConversionCache.addCurrencyConversionRate("ZAR", createZarCurrencyConversionRate(), TRACE_ID);
        currencyConversionCache.addCurrencyConversionRate("USD", createUsdCurrencyConversionRate(), TRACE_ID);
        currencyConversionCache.addCurrencyConversionRate("TND", createTndCurrencyConversionRate(), TRACE_ID);
        currencyConversionCache.addCurrencyConversionRate("GBP", createGbpCurrencyConversionRate(), TRACE_ID);
        currencyConversionCache.addCurrencyConversionRate("AED", createAedCurrencyConversionRate(), TRACE_ID);
    }

    /**
     * <p>Test 'TRANSACTIONAL CLIENT ACCOUNTS':</p>
     * <p>Given valid client ID and
     * <li>- two active individual ZAR cheque accounts</li>
     * <li>- one active individual ZAR savings account</li>
     * <li>- one active individual ZAR home loan account</li></p>
     * <p>Should return AtmResponse with client details, 2 cheque, and 1 savings accounts and success result status.</p>
     */
    @DisplayName(value = """
            Test 'TRANSACTIONAL CLIENT ACCOUNTS' given valid client ID and active CHQ, CHQ, SAVINGS, HOME LOAN account should return CHQ, CHQ, SAVINGS accounts
            """)
    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andValidClientAccounts_shouldReturnSortedTransactionalAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of(
                createSavingsAccountDto(3L, BigDecimal.valueOf(101500.000)),
                createChequeAccountDto(1L, BigDecimal.valueOf(1250.000)),
                createChequeAccountDto(2L, BigDecimal.valueOf(-2500.000)),
                createHomeLoanAccountDto(5L, BigDecimal.valueOf(-1101500.000))));
        expectedAtmResponse.setResult(createTransactionalResultDto());

        final Client client = createStandardClient();
        final List<ClientAccount> clientAccounts = createStandardZarClientAccounts(client);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andEmptyClientAccounts_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(createStandardClient()));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.empty());

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andNoClientAccounts_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(createStandardClient()));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(List.of()));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andNoClientFound_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(new ClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.empty());

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenInvalidClientId_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(null);
        expectedAtmResponse.setAccounts(null);
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());
        expectedAtmResponse.getResult().setStatusReason("Invalid client identifier (ID) provided");

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, -1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andNullBalanceClientAccount_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(null);

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

                // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNullCurrency_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(null);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNullCurrencyCode_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(new Currency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNullAccountType_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(null);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNullAccountTypeCode_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode(null);
        accountType.setDescription("Cheque Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNullAccountTypeDescription_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription(null);
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNullAccountNumber_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber(null);
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withEmptyAccountNumber_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withInvalidAccountNumber_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("invalid");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withUnknownCurrencyConversionRateInCache_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("1");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);

        final Currency currency = new Currency();
        currency.setCurrencyCode("NSC");
        currency.setDecimalPlaces(2);
        currency.setDescription("Narnia Stone Currency");

        clientAccount.setCurrency(currency);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @Test
    public void getTransactionalClientAccountBalances_givenValidClientId_andClientAccount_withNoCurrencyConversionRateInCache_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("1");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CHQ");
        accountType.setDescription("Cheque account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createZarCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        currencyConversionCache.addCurrencyConversionRate("ZAR", null, TRACE_ID);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findTransactionalClientAccountsByClientId(1)).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getTransactionalClientAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    /**
     * <p>Test 'WITHDRAWAL TRANSACTION':</p>
     * <p>Given valid client ID, account number, amount, and ATM ID, and sufficient funds in the account,
     * Should return AtmResponse with client details, account details, denominations, and success result status.</p>
     */
    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given valid clientId, atmId, amount, and cheque accountNumber should return AtmResponse success result status
            """)
    @Test
    public void postWithdrawalTransaction_givenValidClientId_andValidCHQAccountNumber_andValidAmount_andValidAtm_shouldReturnSuccessWithdrawalResult() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccount(createChequeAccountDto(4L, BigDecimal.valueOf(10000.000)));

        final DenominationDto denomination200Dto = new DenominationDto();
        denomination200Dto.setDenominationId(5L);
        denomination200Dto.setDenominationValue(BigDecimal.valueOf(200.000));
        denomination200Dto.setCount(1);

        final DenominationDto denomination50Dto = new DenominationDto();
        denomination50Dto.setDenominationId(3L);
        denomination50Dto.setDenominationValue(BigDecimal.valueOf(50.000));
        denomination50Dto.setCount(1);

        expectedAtmResponse.setDenomination(List.of(denomination200Dto, denomination50Dto));
        expectedAtmResponse.setResult(createWithdrawalResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = createStandardZarClientAccount(client);

        final List<AtmAllocation> atmAllocationList = createStandardAtmAllocation();

        final AtmAllocationUpdateDto atm10AllocationUpdateDto = new AtmAllocationUpdateDto(1L, 10);
        final AtmAllocationUpdateDto atm50AllocationUpdateDto = new AtmAllocationUpdateDto(3L, 4);
        final AtmAllocationUpdateDto atm100AllocationUpdateDto = new AtmAllocationUpdateDto(4L, 20);
        final AtmAllocationUpdateDto atm200AllocationUpdateDto = new AtmAllocationUpdateDto(5L, 9);

        final List<AtmAllocationUpdateDto> atmAllocationUpdateDtoList = List.of(
                atm10AllocationUpdateDto,
                atm50AllocationUpdateDto,
                atm100AllocationUpdateDto,
                atm200AllocationUpdateDto
        );

        // Mock the repository methods
        Mockito.when(atmRepository.atmExistsByAtmId(3)).thenReturn(true);
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountByClientIdAndAccountNumber(1, "4")).thenReturn(Optional.of(clientAccount));
        Mockito.when(atmAllocationRepository.findAtmAllocationByAtmId(3)).thenReturn(Optional.of(atmAllocationList));
        Mockito.doNothing().when(atmAllocationRepository).updateDenominationCounts(3, atmAllocationUpdateDtoList);
        Mockito.doNothing().when(clientAccountRepository).updateClientAccountByAccountNumber(1, "4", BigDecimal.valueOf(10250.000));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, 1, 3, "4", BigDecimal.valueOf(250.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    /**
     * <p>Test 'WITHDRAWAL TRANSACTION':</p>
     * <p>Given valid client ID, account number, amount, and ATM ID, and insufficient funds in the account,
     * Should return {@link} AtmResponse with client details, account details, denominations, and insufficient fund result status.</p>
     */
    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given valid clientId, atmId, over balance amount, and cheque accountNumber should return AtmResponse insufficient funds status
            """)
    @Test
    public void postWithdrawalTransaction_givenValidData_butWithdrawalAmountGreaterThanAccBalance_shouldReturnInsufficientFundsResult() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccount(createChequeAccountDto(4L, BigDecimal.valueOf(10250.000)));

        expectedAtmResponse.setResult(createWithdrawalErrorResultDto("Insufficient funds"));

        final Client client = createStandardClient();
        final ClientAccount clientAccount = createStandardZarClientAccount(client);

        // Mock the repository methods
        Mockito.when(atmRepository.atmExistsByAtmId(3)).thenReturn(true);
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountByClientIdAndAccountNumber(1, "4")).thenReturn(Optional.of(clientAccount));
//        Mockito.when(atmAllocationRepository.findAtmAllocationByAtmId(3)).thenReturn(Optional.of(atmAllocationList));
//        Mockito.doNothing().when(atmAllocationRepository).updateDenominationCounts(3, atmAllocationUpdateDtoList);
//        Mockito.doNothing().when(clientAccountRepository).updateClientAccountByAccountNumber(1, "4", BigDecimal.valueOf(10250.000));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, 1, 3, "4", BigDecimal.valueOf(27000.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given valid clientId, atmId, amount, and credit card accountNumber should return AtmResponse success result status
            """)
    @Test
    public void postWithdrawalTransaction_givenValidClientId_andValidCCRDAccountNumber_andValidAmount_andValidAtm_shouldReturnSuccessWithdrawalResult() throws Exception {
        // Prepare the expected AtmResponse object
        final BigDecimal ccrdLimitBalance = BigDecimal.valueOf(25000);
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccount(createCCRDAccountDto(4L, BigDecimal.valueOf(10000.000), ccrdLimitBalance));

        final DenominationDto denomination200Dto = new DenominationDto();
        denomination200Dto.setDenominationId(5L);
        denomination200Dto.setDenominationValue(BigDecimal.valueOf(200.000));
        denomination200Dto.setCount(1);

        final DenominationDto denomination50Dto = new DenominationDto();
        denomination50Dto.setDenominationId(3L);
        denomination50Dto.setDenominationValue(BigDecimal.valueOf(50.000));
        denomination50Dto.setCount(1);

        expectedAtmResponse.setDenomination(List.of(denomination200Dto, denomination50Dto));
        expectedAtmResponse.setResult(createWithdrawalResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = createStandardZarCCRDClientAccount(client);

        final List<AtmAllocation> atmAllocationList = createStandardAtmAllocation();

        final AtmAllocationUpdateDto atm10AllocationUpdateDto = new AtmAllocationUpdateDto(1L, 10);
        final AtmAllocationUpdateDto atm50AllocationUpdateDto = new AtmAllocationUpdateDto(3L, 4);
        final AtmAllocationUpdateDto atm100AllocationUpdateDto = new AtmAllocationUpdateDto(4L, 20);
        final AtmAllocationUpdateDto atm200AllocationUpdateDto = new AtmAllocationUpdateDto(5L, 9);

        final List<AtmAllocationUpdateDto> atmAllocationUpdateDtoList = List.of(
                atm10AllocationUpdateDto,
                atm50AllocationUpdateDto,
                atm100AllocationUpdateDto,
                atm200AllocationUpdateDto
        );

        // Mock the repository methods
        Mockito.when(atmRepository.atmExistsByAtmId(3)).thenReturn(true);
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountByClientIdAndAccountNumber(1, "4")).thenReturn(Optional.of(clientAccount));
        Mockito.when(atmAllocationRepository.findAtmAllocationByAtmId(3)).thenReturn(Optional.of(atmAllocationList));
        Mockito.when(creditCardLimitRepository.findCreditCardLimitByClientAccountNumber(clientAccount.getClientAccountNumber())).thenReturn(Optional.of(ccrdLimitBalance.setScale(3, RoundingMode.HALF_UP)));
        Mockito.doNothing().when(atmAllocationRepository).updateDenominationCounts(3, atmAllocationUpdateDtoList);
        Mockito.doNothing().when(clientAccountRepository).updateClientAccountByAccountNumber(1, "4", BigDecimal.valueOf(10250.000));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, 1, 3, "4", BigDecimal.valueOf(250.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given valid clientId, atmId, amount, and credit card accountNumber should return AtmResponse success result status
            """)
    @Test
    public void postWithdrawalTransaction_givenValidClientId_andValidSVGSAccountNumber_andValidAmount_andValidAtm_shouldReturnSuccessWithdrawalResult() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccount(createSavingsAccountDto(4L, BigDecimal.valueOf(10000.000)));

        final DenominationDto denomination200Dto = new DenominationDto();
        denomination200Dto.setDenominationId(5L);
        denomination200Dto.setDenominationValue(BigDecimal.valueOf(200.000));
        denomination200Dto.setCount(1);

        final DenominationDto denomination50Dto = new DenominationDto();
        denomination50Dto.setDenominationId(3L);
        denomination50Dto.setDenominationValue(BigDecimal.valueOf(50.000));
        denomination50Dto.setCount(1);

        expectedAtmResponse.setDenomination(List.of(denomination200Dto, denomination50Dto));
        expectedAtmResponse.setResult(createWithdrawalResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = createStandardZarSavingsClientAccount(client);

        final List<AtmAllocation> atmAllocationList = createStandardAtmAllocation();

        final AtmAllocationUpdateDto atm10AllocationUpdateDto = new AtmAllocationUpdateDto(1L, 10);
        final AtmAllocationUpdateDto atm50AllocationUpdateDto = new AtmAllocationUpdateDto(3L, 4);
        final AtmAllocationUpdateDto atm100AllocationUpdateDto = new AtmAllocationUpdateDto(4L, 20);
        final AtmAllocationUpdateDto atm200AllocationUpdateDto = new AtmAllocationUpdateDto(5L, 9);

        final List<AtmAllocationUpdateDto> atmAllocationUpdateDtoList = List.of(
                atm10AllocationUpdateDto,
                atm50AllocationUpdateDto,
                atm100AllocationUpdateDto,
                atm200AllocationUpdateDto
        );

        // Mock the repository methods
        Mockito.when(atmRepository.atmExistsByAtmId(3)).thenReturn(true);
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountByClientIdAndAccountNumber(1, "4")).thenReturn(Optional.of(clientAccount));
        Mockito.when(atmAllocationRepository.findAtmAllocationByAtmId(3)).thenReturn(Optional.of(atmAllocationList));
        Mockito.doNothing().when(atmAllocationRepository).updateDenominationCounts(3, atmAllocationUpdateDtoList);
        Mockito.doNothing().when(clientAccountRepository).updateClientAccountByAccountNumber(1, "4", BigDecimal.valueOf(10250.000));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, 1, 3, "4", BigDecimal.valueOf(250.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    /**
     * <p>Test 'WITHDRAWAL TRANSACTION':</p>
     * <p>Given valid client ID, account number, amount, and ATM ID, but insufficient funds in the ATM,
     * Should return AtmResponse with client details, account details, offer a lower withdrawal amount and error result status.</p>
     */
    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given valid clientId, atmId, amount, and accountNumber with insufficient funds in 
            the ATM should return AtmResponse error result status
            """)
    @Test
    public void postWithdrawalTransaction_givenValidInput_withUnderFundedAtm_shouldReturnUnfundedResult() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccount(createChequeAccountDto(4L, BigDecimal.valueOf(10250.000)));

        expectedAtmResponse.setDenomination(List.of());
        expectedAtmResponse.setResult(createWithdrawalErrorResultDto("Amount not available, would you like to draw 100.00?"));

        final Client client = createStandardClient();
        final ClientAccount clientAccount = createStandardZarClientAccount(client);

        final List<AtmAllocation> atmAllocationList
                = List.of(createAtmAllocationWithDenomination(3, createStandardAtm(), create10NoteDenomination(), 10));

        // Mock the repository methods
        Mockito.when(atmRepository.atmExistsByAtmId(3)).thenReturn(true);
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountByClientIdAndAccountNumber(1, "4")).thenReturn(Optional.of(clientAccount));
        Mockito.when(atmAllocationRepository.findAtmAllocationByAtmId(3)).thenReturn(Optional.of(atmAllocationList));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, 1, 3, "4", BigDecimal.valueOf(250.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    /**
     * <p>Test 'WITHDRAWAL TRANSACTION':</p>
     * <p>Given valid client ID, account number, amount, and ATM ID, but incompatible denominations in the ATM,
     * i.e., The ATM_ALLOCATION shows that the ATM only has 200.000 notes but the client is requesting for a withdrawal of 250.000
     * Should return AtmResponse with client details, account details, no denominations, and error result status.</p>
     */
    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given valid clientId, atmId, amount, and accountNumber with incompatible denominations in 
            the ATM should return AtmResponse error result status
            """)
    @Test
    public void postWithdrawalTransaction_givenValidInput_withIncompatibleDenominationsInAtm_shouldReturnUnfundedResult() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccount(createChequeAccountDto(4L, BigDecimal.valueOf(10250.00)));

        expectedAtmResponse.setDenomination(List.of());
        expectedAtmResponse.setResult(createWithdrawalErrorResultDto("ATM can only dispense cash in multiples of 200.00"));

        final Client client = createStandardClient();
        final ClientAccount clientAccount = createStandardZarClientAccount(client);

        final List<AtmAllocation> atmAllocationList
                = List.of(createAtmAllocationWithDenomination(3, createStandardAtm(), create200NoteDenomination(), 10));

        // Mock the repository methods
        Mockito.when(atmRepository.atmExistsByAtmId(3)).thenReturn(true);
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountByClientIdAndAccountNumber(1, "4")).thenReturn(Optional.of(clientAccount));
        Mockito.when(atmAllocationRepository.findAtmAllocationByAtmId(3)).thenReturn(Optional.of(atmAllocationList));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, 1, 3, "4", BigDecimal.valueOf(250.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            Test 'WITHDRAWAL TRANSACTION': given invalid clientId should return AtmResponse invalid client identifier status
            """)
    @Test
    public void postWithdrawalTransaction_givenInvalidClientId_shouldReturnInvalidClientIdResult() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(new ClientDto());
        expectedAtmResponse.setAccount(new AccountDto());

        expectedAtmResponse.setResult(createWithdrawalErrorResultDto("Invalid client identifier (ID) provided"));

        final AtmResponse actualAtmResponse = bankService.postWithdrawal(TRACE_ID, -1, 3, "4", BigDecimal.valueOf(27000.000));

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and has active USD, TND, AED, and GBP accounts should return TND, AED, USD, GBP accounts
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andValidClientAccounts_shouldReturnSortedTransactionalAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of(
                createTndCFCAAccountDto(2L),
                createAedCFCAAccountDto(4L),
                createUsdCFCAAccountDto(1L),
                createGbpCFCAAccountDto(3L)
        ));
        expectedAtmResponse.setResult(createForexResultDto());

        final Client client = createStandardClient();
        final List<ClientAccount> clientAccounts = createStandardCFCAAccounts(client);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and DB returns an empty list of accounts should return no account found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andEmptyClientAccounts_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(createStandardClient()));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.empty());

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and DB returns an empty optional list of accounts should return no account found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andNoClientAccounts_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(createStandardClient()));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(List.of()));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId but no DB client record should return no account found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andNoClientFound_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(new ClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.empty());

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given invalid clientId should return invalid client identifier
            """)
    @Test
    public void getForexAccountBalances_givenInvalidClientId_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(null);
        expectedAtmResponse.setAccounts(null);
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());
        expectedAtmResponse.getResult().setStatusReason("Invalid client identifier (ID) provided");

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, -1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and null balanceClientAccount should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andNullBalanceClientAccount_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(null);

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with null currency should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNullCurrency_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(null);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with null currencyCode should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNullCurrencyCode_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(new Currency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with null accountType should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNullAccountType_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(null);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with null accountTypeCode should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNullAccountTypeCode_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode(null);
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with null accountTypeDescription should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNullAccountTypeDescription_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription(null);
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with null accountNumber should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNullAccountNumber_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber(null);
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with empty accountNumber should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withEmptyAccountNumber_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with invalid accountNumber should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withInvalidAccountNumber_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("invalid");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with unknown currency conversion rate in cache should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withUnknownCurrencyConversionRateInCache_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("1");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);

        final Currency currency = new Currency();
        currency.setCurrencyCode("NSC");
        currency.setDecimalPlaces(2);
        currency.setDescription("Narnia Stone Currency");

        clientAccount.setCurrency(currency);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    @DisplayName("""
            test 'CUSTOMER FOREIGN CURRENCY ACCOUNT': given valid clientId and clientAccount with no currency conversion rate in cache should return no accounts found
            """)
    @Test
    public void getForexAccountBalances_givenValidClientId_andClientAccount_withNoCurrencyConversionRateInCache_shouldReturnNoAccountsFoundAtmResponse() throws Exception {
        // Prepare the expected AtmResponse object
        final AtmResponse expectedAtmResponse = new AtmResponse();
        expectedAtmResponse.setClient(createClientDto());
        expectedAtmResponse.setAccounts(List.of());
        expectedAtmResponse.setResult(createNoAccountsToDisplayResultDto());

        final Client client = createStandardClient();
        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("1");
        clientAccount.setClient(client);

        final AccountType accountType = new AccountType();
        accountType.setAccountTypeCode("CFCA");
        accountType.setDescription("Customer Foreign Currency Account");
        accountType.setTransactional(true);

        clientAccount.setAccountType(accountType);
        clientAccount.setCurrency(createUsdCurrency());
        clientAccount.setDisplayBalance(BigDecimal.valueOf(200.000));

        final List<ClientAccount> clientAccounts = List.of(clientAccount);

        currencyConversionCache.addCurrencyConversionRate("USD", null, TRACE_ID);

        // Mock the repository methods
        Mockito.when(clientRepository.findByClientId(1)).thenReturn(Optional.of(client));
        Mockito.when(clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CFCA")).thenReturn(Optional.of(clientAccounts));

        // Perform SUT
        final AtmResponse actualAtmResponse = bankService.getForexAccountBalances(TRACE_ID, 1);

        // Verify results
        Assertions.assertEquals(expectedAtmResponse, actualAtmResponse);
    }

    private ClientDto createClientDto() {
        final ClientDto clientDto = new ClientDto();
        clientDto.setId(1L);
        clientDto.setTitle("Mr");
        clientDto.setName("Tao");
        clientDto.setSurname("Muzvidziwa");
        return clientDto;
    }

    private AccountDto createChequeAccountDto(final Long accountNumber, final BigDecimal balance) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("CHQ");
        accountDto.setAccountTypeDescription("Cheque Account");
        accountDto.setCurrencyCode("ZAR");
        accountDto.setConversionRate(BigDecimal.valueOf(1.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setBalance(balance.setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getBalance().add(BigDecimal.valueOf(10000.000).setScale(3, RoundingMode.HALF_UP)));
        return accountDto;
    }

    private AccountDto createCCRDAccountDto(final Long accountNumber, final BigDecimal balance, final BigDecimal ccrdLimitBalance) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("CCRD");
        accountDto.setAccountTypeDescription("Credit Card Account");
        accountDto.setCurrencyCode("ZAR");
        accountDto.setConversionRate(BigDecimal.valueOf(1.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setBalance(balance.setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getBalance().subtract(ccrdLimitBalance).setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(ccrdLimitBalance.setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private AccountDto createSVGSAccountDto(final Long accountNumber, final BigDecimal balance) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("SVGS");
        accountDto.setAccountTypeDescription("Savings Account");
        accountDto.setCurrencyCode("ZAR");
        accountDto.setConversionRate(BigDecimal.valueOf(1.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setBalance(balance.setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getBalance().add(BigDecimal.valueOf(10000.000).setScale(3, RoundingMode.HALF_UP)));
        return accountDto;
    }

    private AccountDto createSavingsAccountDto(final Long accountNumber, final BigDecimal balance) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("SVGS");
        accountDto.setAccountTypeDescription("Savings Account");
        accountDto.setCurrencyCode("ZAR");
        accountDto.setConversionRate(BigDecimal.valueOf(1.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setBalance(balance.setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private AccountDto createUsdCFCAAccountDto(final Long accountNumber) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("CFCA");
        accountDto.setAccountTypeDescription("Customer Foreign Currency Account");
        accountDto.setCurrencyCode("USD");
        accountDto.setConversionRate(BigDecimal.valueOf(18.6167).setScale(3, RoundingMode.HALF_UP));
        accountDto.setCcyBalance(BigDecimal.valueOf(1500.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getCcyBalance().multiply(BigDecimal.valueOf(18.6167)).setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getCcyBalance().setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private AccountDto createTndCFCAAccountDto(final Long accountNumber) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("CFCA");
        accountDto.setAccountTypeDescription("Customer Foreign Currency Account");
        accountDto.setCurrencyCode("TND");
        accountDto.setConversionRate(BigDecimal.valueOf(0.1666).setScale(3, RoundingMode.HALF_UP));
        accountDto.setCcyBalance(BigDecimal.valueOf(500.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getCcyBalance().multiply(BigDecimal.valueOf(0.1666)).setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getCcyBalance().setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private AccountDto createGbpCFCAAccountDto(final Long accountNumber) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("CFCA");
        accountDto.setAccountTypeDescription("Customer Foreign Currency Account");
        accountDto.setCurrencyCode("GBP");
        accountDto.setConversionRate(BigDecimal.valueOf(23.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setCcyBalance(BigDecimal.valueOf(2025.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getCcyBalance().multiply(BigDecimal.valueOf(23.000)).setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getCcyBalance().setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private AccountDto createAedCFCAAccountDto(final Long accountNumber) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("CFCA");
        accountDto.setAccountTypeDescription("Customer Foreign Currency Account");
        accountDto.setCurrencyCode("AED");
        accountDto.setConversionRate(BigDecimal.valueOf(0.3196).setScale(3, RoundingMode.HALF_UP));
        accountDto.setCcyBalance(BigDecimal.valueOf(1000.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getCcyBalance().divide(BigDecimal.valueOf(0.3196), 3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getCcyBalance().setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private AccountDto createHomeLoanAccountDto(final Long accountNumber, final BigDecimal balance) {
        final AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(accountNumber);
        accountDto.setTypeCode("HLOAN");
        accountDto.setAccountTypeDescription("Home Loan Account");
        accountDto.setCurrencyCode("ZAR");
        accountDto.setConversionRate(BigDecimal.valueOf(1.000).setScale(3, RoundingMode.HALF_UP));
        accountDto.setBalance(balance.setScale(3, RoundingMode.HALF_UP));
        accountDto.setZarBalance(accountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
        accountDto.setAccountLimit(accountDto.getBalance().setScale(3, RoundingMode.HALF_UP));
        return accountDto;
    }

    private ResultDto createTransactionalResultDto() {
        final ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(true);
        resultDto.setStatusCode(200);
        resultDto.setStatusReason("Displaying transactional accounts");
        return resultDto;
    }

    private ResultDto createForexResultDto() {
        final ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(true);
        resultDto.setStatusCode(200);
        resultDto.setStatusReason("Displaying foreign currency accounts");
        return resultDto;
    }

    private ResultDto createWithdrawalResultDto() {
        final ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(true);
        resultDto.setStatusCode(200);
        resultDto.setStatusReason("Withdrawal successful");
        return resultDto;
    }

    private ResultDto createWithdrawalErrorResultDto(final String message) {
        final ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(false);
        resultDto.setStatusCode(400);
        resultDto.setStatusReason(message);
        return resultDto;
    }

    private ResultDto createNoAccountsToDisplayResultDto() {
        final ResultDto resultDto = new ResultDto();
        resultDto.setSuccess(false);
        resultDto.setStatusCode(400);
        resultDto.setStatusReason("No accounts to display");
        return resultDto;
    }

    private Map<String, ConversionRatesDto> createConversionRatesDtoMap() {
        final ConversionRatesDto conversionRatesDtoZar = new ConversionRatesDto();
        conversionRatesDtoZar.setConversionIndicator("/");
        conversionRatesDtoZar.setConversionRate("1.000");

        final ConversionRatesDto conversionRatesDtoUsd = new ConversionRatesDto();
        conversionRatesDtoUsd.setConversionIndicator("*");
        conversionRatesDtoUsd.setConversionRate("18.6167");

        final ConversionRatesDto conversionRatesDtoTnd = new ConversionRatesDto();
        conversionRatesDtoTnd.setConversionIndicator("*");
        conversionRatesDtoTnd.setConversionRate("0.1666");

        return Map.of(
                "ZAR", conversionRatesDtoZar,
                "USD", conversionRatesDtoUsd,
                "TND", conversionRatesDtoTnd
        );
    }

    private ConversionRatesDto createZarConversionRateDto() {
        final ConversionRatesDto conversionRatesDto = new ConversionRatesDto();
        conversionRatesDto.setConversionIndicator("/");
        conversionRatesDto.setConversionRate("1.000");
        return conversionRatesDto;
    }

    private ConversionRatesDto createUsdConversionRateDto() {
        final ConversionRatesDto conversionRatesDto = new ConversionRatesDto();
        conversionRatesDto.setConversionIndicator("*");
        conversionRatesDto.setConversionRate("18.6167");
        return conversionRatesDto;
    }

    private ConversionRatesDto createTndConversionRateDto() {
        final ConversionRatesDto conversionRatesDto = new ConversionRatesDto();
        conversionRatesDto.setConversionIndicator("*");
        conversionRatesDto.setConversionRate("0.1666");
        return conversionRatesDto;
    }

    private CurrencyConversionRate createZarCurrencyConversionRate() {
        final CurrencyConversionRate conversionRate = new CurrencyConversionRate();
        conversionRate.setCurrencyCode("ZAR");
        conversionRate.setConversionIndicator("/");
        conversionRate.setRate(BigDecimal.valueOf(1.000));
        return conversionRate;
    }

    private CurrencyConversionRate createUsdCurrencyConversionRate() {
        final CurrencyConversionRate conversionRate = new CurrencyConversionRate();
        conversionRate.setCurrencyCode("USD");
        conversionRate.setConversionIndicator("*");
        conversionRate.setRate(BigDecimal.valueOf(18.6167));
        return conversionRate;
    }

    private CurrencyConversionRate createTndCurrencyConversionRate() {
        final CurrencyConversionRate conversionRate = new CurrencyConversionRate();
        conversionRate.setCurrencyCode("TND");
        conversionRate.setConversionIndicator("*");
        conversionRate.setRate(BigDecimal.valueOf(0.1666));
        return conversionRate;
    }

    private CurrencyConversionRate createGbpCurrencyConversionRate() {
        final CurrencyConversionRate conversionRate = new CurrencyConversionRate();
        conversionRate.setCurrencyCode("TND");
        conversionRate.setConversionIndicator("*");
        conversionRate.setRate(BigDecimal.valueOf(23.000));
        return conversionRate;
    }

    private CurrencyConversionRate createAedCurrencyConversionRate() {
        final CurrencyConversionRate conversionRate = new CurrencyConversionRate();
        conversionRate.setCurrencyCode("AED");
        conversionRate.setConversionIndicator("/");
        conversionRate.setRate(BigDecimal.valueOf(0.3196));
        return conversionRate;
    }

    private Client createStandardClient() {
        final Client client = new Client();
        client.setClientId(1);
        client.setTitle("Mr");
        client.setName("Tao");
        client.setSurname("Muzvidziwa");
        client.setDob(java.sql.Date.valueOf("1990-01-01"));

        final ClientType clientType = new ClientType();
        clientType.setClientTypeCode("I");
        clientType.setDescription("Individual");

        final ClientSubType clientSubType = new ClientSubType();
        clientSubType.setClientSubTypeCode("MAL");
        clientSubType.setClientType(clientType);
        clientSubType.setDescription("Male");

        return client;
    }

    private List<ClientAccount> createStandardZarClientAccounts(final Client client) {
        final AccountType chequeAccountType = new AccountType();
        chequeAccountType.setAccountTypeCode("CHQ");
        chequeAccountType.setDescription("Cheque Account");
        chequeAccountType.setTransactional(true);

        final AccountType savingsAccountType = new AccountType();
        savingsAccountType.setAccountTypeCode("SVGS");
        savingsAccountType.setDescription("Savings Account");
        savingsAccountType.setTransactional(true);

        final AccountType homeLoanAccountType = new AccountType();
        homeLoanAccountType.setAccountTypeCode("HLOAN");
        homeLoanAccountType.setDescription("Home Loan Account");
        homeLoanAccountType.setTransactional(true);

        final Currency currency = new Currency();
        currency.setCurrencyCode("ZAR");
        currency.setDecimalPlaces(2);
        currency.setDescription("South African rand");

        final ClientAccount clientAccount1 = new ClientAccount();
        clientAccount1.setClientAccountNumber("1");
        clientAccount1.setClient(client);
        clientAccount1.setAccountType(chequeAccountType);
        clientAccount1.setCurrency(currency);
        clientAccount1.setDisplayBalance(BigDecimal.valueOf(1250));

        final ClientAccount clientAccount2 = new ClientAccount();
        clientAccount2.setClientAccountNumber("2");
        clientAccount2.setClient(client);
        clientAccount2.setAccountType(chequeAccountType);
        clientAccount2.setCurrency(currency);
        clientAccount2.setDisplayBalance(BigDecimal.valueOf(-2500));

        final ClientAccount clientAccount3 = new ClientAccount();
        clientAccount3.setClientAccountNumber("3");
        clientAccount3.setClient(client);
        clientAccount3.setAccountType(savingsAccountType);
        clientAccount3.setCurrency(currency);
        clientAccount3.setDisplayBalance(BigDecimal.valueOf(101500));

        final ClientAccount clientAccount4 = new ClientAccount();
        clientAccount4.setClientAccountNumber("5");
        clientAccount4.setClient(client);
        clientAccount4.setAccountType(homeLoanAccountType);
        clientAccount4.setCurrency(currency);
        clientAccount4.setDisplayBalance(BigDecimal.valueOf(-1101500));

        return List.of(clientAccount1, clientAccount2, clientAccount3, clientAccount4);
    }

    private ClientAccount createStandardZarClientAccount(final Client client) {
        final AccountType chequeAccountType = new AccountType();
        chequeAccountType.setAccountTypeCode("CHQ");
        chequeAccountType.setDescription("Cheque Account");
        chequeAccountType.setTransactional(true);

        final Currency currency = new Currency();
        currency.setCurrencyCode("ZAR");
        currency.setDecimalPlaces(2);
        currency.setDescription("South African rand");

        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);
        clientAccount.setAccountType(chequeAccountType);
        clientAccount.setCurrency(currency);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(10250));

        return clientAccount;
    }

    private ClientAccount createStandardZarCCRDClientAccount(final Client client) {
        final AccountType chequeAccountType = new AccountType();
        chequeAccountType.setAccountTypeCode("CCRD");
        chequeAccountType.setDescription("Credit Card Account");
        chequeAccountType.setTransactional(true);

        final Currency currency = new Currency();
        currency.setCurrencyCode("ZAR");
        currency.setDecimalPlaces(2);
        currency.setDescription("South African rand");

        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);
        clientAccount.setAccountType(chequeAccountType);
        clientAccount.setCurrency(currency);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(10250));

        return clientAccount;
    }

    private ClientAccount createStandardZarSavingsClientAccount(final Client client) {
        final AccountType chequeAccountType = new AccountType();
        chequeAccountType.setAccountTypeCode("SVGS");
        chequeAccountType.setDescription("Savings Account");
        chequeAccountType.setTransactional(true);

        final Currency currency = new Currency();
        currency.setCurrencyCode("ZAR");
        currency.setDecimalPlaces(2);
        currency.setDescription("South African rand");

        final ClientAccount clientAccount = new ClientAccount();
        clientAccount.setClientAccountNumber("4");
        clientAccount.setClient(client);
        clientAccount.setAccountType(chequeAccountType);
        clientAccount.setCurrency(currency);
        clientAccount.setDisplayBalance(BigDecimal.valueOf(10250));

        return clientAccount;
    }

    private List<ClientAccount> createStandardCFCAAccounts(final Client client) {
        final AccountType cFCAAccountType = new AccountType();
        cFCAAccountType.setAccountTypeCode("CFCA");
        cFCAAccountType.setDescription("Customer Foreign Currency Account");
        cFCAAccountType.setTransactional(false);

        final ClientAccount usdAccount = new ClientAccount();
        usdAccount.setClientAccountNumber("1");
        usdAccount.setClient(client);
        usdAccount.setAccountType(cFCAAccountType);
        usdAccount.setCurrency(createUsdCurrency());
        usdAccount.setDisplayBalance(BigDecimal.valueOf(1500.000));

        final ClientAccount tndAccount = new ClientAccount();
        tndAccount.setClientAccountNumber("2");
        tndAccount.setClient(client);
        tndAccount.setAccountType(cFCAAccountType);
        tndAccount.setCurrency(createTndCurrency());
        tndAccount.setDisplayBalance(BigDecimal.valueOf(500.000));

        final ClientAccount gbpAccount = new ClientAccount();
        gbpAccount.setClientAccountNumber("3");
        gbpAccount.setClient(client);
        gbpAccount.setAccountType(cFCAAccountType);
        gbpAccount.setCurrency(createGbpCurrency());
        gbpAccount.setDisplayBalance(BigDecimal.valueOf(2025.000));

        final ClientAccount aedAccount = new ClientAccount();
        aedAccount.setClientAccountNumber("4");
        aedAccount.setClient(client);
        aedAccount.setAccountType(cFCAAccountType);
        aedAccount.setCurrency(createAedCurrency());
        aedAccount.setDisplayBalance(BigDecimal.valueOf(1000.000));

        return List.of(usdAccount, tndAccount, gbpAccount, aedAccount);
    }

    private Currency createUsdCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("USD");
        currency.setDecimalPlaces(2);
        currency.setDescription("United States dollar");
        return currency;
    }

    private Currency createTndCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("TND");
        currency.setDecimalPlaces(3);
        currency.setDescription("Tunisian dinar");
        return currency;
    }

    private Currency createGbpCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("GBP");
        currency.setDecimalPlaces(2);
        currency.setDescription("British pound sterling");
        return currency;
    }

    private Currency createAedCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("AED");
        currency.setDecimalPlaces(2);
        currency.setDescription("United Arab Emirates dirham");
        return currency;
    }

    private Currency createNscCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("NSC");
        currency.setDecimalPlaces(2);
        currency.setDescription("Narnia Stone Currency");
        return currency;
    }

    private Currency createZarCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("ZAR");
        currency.setDecimalPlaces(2);
        currency.setDescription("South African rand");
        return currency;
    }

    private List<AtmAllocation> createStandardAtmAllocation() {
        final Atm atm = createStandardAtm();

        int atmAllocationId = 1;

        return List.of(
                createAtmAllocationWithDenomination(atmAllocationId++, atm, create10NoteDenomination(), 10),
                createAtmAllocationWithDenomination(atmAllocationId++, atm, create50NoteDenomination(), 5),
                createAtmAllocationWithDenomination(atmAllocationId++, atm, create100NoteDenomination(), 20),
                createAtmAllocationWithDenomination(atmAllocationId, atm, create200NoteDenomination(), 10)
        );
    }

    private Atm createStandardAtm() {
        final Atm atm = new Atm();
        atm.setAtmId(3);
        atm.setName("ATM 3");
        atm.setLocation("Test Location");
        return atm;
    }

    private AtmAllocation createAtmAllocationWithDenomination(final int atmAllocationId, final Atm atm, final Denomination denomination, final int count) {
        final AtmAllocation atmAllocation = new AtmAllocation();
        atmAllocation.setAtmAllocationId(atmAllocationId);
        atmAllocation.setAtm(atm);
        atmAllocation.setDenomination(denomination);
        atmAllocation.setCount(count);
        return atmAllocation;
    }

    private Denomination create10NoteDenomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationId(1);
        denomination.setDenominationValue(BigDecimal.valueOf(10.00));

        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("N");
        denominationType.setDescription("Bank Note");

        denomination.setDenominationType(denominationType);

        return denomination;
    }

    private Denomination create20NoteDenomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationId(2);
        denomination.setDenominationValue(BigDecimal.valueOf(20.00));

        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("N");
        denominationType.setDescription("Bank Note");

        denomination.setDenominationType(denominationType);

        return denomination;
    }

    private Denomination create50NoteDenomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationId(3);
        denomination.setDenominationValue(BigDecimal.valueOf(50.00));

        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("N");
        denominationType.setDescription("Bank Note");

        denomination.setDenominationType(denominationType);

        return denomination;
    }

    private Denomination create100NoteDenomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationId(4);
        denomination.setDenominationValue(BigDecimal.valueOf(100.00));

        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("N");
        denominationType.setDescription("Bank Note");

        denomination.setDenominationType(denominationType);

        return denomination;
    }

    private Denomination create200NoteDenomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationId(5);
        denomination.setDenominationValue(BigDecimal.valueOf(200.00));

        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("N");
        denominationType.setDescription("Bank Note");

        denomination.setDenominationType(denominationType);

        return denomination;
    }

    private Denomination create5CoinDenomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationId(10);
        denomination.setDenominationValue(BigDecimal.valueOf(5.00));

        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("C");
        denominationType.setDescription("Bank Coin");

        denomination.setDenominationType(denominationType);

        return denomination;
    }

}
