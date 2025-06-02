package za.co.discovery.tao.muzvidziwa.domain.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CURRENCY_CONVERSION_RATE")
public class CurrencyConversionRate {
    @Id
    @Column(name = "CURRENCY_CODE", nullable = false, length = 3)
    private String currencyCode;

    @OneToOne
    @JoinColumn(name = "CURRENCY_CODE", referencedColumnName = "CURRENCY_CODE")
    private Currency currency;

    @Column(name = "CONVERSION_INDICATOR", nullable = false, length = 1)
    private String conversionIndicator;

    @Column(name = "RATE", nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;
}
