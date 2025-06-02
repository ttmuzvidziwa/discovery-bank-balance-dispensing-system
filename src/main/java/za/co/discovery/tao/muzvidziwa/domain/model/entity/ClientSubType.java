package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CLIENT_SUB_TYPE")
public class ClientSubType {
    @Id
    @Column(name = "CLIENT_SUB_TYPE_CODE", nullable = false, length = 4)
    private String clientSubTypeCode;

    @ManyToOne
    @JoinColumn(name = "CLIENT_TYPE_CODE", nullable = false)
    private ClientType clientType;

    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;
}
