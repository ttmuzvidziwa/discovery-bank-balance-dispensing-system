package za.co.discovery.tao.muzvidziwa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.AtmAllocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtmAllocationRepository extends JpaRepository<AtmAllocation, Long>, AtmAllocationRepositoryCustom {
    /**
     * This method retrieves an ATM allocation by its ATM ID.
     *
     * @param atmId the ID of the ATM for which to retrieve the allocation
     * @return an Optional containing the AtmAllocation entity if found, or empty if none exists
     */
    @Query(value = """
            select aa.*
            from ATM_ALLOCATION aa
            join ATM a on aa.ATM_ID = a.ATM_ID
            where aa.ATM_ID  = :atmId
            """, nativeQuery = true)
    Optional<List<AtmAllocation>> findAtmAllocationByAtmId(@Param("atmId") final long atmId);
}
