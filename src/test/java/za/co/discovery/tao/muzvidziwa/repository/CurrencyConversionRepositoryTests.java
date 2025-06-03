package za.co.discovery.tao.muzvidziwa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Currency;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CurrencyConversionRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@DataJpaTest
public class CurrencyConversionRepositoryTests {
    @Autowired
    private CurrencyConversionRepository currencyConversionRepository;

    @DisplayName("""
            test 'Find All Currency Conversion Rates' where conversion rates are in DB should return currency conversion rate
            """)
    @Test
    public void findAllCurrencyConversionRates_givenCCRInDB_shouldReturnCurrencyConversionRate() throws Exception {
        // Prepare expectations
        final List<CurrencyConversionRate> expectedCCR = List.of(createCurrencyConversionRate());

        // Perform SUT
        Optional<List<CurrencyConversionRate>> actualCCR = currencyConversionRepository.findAllCurrencyConversionRates();

        // Verify results
        Assertions.assertTrue(actualCCR.isPresent());
        Assertions.assertEquals(expectedCCR, actualCCR.get());
    }

    private CurrencyConversionRate createCurrencyConversionRate() {
        final CurrencyConversionRate ccr = new CurrencyConversionRate();
        ccr.setCurrencyCode("USD");
        ccr.setCurrency(createCurrency());
        ccr.setConversionIndicator("*");
        ccr.setRate(BigDecimal.valueOf(11.6167).setScale(8, RoundingMode.HALF_UP));
        return ccr;
    }

    private Currency createCurrency() {
        final Currency currency = new Currency();
        currency.setCurrencyCode("USD");
        currency.setDescription("United States dollar");
        currency.setDecimalPlaces(2);
        return currency;
    }
}
