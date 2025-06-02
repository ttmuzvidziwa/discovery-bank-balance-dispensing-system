package za.co.discovery.tao.muzvidziwa.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.AtmAllocationUpdateDto;

import java.util.List;

public interface AtmAllocationRepositoryCustom {

    @Transactional
    void updateDenominationCounts(long atmId, List<AtmAllocationUpdateDto> updates);
}