package za.co.discovery.tao.muzvidziwa.domain.model.dto;

import lombok.Data;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.DenominationDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AtmStateDto {
    private Map<BigDecimal, DenominationDto> denominationMap;
    private List<BigDecimal> denominationList;
    private BigDecimal totalAmount;
    private List<AtmAllocationUpdateDto> atmAllocationUpdateList = new ArrayList<>();

    public void addAtmAllocationUpdate(final AtmAllocationUpdateDto atmAllocationUpdate) {
        this.atmAllocationUpdateList.add(atmAllocationUpdate);
    }
}
