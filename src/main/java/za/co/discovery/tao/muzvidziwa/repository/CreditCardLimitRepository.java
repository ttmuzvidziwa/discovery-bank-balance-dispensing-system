package za.co.discovery.tao.muzvidziwa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.CreditCardLimit;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CreditCardLimitRepository extends JpaRepository<CreditCardLimit, Long> {

    @Query(value = """
            select ccl.ACCOUNT_LIMIT
            from CREDIT_CARD_LIMIT ccl
            join CLIENT_ACCOUNT ca on ccl.CLIENT_ACCOUNT_NUMBER = ca.CLIENT_ACCOUNT_NUMBER
            join ACCOUNT_TYPE at on ca.ACCOUNT_TYPE_CODE = at.ACCOUNT_TYPE_CODE
            where ccl.CLIENT_ACCOUNT_NUMBER = :accountNumber
                 and at.ACCOUNT_TYPE_CODE = 'CCRD'
                 and at.TRANSACTIONAL = true
            """, nativeQuery = true)
    Optional<BigDecimal> findCreditCardLimitByClientAccountNumber(@Param("accountNumber") final String clientAccountNumber);
}
