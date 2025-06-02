package za.co.discovery.tao.muzvidziwa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankBalanceDispensingSystem {

	public static void main(final String[] args) {
		SpringApplication.run(BankBalanceDispensingSystem.class, args);
	}

}
