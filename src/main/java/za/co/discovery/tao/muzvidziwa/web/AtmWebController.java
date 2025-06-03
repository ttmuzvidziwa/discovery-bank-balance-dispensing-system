package za.co.discovery.tao.muzvidziwa.web;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import za.co.discovery.tao.muzvidziwa.domain.model.entity.Atm;
import za.co.discovery.tao.muzvidziwa.domain.model.response.AtmResponse;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ClientDto;
import za.co.discovery.tao.muzvidziwa.domain.model.response.dto.ResultDto;
import za.co.discovery.tao.muzvidziwa.domain.util.GeneralUtils;
import za.co.discovery.tao.muzvidziwa.repository.AtmRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AtmWebController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtmRepository atmRepository;

    @GetMapping("/transactionalBalances")
    public String showBalances(@RequestParam(value = "clientId", required = false) Integer clientId, Model model) {
        if (clientId != null) {
            try {
                String apiUrl = "http://localhost:8080/discovery-atm/queryTransactionalBalances?clientId=" + clientId;
                AtmResponse response = restTemplate.getForObject(apiUrl, AtmResponse.class);
                if (response != null && response.getAccounts() != null && !response.getAccounts().isEmpty()) {
                    model.addAttribute("client", response.getClient());
                    model.addAttribute("accounts", response.getAccounts());
                } else {
                    final ClientDto clientDto = new ClientDto();
                    clientDto.setId(GeneralUtils.parseIntToLong(clientId));

                    final ResultDto resultDto = new ResultDto();
                    resultDto.setStatusReason("No accounts to display");

                    model.addAttribute("client", clientDto);
                    model.addAttribute("accounts", "no-data");
                    model.addAttribute("result", resultDto);
                }
            } catch (Exception e) {
                model.addAttribute("accounts", null);
            }
        }
        return "transactional-balances";
    }

    @GetMapping("/forexBalances")
    public String showForexBalances(@RequestParam(value = "clientId", required = false) Integer clientId, Model model) {
        if (clientId != null) {
            try {
                String apiUrl = "http://localhost:8080/discovery-atm/queryCcyBalances?clientId=" + clientId;
                AtmResponse response = restTemplate.getForObject(apiUrl, AtmResponse.class);
                if (response != null && response.getAccounts() != null && !response.getAccounts().isEmpty()) {
                    model.addAttribute("client", response.getClient());
                    model.addAttribute("accounts", response.getAccounts());
                } else {
                    final ClientDto clientDto = new ClientDto();
                    clientDto.setId(GeneralUtils.parseIntToLong(clientId));

                    final ResultDto resultDto = new ResultDto();
                    resultDto.setStatusReason("No accounts to display");

                    model.addAttribute("client", clientDto);
                    model.addAttribute("accounts", "no-data");
                    model.addAttribute("result", resultDto);
                }
            } catch (Exception e) {
                model.addAttribute("accounts", null);
            }
        }
        return "forex-balances";
    }

    @GetMapping("/clientLanding")
    public String showClientLandingPage(@RequestParam(value = "clientId", required = false) Integer clientId, Model model) {
        if (clientId != null) {
            try {
                String apiUrl = "http://localhost:8080/discovery-atm/queryTransactionalBalances?clientId=" + clientId;
                AtmResponse response = restTemplate.getForObject(apiUrl, AtmResponse.class);
                if (response != null && response.getAccounts() != null && !response.getAccounts().isEmpty()) {
                    model.addAttribute("client", response.getClient());
                    model.addAttribute("accounts", response.getAccounts());
                } else {
                    model.addAttribute("accounts", null);
                }
            } catch (Exception e) {
                model.addAttribute("accounts", null);
            }
        }
        return "client-landing";
    }

    @GetMapping("/withdrawalAccount")
    public String showWithdrawalAccountPage(@RequestParam(value = "clientId", required = false) Integer clientId, Model model) {
        if (clientId != null) {
            try {
                String apiUrl = "http://localhost:8080/discovery-atm/queryTransactionalBalances?clientId=" + clientId;
                AtmResponse response = restTemplate.getForObject(apiUrl, AtmResponse.class);
                if (response != null && response.getAccounts() != null && !response.getAccounts().isEmpty()) {
                    model.addAttribute("client", response.getClient());
                    model.addAttribute("accounts", response.getAccounts());
                } else {
                    model.addAttribute("accounts", null);
                }
            } catch (Exception e) {
                model.addAttribute("accounts", null);
            }
        }
        return "withdrawal-account";
    }

    @PostMapping("/withdrawal")
    public String postWithdrawal(@RequestParam(value = "clientId") Integer clientId,
                                 @RequestParam(value = "accountNumber") String accountNumber,
                                 @RequestParam(value = "accountType") String accountType,
                                 Model model) {
        if (clientId != null && accountNumber != null) {
            // This method passes the clientId and accountNumber to the withdrawal page so that the client can enter the amount to withdraw.
            try {
                model.addAttribute("clientId", clientId);
                model.addAttribute("accountNumber", accountNumber);
                model.addAttribute("accountType", accountType);
                // Fetch the list of ATMs to display on the withdrawal page
                final Optional<List<Atm>> atms = atmRepository.findAllAtms();
                if (atms.isPresent()) {
                    model.addAttribute("atms", atms.get());
                } else {
                    model.addAttribute("atms", null);
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error preparing withdrawal page: " + e.getMessage());
            }
        }
        return "withdrawal";
    }

    @PostMapping("/processWithdrawal")
    public String processWithdrawal(@RequestParam(value = "clientId") Integer clientId,
                                    @RequestParam(value = "atmId") Integer atmId,
                                    @RequestParam(value = "accountNumber") String accountNumber,
                                    @RequestParam(value = "requiredAmount") String requiredAmount,
                                    Model model) {
        // This method processes the withdrawal request and returns the result to the user.
        try {
            String apiUrl = "http://localhost:8080/discovery-atm/withdraw?clientId=" + clientId +
                    "&atmId=" + atmId +
                    "&accountNumber=" + accountNumber +
                    "&requiredAmount=" + requiredAmount;
            AtmResponse response = restTemplate.postForObject(apiUrl, null, AtmResponse.class);
            if (response != null && response.getResult() != null) {
                model.addAttribute("client", response.getClient());
                model.addAttribute("account", response.getAccount());
                model.addAttribute("result", response.getResult());
            } else {
                model.addAttribute("result", null);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error processing withdrawal: " + e.getMessage());
        }
        return "withdrawal-result";
    }

    @GetMapping("/endSession")
    public String endSession(HttpSession session) {
        session.invalidate();
        return "redirect:/"; // or your landing page
    }
}
