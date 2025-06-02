package za.co.discovery.tao.muzvidziwa.domain.model.response.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import za.co.discovery.tao.muzvidziwa.domain.model.response.views.View;

import java.math.BigDecimal;

@Data
@JsonView({View.Transactional.class, View.Currency.class, View.Withdrawal.class})
public class AccountDto {
    @Schema(description = "Client's bank account number", example = "4067342946")
    private Long accountNumber;

    @Schema(description = "Bank account type", example = "CHQ")
    private String typeCode;

    @Schema(description = "Bank account type description", example = "Cheque Account")
    private String accountTypeDescription;

    @Schema(description = "Currency code", example = "ZAR")
    private String currencyCode;

    @Schema(description = "Currency conversion rate to ZAR", example = "1.000")
    private BigDecimal conversionRate;

    @JsonView({View.Transactional.class, View.Withdrawal.class})
    @Schema(description = "Account balance in ZAR", example = "1000.00")
    private BigDecimal balance;

    @JsonView(View.Currency.class)
    @Schema(description = "Account balance in account currency", example = "500.00")
    private BigDecimal ccyBalance;

    @Schema(description = "Account balance in ZAR. For foreign currency accounts this balance would be the converted value to ZAR", example = "500.00")
    private BigDecimal zarBalance;

    @Schema(description = "Account limit in account currency", example = "2000.00")
    private BigDecimal accountLimit;
}
