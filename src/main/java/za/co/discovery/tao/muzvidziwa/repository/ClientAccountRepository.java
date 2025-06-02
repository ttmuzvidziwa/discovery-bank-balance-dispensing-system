package za.co.discovery.tao.muzvidziwa.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.ClientAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccount, Long> {
    /**
     * This method retrieves all client accounts for a given client ID that are transactional.
     *
     * @param clientId the ID of the client for whom to retrieve accounts
     * @return an Optional containing a list of ClientAccount entities if found, or empty if none exist
     * */
    @Query(value = """
            select ca.* 
            from CLIENT_ACCOUNT ca
            join CLIENT c on ca.CLIENT_ID = c.CLIENT_ID
            join ACCOUNT_TYPE at on ca.ACCOUNT_TYPE_CODE = at.ACCOUNT_TYPE_CODE
            where c.CLIENT_ID = :clientId
                and at.TRANSACTIONAL = true
            """, nativeQuery = true)
    Optional<List<ClientAccount>> findTransactionalClientAccountsByClientId(@Param("clientId") final long clientId);

    /**
     * This method retrieves all client accounts for a given client ID and account type.
     *
     * @param clientId the ID of the client for whom to retrieve accounts
     * @param accountType the type of account to filter by
     * @return an Optional containing a list of ClientAccount entities if found, or empty if none exist
     * */
    @Query(value = """
            select ca.* 
            from CLIENT_ACCOUNT ca
            join CLIENT c on ca.CLIENT_ID = c.CLIENT_ID
            join ACCOUNT_TYPE at on ca.ACCOUNT_TYPE_CODE = at.ACCOUNT_TYPE_CODE
            join CLIENT_SUB_TYPE cst on c.CLIENT_SUB_TYPE_CODE = cst.CLIENT_SUB_TYPE_CODE
            join CLIENT_TYPE ct on cst.CLIENT_TYPE_CODE = ct.CLIENT_TYPE_CODE
            where c.CLIENT_ID = :clientId
              and at.ACCOUNT_TYPE_CODE = :accountType
              and ct.CLIENT_TYPE_CODE IN ('I', 'N')
            """, nativeQuery = true)
    Optional<List<ClientAccount>> findClientAccountsByClientIdAndAccountType(@Param("clientId") final long clientId, @Param("accountType") final String accountType);

    /**
     * This method retrieves a transactional client account for a given client ID and account number.
     *
     * @param clientId the ID of the client for whom to retrieve account
     * @param accountNumber the account number to retrieve
     * @return an Optional containing a single ClientAccount entity if found, or empty if ond does not exist
     * */
    @Query(value = """
            select ca.* 
            from CLIENT_ACCOUNT ca
            join CLIENT c on ca.CLIENT_ID = c.CLIENT_ID
            join ACCOUNT_TYPE at on ca.ACCOUNT_TYPE_CODE = at.ACCOUNT_TYPE_CODE
            join CLIENT_SUB_TYPE cst on c.CLIENT_SUB_TYPE_CODE = cst.CLIENT_SUB_TYPE_CODE
            join CLIENT_TYPE ct on cst.CLIENT_TYPE_CODE = ct.CLIENT_TYPE_CODE                                 
            where c.CLIENT_ID = :clientId
              and ca.CLIENT_ACCOUNT_NUMBER = :accountNumber
              and ct.CLIENT_TYPE_CODE IN ('I', 'N')
              and at.TRANSACTIONAL = true
            """, nativeQuery = true)
    Optional<ClientAccount> findClientAccountByClientIdAndAccountNumber(@Param("clientId") final long clientId, @Param("accountNumber") final String accountNumber);

    /**
     * This method updates the display balance of a client account identified by its account number.
     *
     * @param clientAccountNumber the account number of the client account to update
     * @param displayBalance the new display balance to set
     * @return the number of rows affected by the update operation, which should be 1 if the update was successful
     */
    @Transactional
    @Modifying
    @Query(value = """
            update CLIENT_ACCOUNT ca
            set ca.DISPLAY_BALANCE = :displayBalance
            where ca.CLIENT_ACCOUNT_NUMBER = :clientAccountNumber
                and ca.CLIENT_ID = :clientId
            """, nativeQuery = true)
    void updateClientAccountByAccountNumber(@Param("clientId") final long clientId,
                                           @Param("clientAccountNumber") final String clientAccountNumber,
                                           @Param("displayBalance") final BigDecimal displayBalance);
}
