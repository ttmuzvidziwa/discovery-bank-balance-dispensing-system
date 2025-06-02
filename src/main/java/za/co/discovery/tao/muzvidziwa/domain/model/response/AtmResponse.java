package za.co.discovery.tao.muzvidziwa.domain.model.response;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.AccountDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ClientDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.DenominationDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ResultDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.views.View;

import java.util.List;

@Data
@EqualsAndHashCode
public class AtmResponse {
    @JsonView({View.Transactional.class, View.Currency.class, View.Withdrawal.class})
    private ClientDto client;

    @JsonView({View.Transactional.class, View.Currency.class})
    private List<AccountDto> accounts;

    @JsonView({View.Withdrawal.class})
    private AccountDto account;

    @JsonView({View.Withdrawal.class})
    private List<DenominationDto> denomination;

    @JsonView({View.Transactional.class, View.Currency.class, View.Withdrawal.class})
    private ResultDto result;
}
