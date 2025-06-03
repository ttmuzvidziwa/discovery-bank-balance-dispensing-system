package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "ATM_ALLOCATION")
public class AtmAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ATM_ALLOCATION_ID")
    private Integer atmAllocationId;

    @ManyToOne
    @JoinColumn(name = "ATM_ID", nullable = false)
    private Atm atm;

    @ManyToOne
    @JoinColumn(name = "DENOMINATION_ID", nullable = false)
    private Denomination denomination;

    @Column(name = "COUNT", nullable = false)
    private Integer count;
}
