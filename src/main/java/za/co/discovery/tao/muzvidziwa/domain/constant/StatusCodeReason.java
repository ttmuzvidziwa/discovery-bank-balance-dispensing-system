package za.co.discovery.tao.muzvidziwa.domain.constant;

public enum StatusCodeReason {
    NO_ACCOUNT_TO_DSPLAY_SCR(400, "No accounts to display"),
    INVALID_CLIENT_IDENTIFIER_SCR(400, "Invalid client identifier (ID) provided"),
    INVALID_CLIENT_ACCOUTN_NUMBER_SCR(400, "Invalid client account number provided"),
    INVALID_WITHDRAWAL_AMOUNT_SCR(400, "Invalid withdrawal amount requested"),
    NO_CLIENT_WITH_ID_SCR(400, "No client with the provided ID found"),
    DISPLAY_TRANSACTIONAL_ACCOUNTS_SCR(200, "Displaying transactional accounts"),
    DISPLAY_FOREIGN_CURRENCY_ACCOUNT_SCR(200, "Displaying foreign currency accounts"),
    WITHDRAWAL_SUCCESSFUL_SCR(200, "Withdrawal successful"),
    INSUFFICIENT_FUNDS_SCR(400, "Insufficient funds"),
    ATM_NOT_FOUND_UNFUNDED_SCR(400, "ATM not registered or unfunded"),
    WITHDRAWAL_AMOUNT_NOT_AVAILABLE_SCR(400, "Amount not available, would you like to draw "),
    GENERAL_ERROR_SCR(500, "An error occurred while processing your request");

    public final int statusCode;
    public final String statusReason;

    StatusCodeReason(final int statusCode, final String statusReason) {
        this.statusCode = statusCode;
        this.statusReason = statusReason;
    }
}
