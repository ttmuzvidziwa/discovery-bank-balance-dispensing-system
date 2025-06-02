package za.co.discovery.tao.muzvidziwa.domain.model.response.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.discovery.tao.muzvidziwa.domain.model.response.views.View;

@Data
@EqualsAndHashCode
@JsonView({View.Transactional.class, View.Currency.class, View.Withdrawal.class})
public class ResultDto {
    @Schema(description = "Indicates whether the operation was successful", example = "true")
    private boolean success;

    @Schema(description = "Status code of the operation", example = "200")
    private int statusCode;

    @Schema(description = "Message providing additional information about the operation", example = "Withdrawal successful")
    private String statusReason;
}
