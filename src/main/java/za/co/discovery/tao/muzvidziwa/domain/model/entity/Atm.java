package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "ATM")
public class Atm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ATM_ID")
    private Integer atmId;

    @Column(name = "NAME", nullable = false, length = 10, unique = true)
    private String name;

    @Column(name = "LOCATION", nullable = false, length = 255)
    private String location;
}
