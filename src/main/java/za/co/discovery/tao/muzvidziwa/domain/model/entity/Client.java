package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import java.sql.Date;

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
@Table(name = "CLIENT")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_ID")
    private Integer clientId;

    @Column(name = "TITLE", length = 10)
    private String title;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "SURNAME", length = 100)
    private String surname;

    @Column(name = "DOB", nullable = false)
    private Date dob;

    @ManyToOne
    @JoinColumn(name = "CLIENT_SUB_TYPE_CODE", nullable = false)
    private ClientSubType clientSubType;
}
