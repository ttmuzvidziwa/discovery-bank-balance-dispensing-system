package za.co.discovery.tao.muzvidziwa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@ActiveProfiles("test")
@DataJpaTest
public class CreditCardLimitRepositoryTests {
    @Autowired
    private CreditCardLimitRepository creditCardLimitRepository;

    @DisplayName("""
            test 'Find Credit Card Limit By Client Account Number' where client account is a CC and has a limit should return limit value
            """)
    @Test
    public void findCreditCardLimitByClientAccountNumber_givenAccountNumberInDB_shouldReturnCardLimit() {
        // Perform SUT
        Optional<BigDecimal> actualCCLimit = creditCardLimitRepository.findCreditCardLimitByClientAccountNumber("1003");

        // Verify results
        Assertions.assertTrue(actualCCLimit.isPresent());
        Assertions.assertEquals(BigDecimal.valueOf(2500).setScale(3, RoundingMode.HALF_UP), actualCCLimit.get());
    }

    @DisplayName("""
            test 'Find Credit Card Limit By Client Account Number' where client account is a CC and has a limit should return limit value
            """)
    @Test
    public void findCreditCardLimitByClientAccountNumber_givenNoAccountNumberInDB_shouldReturnCardLimit() {
        // Perform SUT
        Optional<BigDecimal> actualCCLimit = creditCardLimitRepository.findCreditCardLimitByClientAccountNumber("1002");

        // Verify results
        Assertions.assertFalse(actualCCLimit.isPresent());
    }
}
