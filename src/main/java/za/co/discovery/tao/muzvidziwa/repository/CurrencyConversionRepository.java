package za.co.discovery.tao.muzvidziwa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CurrencyConversionRate;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyConversionRepository extends JpaRepository<CurrencyConversionRate, Long> {
    @Query(value = """
            select ccr.* 
            from CURRENCY_CONVERSION_RATE ccr
            """, nativeQuery = true)
    Optional<List<CurrencyConversionRate>> findAllCurrencyConversionRates();
}
