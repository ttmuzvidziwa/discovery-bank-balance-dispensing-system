package za.co.discovery.tao.muzvidziwa.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import za.co.discovery.tao.muzvidziwa.domain.model.cache.CurrencyConversionCache;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CurrencyConversionRate;
import za.co.discovery.tao.muzvidziwa.domain.util.GeneralUtils;
import za.co.discovery.tao.muzvidziwa.domain.util.LoggerUtils;
import za.co.discovery.tao.muzvidziwa.repository.CurrencyConversionRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static za.co.discovery.tao.muzvidziwa.domain.constant.Sources.SCHEDULED_SYSTEM_TASK;

@Service
@RequiredArgsConstructor
public class ScheduledService {
    private final CurrencyConversionRepository currencyConversionRepository;
    private final CurrencyConversionCache currencyConversionCache;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Scheduled(cron = "0 0 * * * ?")
    // Pull currency conversion rate on start-up, then every hour.
    @Scheduled(initialDelay = 1000)
    public void findAndUpdateCurrencyConversionRates() {
        final String traceId = GeneralUtils.generateUniqueId();

        LoggerUtils.logInfo(traceId, SCHEDULED_SYSTEM_TASK, "Updating currency conversion rates.");

        Optional<List<CurrencyConversionRate>> currencyConversionRates = currencyConversionRepository.findAllCurrencyConversionRates();
        if (currencyConversionRates.isEmpty() || currencyConversionRates.get().isEmpty()) {
            LoggerUtils.logDebug(GeneralUtils.generateUniqueId(), SCHEDULED_SYSTEM_TASK, "No currency conversion rates found.");
            return;
        }

        for (CurrencyConversionRate currencyConversionRate : currencyConversionRates.get()) {
            if (currencyConversionRate != null
                    && currencyConversionRate.getCurrencyCode() != null
                    && currencyConversionRate.getCurrency() != null
                    && currencyConversionRate.getConversionIndicator() != null
                    && currencyConversionRate.getRate() != null) {
                final String currencyCode = currencyConversionRate.getCurrencyCode().toUpperCase();
                currencyConversionCache.addCurrencyConversionRate(currencyCode, currencyConversionRate, traceId);
            } else {
                LoggerUtils.logError(traceId, SCHEDULED_SYSTEM_TASK, "Invalid currency conversion rate found: {}", currencyConversionRate);
            }
        }
    }

    @Scheduled(cron = "0 0 0 L * ?")
    public void runTransactionalAccountBalanceReportingScript() {
        try {
            Resource resource = resourceLoader.getResource("classpath:/sql/trans-account-highest-balance-report.sql");
            String sql;
            try (InputStream inputStream = resource.getInputStream()) {
                sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            StringBuilder sb = new StringBuilder();
            // Add header line
            sb.append("Client Id, Client Surname, Client Account Number, Account Description, Display Balance")
                    .append(System.lineSeparator());

            jdbcTemplate.query(sql, (rs) -> {
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(rs.getString(i));
                    if (i < columnCount) sb.append(", ");
                }
                sb.append(System.lineSeparator());
            });

            Path reportDir = Path.of("src/main/resources/report");
            Files.createDirectories(reportDir);
            Path tempFile = reportDir.resolve("transactional_account_balance_report_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".txt");
            Files.writeString(tempFile, sb.toString(), StandardCharsets.UTF_8);
            LoggerUtils.logInfo(GeneralUtils.generateUniqueId(), SCHEDULED_SYSTEM_TASK, "Report written to: " + tempFile.toAbsolutePath());
        } catch (Exception e) {
            LoggerUtils.logError(GeneralUtils.generateUniqueId(), SCHEDULED_SYSTEM_TASK, "Error executing transactional balance script: {}", e.getMessage());
            throw new RuntimeException("Failed to execute transactional balance script", e);
        }
    }

    @Scheduled(cron = "0 0 0 L * ?")
    public void runClientAggregateFinancialPositionReportingScript() {
        try {
            Resource resource = resourceLoader.getResource("classpath:/sql/client-aggregate-financial-position-report.sql");
            String sql;
            try (InputStream inputStream = resource.getInputStream()) {
                sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            StringBuilder sb = new StringBuilder();
            // Add header line
            sb.append("Client, Loan Balance, Transactional Balance, Net Position")
                    .append(System.lineSeparator());

            jdbcTemplate.query(sql, (rs) -> {
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(rs.getString(i));
                    if (i < columnCount) sb.append(", ");
                }
                sb.append(System.lineSeparator());
            });

            Path reportDir = Path.of("src/main/resources/report");
            Files.createDirectories(reportDir);
            Path tempFile = reportDir.resolve("client_aggregate_financial_position_report_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".txt");
            Files.writeString(tempFile, sb.toString(), StandardCharsets.UTF_8);
            LoggerUtils.logInfo(GeneralUtils.generateUniqueId(), SCHEDULED_SYSTEM_TASK, "Report written to: " + tempFile.toAbsolutePath());
        } catch (Exception e) {
            LoggerUtils.logError(GeneralUtils.generateUniqueId(), SCHEDULED_SYSTEM_TASK, "Error executing transactional balance script: {}", e.getMessage());
            throw new RuntimeException("Failed to execute transactional balance script", e);
        }
    }
}
