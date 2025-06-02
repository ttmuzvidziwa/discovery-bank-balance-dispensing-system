package za.co.discovery.tao.muzvidziwa.domain.constant;

public enum Sources {
    BANK_SERVICE("Bank Service"),
    BANK_CONTROLLER("Bank Controller"),
    BANK_REPOSITORY("Bank Repository"),
    SCHEDULED_SYSTEM_TASK("Scheduled Task"),
    CURRENCY_CONVERSION_CACHE("Currency Conversion Cache");

    public final String source;

    Sources(final String source) {
        this.source = source;
    }
}
