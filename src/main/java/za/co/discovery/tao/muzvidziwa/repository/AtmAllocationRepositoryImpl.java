package za.co.discovery.tao.muzvidziwa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.dto.AtmAllocationUpdateDto;

import java.util.List;

@Repository
public class AtmAllocationRepositoryImpl implements AtmAllocationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void updateDenominationCounts(long atmId, List<AtmAllocationUpdateDto> updates) {
        for (AtmAllocationUpdateDto update : updates) {
            entityManager.createNativeQuery(
                            "UPDATE ATM_ALLOCATION aa " +
                                    "SET aa.COUNT = :count " +
                                    "WHERE aa.ATM_ID = :atmId AND aa.DENOMINATION_ID = :denominationId"
                    )
                    .setParameter("count", update.getBalanceCount())
                    .setParameter("atmId", atmId)
                    .setParameter("denominationId", update.getDenominationId())
                    .executeUpdate();
            entityManager.flush();
        }

    }
}