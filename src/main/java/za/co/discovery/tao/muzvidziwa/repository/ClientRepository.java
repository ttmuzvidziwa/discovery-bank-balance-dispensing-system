package za.co.discovery.tao.muzvidziwa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    /**
     * Finds a client by their client ID.
     *
     * @param clientId the client ID to search for
     * @return an Optional containing the Client if found, or empty if not found
     */
    @Query(value = """
            select c.* 
            from CLIENT c 
                join CLIENT_SUB_TYPE cst on c.CLIENT_SUB_TYPE_CODE = cst.CLIENT_SUB_TYPE_CODE
                join CLIENT_TYPE ct on cst.CLIENT_TYPE_CODE = ct.CLIENT_TYPE_CODE
            where c.CLIENT_ID  = :clientId 
                        and ct.CLIENT_TYPE_CODE IN ('I', 'N')
            """, nativeQuery = true)
    Optional<Client> findByClientId(@Param("clientId") int clientId);
}
