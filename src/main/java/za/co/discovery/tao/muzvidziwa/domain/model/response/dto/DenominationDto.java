package za.co.discovery.tao.muzvidziwa.domain.model.response.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import za.co.discovery.tao.muzvidziwa.domain.model.response.views.View;

import java.math.BigDecimal;

@Data
@JsonView({View.Transactional.class, View.Currency.class, View.Withdrawal.class})
public class DenominationDto {
    @Schema(description = "Currency denomination database ID", example = "9")
    private Long denominationId;

    @Schema(description = "Denomination value", example = "100.00")
    private BigDecimal denominationValue;

    @Schema(description = "Number of notes/coins available in this denomination", example = "5")
    private Integer count;
}
