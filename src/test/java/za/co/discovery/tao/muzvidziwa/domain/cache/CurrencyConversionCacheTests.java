package za.co.discovery.tao.muzvidziwa.domain.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.co.discovery.tao.muzvidziwa.domain.model.cache.CurrencyConversionCache;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.ConversionRatesDto;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CurrencyConversionRate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurrencyConversionCacheTests {

    @Autowired
    private CurrencyConversionCache cache;

    private static final String TRACE_ID = "test-trace";
    private static final String CURRENCY_CODE = "USD";
    private static final String OTHER_CURRENCY_CODE = "EUR";

    @BeforeEach
    void setUp() {
        cache.clearCache(TRACE_ID);
    }

    @DisplayName("""
            test 'Add Currency Conversion Rate" should add a currency conversion rate into the cache
            """)
    @Test
    void addCurrencyConversionRate_givenValidCCRData_shouldAddAndRetrieveRate() {
        CurrencyConversionRate rate = new CurrencyConversionRate();
        rate.setCurrencyCode(CURRENCY_CODE);
        rate.setRate(BigDecimal.valueOf(18.5));
        rate.setConversionIndicator("*");

        cache.addCurrencyConversionRate(CURRENCY_CODE, rate, TRACE_ID);

        final ConversionRatesDto actualCRDto = cache.getCurrencyConversionRate(CURRENCY_CODE, TRACE_ID);
        assertNotNull(actualCRDto);
        assertEquals("18.5", actualCRDto.getConversionRate());
        assertEquals("*", actualCRDto.getConversionIndicator());
    }

    @DisplayName("""
           test 'Get Currency Conversion Rate' given that the currency conversion does not exist should return null""")
    @Test
    void getCurrencyConversionRate_givenCCRIsNotPresent_shouldReturnNullConversionDto() {
        final ConversionRatesDto actualCRDto = cache.getCurrencyConversionRate(OTHER_CURRENCY_CODE, TRACE_ID);
        assertNull(actualCRDto);
    }

    @DisplayName("""
            test 'Add Currency Conversion Rate' given that a duplicate entry exists should update the rate in the cache
            """)
    @Test
    void addCurrencyConversionRate_givenCCRExistsInCache_shouldUpdateCCRInCache() {
        CurrencyConversionRate rate1 = new CurrencyConversionRate();
        rate1.setCurrencyCode(CURRENCY_CODE);
        rate1.setRate(BigDecimal.valueOf(18.5));
        rate1.setConversionIndicator("*");

        CurrencyConversionRate rate2 = new CurrencyConversionRate();
        rate2.setCurrencyCode(CURRENCY_CODE);
        rate2.setRate(BigDecimal.valueOf(19.0));
        rate2.setConversionIndicator("/");

        cache.addCurrencyConversionRate(CURRENCY_CODE, rate1, TRACE_ID);
        cache.addCurrencyConversionRate(CURRENCY_CODE, rate2, TRACE_ID);

        final ConversionRatesDto actualCRDto = cache.getCurrencyConversionRate(CURRENCY_CODE, TRACE_ID);
        assertNotNull(actualCRDto);
        assertEquals("19.0", actualCRDto.getConversionRate());
        assertEquals("/", actualCRDto.getConversionIndicator());
    }

    @DisplayName("""
            test 'Clear Cache' should remove all entries in the cache
            """)
    @Test
    void clearCache_shouldRemoveAllEntries() {
        CurrencyConversionRate rate = new CurrencyConversionRate();
        rate.setCurrencyCode(CURRENCY_CODE);
        rate.setRate(BigDecimal.valueOf(18.5));
        rate.setConversionIndicator("*");

        cache.addCurrencyConversionRate(CURRENCY_CODE, rate, TRACE_ID);
        assertNotNull(cache.getCurrencyConversionRate(CURRENCY_CODE, TRACE_ID));

        cache.clearCache(TRACE_ID);
        assertNull(cache.getCurrencyConversionRate(CURRENCY_CODE, TRACE_ID));
    }

    @DisplayName("""
            test 'Add Currency Conversion Rate' with null currency conversion rate should add conversion to cache
            """)
    @Test
    void addCurrencyConversionRate_givenNullCCRRate_shouldStoreEmptyDto() {
        cache.addCurrencyConversionRate(CURRENCY_CODE, null, TRACE_ID);

        final ConversionRatesDto actualCRDto = cache.getCurrencyConversionRate(CURRENCY_CODE, TRACE_ID);

        assertNotNull(actualCRDto);
        assertNull(actualCRDto.getConversionRate());
        assertNull(actualCRDto.getConversionIndicator());
    }

    @DisplayName("""
            test 'Get Currency Conversion Rate' given null currency code should return null""")
    @Test
    void getCurrencyConversionRate_givenNullCCRCode_shouldReturnNull() {
        final ConversionRatesDto dto = cache.getCurrencyConversionRate(null, TRACE_ID);
        assertNull(dto);
    }

    @DisplayName("""
            test 'Add Currency Conversion Rate' given null currency code should not throw NPE on get(null)
            """)
    @Test
    void addCurrencyConversionRate_givenNullCurrencyCode_shouldNotThrowNPE() {
        CurrencyConversionRate rate = new CurrencyConversionRate();
        rate.setCurrencyCode(null);
        rate.setRate(BigDecimal.valueOf(10));
        rate.setConversionIndicator("*");

        assertDoesNotThrow(() -> cache.addCurrencyConversionRate(null, rate, TRACE_ID));
        // Should be retrievable with null key
        ConversionRatesDto actualCRDto = cache.getCurrencyConversionRate(null, TRACE_ID);
        assertNotNull(actualCRDto);
        assertEquals("10", actualCRDto.getConversionRate());
    }
}