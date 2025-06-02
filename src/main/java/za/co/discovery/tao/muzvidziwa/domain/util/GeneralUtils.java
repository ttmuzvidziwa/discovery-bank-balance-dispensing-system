package za.co.discovery.tao.muzvidziwa.domain.util;

import java.math.BigDecimal;

public final class GeneralUtils {
    public static boolean isPositiveInteger(final Integer input) {
        if (input == null) {
            return false;
        }
        return input > 0;
    }

    public static boolean isPositiveBigDecimal(final BigDecimal input) {
        if (input == null) {
            return false;
        }
        return input.compareTo(BigDecimal.ZERO) > 0;
    }

    public static Integer parseInteger(final String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseStringToLong(final String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseIntToLong(final Integer input) {
        if (input == null) {
            return null;
        }
        return Long.valueOf(input);
    }

    public static BigDecimal parseBigDecimal(final String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
}
