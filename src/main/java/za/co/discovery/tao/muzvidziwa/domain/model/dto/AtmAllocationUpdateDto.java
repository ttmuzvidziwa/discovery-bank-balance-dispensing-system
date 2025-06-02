package za.co.discovery.tao.muzvidziwa.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AtmAllocationUpdateDto {
    private Long denominationId;
    private Integer balanceCount;
}
