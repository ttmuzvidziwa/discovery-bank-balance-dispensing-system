package za.co.discovery.tao.muzvidziwa.domain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Balance and Dispensing System API")
                        .version("1.0.1")
                        .description("""
                                This application is a Spring Boot-based REST API for Discovery Bank's ATM system. It allows ATM terminals to:
                                
                                 - Retrieve real-time transactional account balances for clients.
                                 - Retrieve forex account balances converted to ZAR.
                                 - Perform cash withdrawals, calculating note denominations based on ATM allocations.
                                 - Handle edge cases such as insufficient funds or unfunded ATMs.
                                
                                 It uses H2 Database, supports overdraft facilities, and provides detailed API documentation via OpenAPI/Swagger. The API is designed for integration with ATM terminals, ensuring secure and efficient banking operations.
                                
                                """)
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Tao Muzvidziwa")
                                .url("www.github.com/ttmuzvidziwa")
                                .email("taongashe.muzvidziwa@gmail.com"))
                );
    }
}
