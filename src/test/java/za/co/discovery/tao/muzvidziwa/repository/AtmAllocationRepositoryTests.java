package za.co.discovery.tao.muzvidziwa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Atm;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.AtmAllocation;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Denomination;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.DenominationType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@DataJpaTest
public class AtmAllocationRepositoryTests {
    @Autowired
    private AtmAllocationRepository atmAllocationRepository;

    @DisplayName("""
            test 'FIND ATM ALLOCATION' where the ATM_ALLOCATIONs exists in the DB should return list of ATM_ALLOCATIONs
            """)
    @Test
    public void findAtmAllocationByAtmId_givenAtmAllocationInDB_shouldReturnList() throws Exception {
        // Prepare expectations
        final Atm atm = createAtm();

        final AtmAllocation atmAllocation10Denomination = new AtmAllocation();
        atmAllocation10Denomination.setAtmAllocationId(1);
        atmAllocation10Denomination.setAtm(atm);
        atmAllocation10Denomination.setDenomination(create10Denomination());
        atmAllocation10Denomination.setCount(10);

        List<AtmAllocation> expectedAtmAllocationList = List.of(atmAllocation10Denomination);

        // Perform SUT
        final Optional<List<AtmAllocation>> atmAllocationList = atmAllocationRepository.findAtmAllocationByAtmId(1L);

        Assertions.assertTrue(atmAllocationList.isPresent());
        Assertions.assertEquals(1, atmAllocationList.get().size());
        Assertions.assertEquals(expectedAtmAllocationList.get(0).getAtm(), atmAllocationList.get().get(0).getAtm());
        Assertions.assertEquals(expectedAtmAllocationList.get(0).getDenomination(), atmAllocationList.get().get(0).getDenomination());
        Assertions.assertEquals(expectedAtmAllocationList.get(0).getCount(), atmAllocationList.get().get(0).getCount());
    }

    @DisplayName("""
            test 'FIND ATM ALLOCATION' where the ATM_ALLOCATIONs exists in the DB should return list of ATM_ALLOCATIONs
            """)
    @Test
    public void findAtmAllocationByAtmId_givenNoAtmAllocationInDB_shouldReturnEmptyList() throws Exception {
        // Prepare expectations
        final Atm atm = createAtm();

        final AtmAllocation atmAllocation10Denomination = new AtmAllocation();
        atmAllocation10Denomination.setAtmAllocationId(1);
        atmAllocation10Denomination.setAtm(atm);
        atmAllocation10Denomination.setDenomination(create10Denomination());
        atmAllocation10Denomination.setCount(10);

        List<AtmAllocation> expectedAtmAllocationList = List.of(atmAllocation10Denomination);

        // Perform SUT
        final Optional<List<AtmAllocation>> atmAllocationList = atmAllocationRepository.findAtmAllocationByAtmId(3);

        Assertions.assertTrue(atmAllocationList.isPresent());
        Assertions.assertEquals(0, atmAllocationList.get().size());
    }

    private Atm createAtm() {
        final Atm atm = new Atm();
        atm.setAtmId(1);
        atm.setName("ATM1");
        atm.setLocation("Test Location");

        return atm;
    }

    private Denomination create10Denomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationType(createNoteDenominationType());
        denomination.setDenominationValue(BigDecimal.valueOf(10.00).setScale(2, RoundingMode.HALF_UP));
        denomination.setDenominationId(1);
        return denomination;
    }

    private Denomination create20Denomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationType(createNoteDenominationType());
        denomination.setDenominationValue(BigDecimal.valueOf(20.00));
        denomination.setDenominationId(2);
        return denomination;
    }

    private Denomination create50Denomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationType(createNoteDenominationType());
        denomination.setDenominationValue(BigDecimal.valueOf(50.00));
        denomination.setDenominationId(3);
        return denomination;
    }

    private Denomination create5Denomination() {
        final Denomination denomination = new Denomination();
        denomination.setDenominationType(createCoinDenominationType());
        denomination.setDenominationValue(BigDecimal.valueOf(5.00));
        denomination.setDenominationId(10);
        return denomination;
    }

    private DenominationType createNoteDenominationType() {
        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("N");
        denominationType.setDescription("Note");
        return denominationType;
    }

    private DenominationType createCoinDenominationType() {
        final DenominationType denominationType = new DenominationType();
        denominationType.setDenominationTypeCode("C");
        denominationType.setDescription("Coin");
        return denominationType;
    }
}
