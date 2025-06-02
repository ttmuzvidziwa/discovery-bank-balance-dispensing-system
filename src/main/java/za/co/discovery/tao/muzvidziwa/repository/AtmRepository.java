package za.co.discovery.tao.muzvidziwa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Atm;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtmRepository extends JpaRepository<Atm, Long> {
    /**
     * Finds an ATM by its ID.
     *
     * @return an Optional list of {@link Atm}containing the ATM if found, or empty if not found
     */
    @Query(value = """
            select a.*
            from ATM a 
            """, nativeQuery = true)
    Optional<List<Atm>> findAllAtms();

    /**
     * Checks if an ATM exists by its ID.
     *
     * @param atmId the ID of the ATM to check
     * @return true if the ATM exists, false otherwise
     */
    @Query(value = """
            select case 
                when count(a.*) = 1 
                then true 
                    else false 
                end
            from ATM a
            where a.ATM_ID = :atmId
            """, nativeQuery = true)
    boolean atmExistsByAtmId(@Param("atmId") final long atmId);
}
