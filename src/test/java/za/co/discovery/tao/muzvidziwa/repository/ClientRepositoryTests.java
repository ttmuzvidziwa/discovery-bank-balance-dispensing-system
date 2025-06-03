package za.co.discovery.tao.muzvidziwa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Client;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientSubType;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientType;

import java.sql.Date;
import java.util.Optional;

@ActiveProfiles("test")
@DataJpaTest
public class ClientRepositoryTests {
    @Autowired
    private ClientRepository clientRepository;

    @DisplayName("""
            test 'Find By Client Id' where client profile in DB should return CLIENT
            """)
    @Test
    public void findByClientId_givenClientInDB_shouldReturnClient() throws Exception {
        // Prepare expectations
        final Client expectedClient = createClient();

        // Perform SUT
        Optional<Client> actualClient = clientRepository.findByClientId(1);

        // Verify results
        Assertions.assertTrue(actualClient.isPresent());
        Assertions.assertEquals(expectedClient, actualClient.get());
    }

    @DisplayName("""
            test 'Find By Client Id' where no client profile with id is in DB should return empty optional
            """)
    @Test
    public void findByClientId_givenNoClientInDB_shouldReturnEmpty() throws Exception {
        // Perform SUT
        Optional<Client> actualClient = clientRepository.findByClientId(2);

        // Verify results
        Assertions.assertFalse(actualClient.isPresent());
    }

    private Client createClient() {
        final Client client = new Client();
        client.setClientId(1);
        client.setTitle("Mr");
        client.setName("Tao");
        client.setSurname("Muzvidziwa");
        client.setDob(Date.valueOf("1980-01-01"));
        client.setClientSubType(clientSubType());
        return client;
    }

    private ClientSubType clientSubType() {
        final ClientSubType clientSubType = new ClientSubType();
        clientSubType.setClientSubTypeCode("MAL");
        clientSubType.setClientType(createClientType());
        clientSubType.setDescription("Male");
        return clientSubType;
    }

    private ClientType createClientType() {
        final ClientType clientType = new ClientType();
        clientType.setClientTypeCode("I");
        clientType.setDescription("Individual");
        return clientType;
    }
}
