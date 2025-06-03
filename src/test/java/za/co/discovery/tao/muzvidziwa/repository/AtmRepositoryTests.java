package za.co.discovery.tao.muzvidziwa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Atm;

import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@DataJpaTest
public class AtmRepositoryTests {
    @Autowired
    private AtmRepository atmRepository;

    @DisplayName("""
            test 'Find All Atms' where there are ATMs in the DB should return list of ATMs
            """)
    @Test
    public void findAllAtms_givenATMInDB_shouldReturnListOfAtms() throws Exception {
        // Prepare expectations
        final Atm expectedAtm = createAtm();

        // Perform SUT
        Optional<List<Atm>> atmListInMemory = atmRepository.findAllAtms();

        // Verify results
        Assertions.assertTrue(atmListInMemory.isPresent());
        Assertions.assertEquals(1, atmListInMemory.get().size());
        Assertions.assertEquals(expectedAtm.getAtmId(), atmListInMemory.get().get(0).getAtmId());
        Assertions.assertEquals(expectedAtm.getName(), atmListInMemory.get().get(0).getName());
        Assertions.assertEquals(expectedAtm.getLocation(), atmListInMemory.get().get(0).getLocation());
    }

    @DisplayName("""
            test 'ATM Exists By Id' where there are ATMs in the DB should return TRUE
            """)
    @Test
    public void atmExistsByAtmId_givenATMInDB_shouldReturnTrue() throws Exception {
        // Perform SUT and verify results
        Assertions.assertTrue(atmRepository.atmExistsByAtmId(1));
    }

    @DisplayName("""
            test 'ATM Exists By Id' where there no are ATMs in the DB should return FALSE
            """)
    @Test
    public void atmExistsByAtmId_givenNoATMInDB_shouldReturnTFalse() throws Exception {
        // Perform SUT and verify results
        Assertions.assertFalse(atmRepository.atmExistsByAtmId(2));
    }

    private Atm createAtm() {
        final Atm atm = new Atm();
        atm.setAtmId(1);
        atm.setName("ATM1");
        atm.setLocation("Test Location");

        return atm;
    }
}
