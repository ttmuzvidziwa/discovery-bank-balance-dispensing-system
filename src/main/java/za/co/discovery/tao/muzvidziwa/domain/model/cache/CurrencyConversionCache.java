package za.co.discovery.tao.muzvidziwa.domain.model.cache;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.ConversionRatesDto;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CurrencyConversionRate;
import za.co.discovery.tao.muzvidziwa.domain.util.LoggerUtils;

import java.util.HashMap;
import java.util.Map;

import static za.co.discovery.tao.muzvidziwa.domain.constant.Sources.CURRENCY_CONVERSION_CACHE;

/**
 * This class represents the cache for currency conversion rates, stored in a map of <CurrencyCode, {@link ConversionRatesDto}>.
 * */

@Data
@NoArgsConstructor
@Component
public class CurrencyConversionCache {
    private Map<String, ConversionRatesDto> currencyConversionRatesMap = new HashMap<>();

    /**
     * Retrieves the currency conversion rate for the specified currency code from the cache.
     * <p>
     * If the conversion rate is not found, a debug log is generated.
     * If the cache map is not initialized, a debug log is also generated.
     *
     * @param currencyCode the currency code to look up
     * @return the {@link ConversionRatesDto} for the given currency code, or {@code null} if not found or if the map is not initialized
     */
    public ConversionRatesDto getCurrencyConversionRate(final String currencyCode, final String traceId) {
        if (currencyConversionRatesMap != null) {
            final ConversionRatesDto currencyConversionRateDto = currencyConversionRatesMap.get(currencyCode);

            if (currencyConversionRateDto == null) {
                LoggerUtils.logDebug(traceId, CURRENCY_CONVERSION_CACHE, "Currency conversion rate for code {} not found in the cache.", currencyCode);
            }

            return currencyConversionRateDto;
        }

        LoggerUtils.logDebug(traceId, CURRENCY_CONVERSION_CACHE, "Currency conversion rates map is not initialized or empty.");
        return null;
    }

    /**
     * Adds a currency conversion rate to the cache.
     * If a rate for the given currency code already exists, it will be updated.
     *
     * @param currencyCode the currency code
     * @param currencyConversionRate the CurrencyConversionRate entity to be added
     */
    public void addCurrencyConversionRate(final String currencyCode, final CurrencyConversionRate currencyConversionRate, final String traceId) {
        if (currencyConversionRatesMap != null) {
            var duplicateRate = currencyConversionRatesMap.put(currencyCode, prepareConversionRateDto(currencyConversionRate));

            if (duplicateRate != null) {
                LoggerUtils.logDebug(traceId, CURRENCY_CONVERSION_CACHE, "Currency conversion rate for code {} updated.", currencyCode);
                return;
            }
            LoggerUtils.logDebug(traceId, CURRENCY_CONVERSION_CACHE, "Added currency conversion rate for code {} to the cache.", currencyCode);
        } else {
            LoggerUtils.logError(traceId, CURRENCY_CONVERSION_CACHE, "Currency conversion rates map is not initialized.");
        }
    }

    public void clearCache(final String traceId) {
        if (currencyConversionRatesMap != null) {
            currencyConversionRatesMap.clear();
            LoggerUtils.logInfo(traceId, CURRENCY_CONVERSION_CACHE, "Currency conversion rates cache cleared.");
        } else {
            LoggerUtils.logDebug(traceId, CURRENCY_CONVERSION_CACHE, "Currency conversion rates map is already empty.");
        }
    }

    /**
     * Prepares a ConversionRatesDto from a CurrencyConversionRate entity.
     *
     * @param currencyConversionRate the CurrencyConversionRate entity
     * @return ConversionRatesDto containing the conversion rate and indicator
     */
    private ConversionRatesDto prepareConversionRateDto(final CurrencyConversionRate currencyConversionRate) {
        final ConversionRatesDto conversionRatesDto = new ConversionRatesDto();

        if (currencyConversionRate == null) {
            return conversionRatesDto;
        }

        // TODO Tao: Change this to BigDecimal instead of String
        conversionRatesDto.setConversionRate(currencyConversionRate.getRate().toString());
        conversionRatesDto.setConversionIndicator(currencyConversionRate.getConversionIndicator());

        return conversionRatesDto;
    }
}
