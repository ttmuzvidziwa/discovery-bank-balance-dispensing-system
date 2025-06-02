package za.co.discovery.tao.muzvidziwa.domain.model.response.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import za.co.discovery.tao.muzvidziwa.domain.model.response.views.View;

@Data
@JsonView({View.Transactional.class, View.Currency.class, View.Withdrawal.class})
@Schema(description = "Client information")
public class ClientDto {
    @Schema(description = "Client ID", example = "12")
    private Long id;

    @Schema(description = "Title", example = "Mr")
    private String title;

    @Schema(description = "First/Given name", example = "Tao")
    private String name;

    @Schema(description = "Last/Surname", example = "Muzvidziwa")
    private String surname;
}
