package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "DENOMINATION")
public class Denomination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DENOMINATION_ID")
    private Integer denominationId;

    @Column(name = "DENOMINATION_VALUE", nullable = false, precision = 18, scale = 2)
    private BigDecimal denominationValue;

    @ManyToOne
    @JoinColumn(name = "DENOMINATION_TYPE_CODE")
    private DenominationType denominationType;
}
