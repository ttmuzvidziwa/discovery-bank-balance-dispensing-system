package za.co.discovery.tao.muzvidziwa.domain.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import za.co.discovery.tao.muzvidziwa.domain.util.GeneralUtils;

import java.math.BigDecimal;

public class GeneralUtilsTests {

    @Test
    public void isPositiveInteger_givenNullIntgerValue_shouldReturnFalse() {
        Assertions.assertFalse(GeneralUtils.isPositiveInteger(null));
    }

    @Test
    public void isPositiveInteger_givenNegativeIntegerValue_shouldReturnFalse() {
        Assertions.assertFalse(GeneralUtils.isPositiveInteger(-1));
    }

    @Test
    public void isPositiveInteger_givenZeroIntegerValue_shouldReturnFalse() {
        Assertions.assertFalse(GeneralUtils.isPositiveInteger(0));
    }

    @Test
    public void isPositiveInteger_givenPositiveIntegerValue_shouldReturnTrue() {
        Assertions.assertTrue(GeneralUtils.isPositiveInteger(1));
    }

    @Test
    public void isPositiveBigDecimal_givenNullBigDecimalValue_shouldReturnFalse() {
        Assertions.assertFalse(GeneralUtils.isPositiveBigDecimal(null));
    }

    @Test
    public void isPositiveBigDecimal_givenNegativeBigDecimalValue_shouldReturnFalse() {
        Assertions.assertFalse(GeneralUtils.isPositiveBigDecimal(new BigDecimal("-1")));
    }

    @Test
    public void isPositiveBigDecimal_givenZeroBigDecimalValue_shouldReturnFalse() {
        Assertions.assertFalse(GeneralUtils.isPositiveBigDecimal(BigDecimal.ZERO));
    }

    @Test
    public void isPositiveBigDecimal_givenPositiveBigDecimalValue_shouldReturnTrue() {
        Assertions.assertTrue(GeneralUtils.isPositiveBigDecimal(new BigDecimal("1")));
    }

    @Test
    public void parseInteger_givenNullString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseInteger(null));
    }

    @Test
    public void parseInteger_givenEmptyString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseInteger(""));
    }

    @Test
    public void parseInteger_givenPositiveIntegerValue_shouldReturnInteger() {
        final Integer actual = 123;
        Assertions.assertEquals(actual, GeneralUtils.parseInteger("123"));
    }

    @Test
    public void parseInteger_givenNegativeIntegerValue_shouldReturnInteger() {
        final Integer actual = -123;
        Assertions.assertEquals(actual, GeneralUtils.parseInteger("-123"));
    }

    @Test
    public void parseInteger_givenInvalidIntegerString_shouldReturnNull() {
        final String input = "abc";
        Assertions.assertNull(GeneralUtils.parseInteger(input));
    }

    @Test
    public void parseStringToLong_givenNullString_shouldReturnNull() {
        final String input = null;
        Assertions.assertNull(GeneralUtils.parseStringToLong(input));
    }

    @Test
    public void parseStringToLong_givenEmptyString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseStringToLong(""));
    }

    @Test
    public void parseStringToLong_givenValidLongString_shouldReturnLong() {
        final Long expected = 123456789L;
        Assertions.assertEquals(expected, GeneralUtils.parseStringToLong("123456789"));
    }

    @Test
    public void parseStringToLong_givenInvalidLongString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseStringToLong("invalid"));
    }

    @Test
    public void parseIntToLong_givenNullInteger_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseIntToLong(null));
    }

    @Test
    public void parseIntToLong_givenValidInteger_shouldReturnPOsitiveLong() {
        final Long expected = 123L;
        Assertions.assertEquals(expected, GeneralUtils.parseIntToLong(123));
    }

    @Test
    public void parseIntToLong_givenNegativeInteger_shouldReturnNegativeLong() {
        final Long expected = -123L;
        Assertions.assertEquals(expected, GeneralUtils.parseIntToLong(-123));
    }

    @Test
    public void parseBigDecimal_givenNullString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseBigDecimal(null));
    }

    @Test
    public void parseBigDecimal_givenEmptyString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseBigDecimal(""));
    }

    @Test
    public void parseBigDecimal_givenInvalidString_shouldReturnNull() {
        Assertions.assertNull(GeneralUtils.parseBigDecimal("qwerty"));
    }

    @Test
    public void parseBigDecimal_givenPositiveNumericalString_shouldReturnBigDecimal() {
        Assertions.assertEquals(BigDecimal.valueOf(12.25), GeneralUtils.parseBigDecimal("12.25"));
    }

    @Test
    public void parseBigDecimal_givenNegativeNumericalString_shouldReturnBigDecimal() {
        Assertions.assertEquals(BigDecimal.valueOf(-12.25), GeneralUtils.parseBigDecimal("-12.25"));
    }

    @Test
    public void parseBigDecimal_givenZeroNumericalString_shouldReturnBigDecimal() {
        Assertions.assertEquals(BigDecimal.ZERO, GeneralUtils.parseBigDecimal("0"));
    }

    @Test
    public void generateUniqueId_shouldReturnNonNullString() {
        Assertions.assertNotNull(GeneralUtils.generateUniqueId());
    }
}
