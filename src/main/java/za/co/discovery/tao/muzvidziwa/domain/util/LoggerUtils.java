package za.co.discovery.tao.muzvidziwa.domain.util;

import lombok.extern.slf4j.Slf4j;
import za.co.discovery.tao.muzvidziwa.domain.constant.Sources;

@Slf4j
public final class LoggerUtils {
    public static void logInfo(final String traceId, final Sources source, final String message, final Object... args) {
        log.info(formatLogMessage(traceId, source, message, args));
    }

    public static void logError(final String traceId, final Sources source, final String message, final Object... args) {
        log.error(formatLogMessage(traceId, source, message, args));
    }

    public static void logDebug(final String traceId, final Sources source, final String message, final Object... args) {
        log.debug(formatLogMessage(traceId, source, message, args));
    }

    private static String formatLogMessage(final String traceId, final Sources source, final String message, final Object... args) {
        final StringBuilder logMessage = new StringBuilder();
        String formattedMessage = message != null ? message : "No message provided";

        if (args != null && args.length > 0 && message != null) {
            int argIndex = 0;
            StringBuilder sb = new StringBuilder();
            int start = 0;
            int idx;
            while ((idx = message.indexOf("{}", start)) != -1) {
                sb.append(message, start, idx);
                if (argIndex < args.length) {
                    sb.append(args[argIndex] != null ? args[argIndex].toString() : "NULL");
                } else {
                    sb.append("NULL");
                }
                argIndex++;
                start = idx + 2;
            }
            sb.append(message.substring(start));
            formattedMessage = sb.toString();
        }

        logMessage.append(traceId != null ? traceId + " - " : "No trace ID - ")
                .append(source != null ? source.source + " - " : "No source - ")
                .append(formattedMessage);

        return logMessage.toString();
    }
}
