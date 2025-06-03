package za.co.discovery.tao.muzvidziwa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.AccountType;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Client;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientAccount;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Currency;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ClientAccountRepositoryTests {

    @Autowired
    private ClientAccountRepository clientAccountRepository;

    @Autowired
    private ClientRepository clientRepository;


    private Client client;
    private AccountType chqType;
    private Currency zarCurrency;

    @Test
    @DisplayName("givenClientId_whenFindTransactionalClientAccountsByClientId_shouldReturnTransactionalAccounts")
    void givenClientId_whenFindTransactionalClientAccountsByClientId_shouldReturnTransactionalAccounts() {
        Optional<List<ClientAccount>> accounts = clientAccountRepository.findTransactionalClientAccountsByClientId(1);
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.get().get(1).getAccountType().getAccountTypeCode()).isEqualTo("CHQ");
    }

    @Test
    @DisplayName("givenClientIdAndAccountType_whenFindClientAccountsByClientIdAndAccountType_shouldReturnMatchingAccounts")
    void givenClientIdAndAccountType_whenFindClientAccountsByClientIdAndAccountType_shouldReturnMatchingAccounts() {
        Optional<List<ClientAccount>> accounts = clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "CHQ");
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.get().get(0).getAccountType().getAccountTypeCode()).isEqualTo("CHQ");
    }

    @Test
    @DisplayName("givenInvalidClientId_whenFindTransactionalClientAccountsByClientId_shouldReturnEmptyList")
    void givenInvalidClientId_whenFindTransactionalClientAccountsByClientId_shouldReturnEmptyList() {
        List<ClientAccount> accounts = clientAccountRepository.findTransactionalClientAccountsByClientId(-1L).orElse(List.of());
        assertThat(accounts).isEmpty();
    }

    @Test
    @DisplayName("givenInvalidAccountType_whenFindClientAccountsByClientIdAndAccountType_shouldReturnEmptyList")
    void givenInvalidAccountType_whenFindClientAccountsByClientIdAndAccountType_shouldReturnEmptyList() {
        List<ClientAccount> accounts = clientAccountRepository.findClientAccountsByClientIdAndAccountType(1, "XYZ").orElse(List.of());
        assertThat(accounts).isEmpty();
    }

    @Test
    @DisplayName("givenInvalidClientIdAndAccountNumber_whenFindClientAccountByClientIdAndAccountNumber_shouldReturnEmptyOptional")
    void givenInvalidClientIdAndAccountNumber_whenFindClientAccountByClientIdAndAccountNumber_shouldReturnEmptyOptional() {
        Optional<ClientAccount> account = clientAccountRepository.findClientAccountByClientIdAndAccountNumber(-1L, "0000000000");
        assertThat(account).isNotPresent();
    }
}