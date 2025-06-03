package za.co.discovery.tao.muzvidziwa.api.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import za.co.discovery.tao.muzvidziwa.domain.exception.BankServiceException;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;
import za.co.discovery.tao.muzvidziwa.domain.model.response.views.View;

import java.math.BigDecimal;

public interface BankController {

    @JsonView(View.Transactional.class)
    @Operation(summary = "Get transactional balances",
            description = "Retrieves a list of transactional accounts with available balances for a specific client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactional account balances",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AtmResponse.class),
                            examples = {@ExampleObject(value = """
                                    {
                                      "client": {
                                        "id": 12345,
                                        "title": "Mr",
                                        "name": "Tao",
                                        "surname": "Muzvidziwa"
                                      },
                                      "accounts": [
                                        {
                                          "accountNumber": 1234567890,
                                          "typeCode": "CHQ",
                                          "accountTypeDescription": "Cheque Account",
                                          "currencyCode": "ZAR",
                                          "conversionRate": 1.000,
                                          "balance": 5000.00,
                                          "zarBalance": 5000.00,
                                          "accountLimit": 15000.00
                                        }
                                      ],
                                      "result": {
                                        "success": true,
                                        "statusCode": 200,
                                        "statusReason": "Success"
                                      }
                                    }
                                    """),
                                    @ExampleObject(value = """
                                                    {
                                              "client": {
                                                "id": 12345,
                                                "title": "Mr",
                                                "name": "Tao",
                                                "surname": "Muzvidziwa"
                                              },
                                              "accounts": null,
                                              "result": {
                                                "success": false,
                                                "statusCode": 400,
                                                "statusReason": "No accounts to display"
                                              }
                                            }
                                            """),
                                    @ExampleObject(value = """
                                                    {
                                              "client": null,
                                              "accounts": null,
                                              "result": {
                                                "success": false,
                                                "statusCode": 500,
                                                "statusReason": "Unspecified error occurred"
                                              }
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "400", description = "Bank service exception message",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankServiceException.class),
                            examples = @ExampleObject(value = """
                                    "Bank service exception message"
                                    """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error message",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exception.class),
                            examples = @ExampleObject(value = """
                                    "Unspecified error occurred"
                                    """)))
    })
    ResponseEntity<Object> getTransactionalBalance(@Parameter(description = "Client ID", required = true)
                                                   @RequestParam("clientId") final Integer clientId);

    @JsonView(View.Currency.class)
    @Operation(summary = "Get forex balances",
            description = "Retrieves a list of forex accounts converted to ZAR balances for a specific client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved forex balances",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AtmResponse.class),
                            examples = {@ExampleObject(value = """
                                    {
                                      "client": {
                                        "id": 12345,
                                        "title": "Mr",
                                        "name": "Tao",
                                        "surname": "Muzvidziwa"
                                      },
                                      "accounts": [
                                        {
                                          "accountNumber": 1234567890,
                                          "typeCode": "CFCA",
                                          "accountTypeDescription": "Customer Foreign Currency Account",
                                          "currencyCode": "USD",
                                          "conversionRate": 16.000,
                                          "ccyBalance": 5000.00,
                                          "zarBalance": 5000.00,
                                          "accountLimit": 80000.00
                                        }
                                      ],
                                      "result": {
                                        "success": true,
                                        "statusCode": 200,
                                        "statusReason": "Success"
                                      }
                                    }
                                    """),
                                    @ExampleObject(value = """
                                                    {
                                              "client": {
                                                "id": 12345,
                                                "title": "Mr",
                                                "name": "Tao",
                                                "surname": "Muzvidziwa"
                                              },
                                              "accounts": null,
                                              "result": {
                                                "success": false,
                                                "statusCode": 400,
                                                "statusReason": "No accounts to display"
                                              }
                                            }
                                            """),
                                    @ExampleObject(value = """
                                                    {
                                              "client": null,
                                              "accounts": null,
                                              "result": {
                                                "success": false,
                                                "statusCode": 500,
                                                "statusReason": "Unspecified error occurred"
                                              }
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "400", description = "Bank service exception message",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankServiceException.class),
                            examples = @ExampleObject(value = """
                                    "No accounts to display"
                                    """))),
            @ApiResponse(responseCode = "500", description = "Runtime exception message",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exception.class),
                            examples = @ExampleObject(value = """
                                    "Springframework DAO exception"
                                    """)))
    })
    ResponseEntity<Object> getForexAccountBalance(@Parameter(description = "Client ID", required = true)
                                                  @RequestParam("clientId") final Integer clientId);

    @JsonView(View.Withdrawal.class)
    @Operation(summary = "Post a withdrawal",
            description = """
                    Performs a cash withdrawal from a specified ATM and a given client's cheque account, dispensing the 
                    requested amount in appropriate denominations.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal successful and denominations returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AtmResponse.class),
                            examples = {@ExampleObject(value = """
                                    {
                                      "client": {
                                        "id": 12345,
                                        "title": "Mr",
                                        "name": "Tao",
                                        "surname": "Muzvidziwa"
                                      },
                                      "account": {
                                          "accountNumber": 1234567890,
                                          "typeCode": "CHQ",
                                          "accountTypeDescription": "Cheque Account",
                                          "currencyCode": "ZAR",
                                          "conversionRate": 1.000,
                                          "balance": 5000.00,
                                          "zarBalance": 5000.00,
                                          "accountLimit": 15000.00
                                      },
                                      "result": {
                                        "success": true,
                                        "statusCode": 200,
                                        "statusReason": "Withdrawal successful"
                                      },
                                      "denomination": [
                                        {
                                          "denominationId": 2,
                                          "denominationValue": 50.00,
                                          "count": 3
                                        }, 
                                        {
                                          "denominationId": 3,
                                          "denominationValue": 100.00,
                                          "count": 1
                                        }
                                      ]
                                    }
                                    """),
                                    @ExampleObject(value = """
                                            {
                                              "client": {
                                                "id": 12345,
                                                "title": "Mr",
                                                "name": "Tao",
                                                "surname": "Muzvidziwa"
                                              },
                                              "account": {},
                                              "result": {
                                                "success": true,
                                                "statusCode": 400,
                                                "statusReason": "No accounts to display"
                                              },
                                              "denomination": []
                                            }
                                            """),
                                    @ExampleObject(value = """
                                            {
                                              "client": {
                                                "id": 12345,
                                                "title": "Mr",
                                                "name": "Tao",
                                                "surname": "Muzvidziwa"
                                              },
                                              "account": {
                                                  "accountNumber": 1234567890,
                                                  "typeCode": "CHQ",
                                                  "accountTypeDescription": "Cheque Account",
                                                  "currencyCode": "ZAR",
                                                  "conversionRate": 1.000,
                                                  "balance": 5000.00,
                                                  "zarBalance": 5000.00,
                                                  "accountLimit": 15000.00
                                              },
                                              "result": {
                                                "success": true,
                                                "statusCode": 400,
                                                "statusReason": "ATM not registered or unfunded"
                                              },
                                              "denomination": []
                                            }
                                            """),
                                    @ExampleObject(value = """
                                            {
                                              "client": {
                                                "id": 12345,
                                                "title": "Mr",
                                                "name": "Tao",
                                                "surname": "Muzvidziwa"
                                              },
                                              "account": {
                                                  "accountNumber": 1234567890,
                                                  "typeCode": "CHQ",
                                                  "accountTypeDescription": "Cheque Account",
                                                  "currencyCode": "ZAR",
                                                  "conversionRate": 1.000,
                                                  "balance": 5000.00,
                                                  "zarBalance": 5000.00,
                                                  "accountLimit": 15000.00
                                              },
                                              "result": {
                                                "success": true,
                                                "statusCode": 400,
                                                "statusReason": "Amount not available. would you like to withdraw XXX"
                                              },
                                              "denomination": [}
                                            }
                                            """),
                                    @ExampleObject(value = """
                                            {
                                              "client": {
                                                "id": 12345,
                                                "title": "Mr",
                                                "name": "Tao",
                                                "surname": "Muzvidziwa"
                                              },
                                              "account": {
                                                  "accountNumber": 1234567890,
                                                  "typeCode": "CHQ",
                                                  "accountTypeDescription": "Cheque Account",
                                                  "currencyCode": "ZAR",
                                                  "conversionRate": 1.000,
                                                  "balance": 5000.00,
                                                  "zarBalance": 5000.00,
                                                  "accountLimit": 15000.00
                                              },
                                              "result": {
                                                "success": true,
                                                "statusCode": 400,
                                                "statusReason": "Insufficient funds"
                                              },
                                              "denomination": []
                                            }
                                            """)
                            })
            ),
            @ApiResponse(responseCode = "400", description = "Bank service exception message",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankServiceException.class),
                            examples = @ExampleObject(value = """
                                    Withdrawal could not be completed
                                    """))
            ),
            @ApiResponse(responseCode = "500", description = "Runtime exception mess",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exception.class),
                            examples = @ExampleObject(value = """
                                    Unspecified error occurred
                                    """)))
    })
    ResponseEntity<Object> postWithdrawal(@Parameter(description = "Client ID", required = true)
                                          @RequestParam("clientId") final Integer clientId,
                                          @Parameter(description = "ATM ID", required = true)
                                          @RequestParam("atmId") final Integer atmId,
                                          @Parameter(description = "Client account number", required = true)
                                          @RequestParam("accountNumber") final String accountNumber,
                                          @Parameter(description = "Required withdrawal amount", required = true)
                                          @RequestParam("requiredAmount") final BigDecimal requiredAmount);
}
