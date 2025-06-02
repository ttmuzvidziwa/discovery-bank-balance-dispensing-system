package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DENOMINATION_TYPE")
public class DenominationType {
    @Id
    @Column(name = "DENOMINATION_TYPE_CODE", nullable = false, length = 1)
    private String denominationTypeCode;

    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;
}
