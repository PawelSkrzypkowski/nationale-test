package test.nationale.nationale.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import test.nationale.nationale.dto.CreateUserAccountRequest;
import test.nationale.nationale.dto.ExchangeRequest;
import test.nationale.nationale.service.AccountOperationsService;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class AccountOperationsController {
    private final AccountOperationsService service;

    @PostMapping(value = "/create")
    public String createUserAccount(@RequestBody @Valid CreateUserAccountRequest createUserAccountRequest) {
        return service.createAccount(createUserAccountRequest);
    }
    @PostMapping(value = "/exchange")
    public String exchange(@RequestBody @Valid ExchangeRequest exchangeRequest) {
        return service.exchange(exchangeRequest);
    }
    @GetMapping(value = "/balance/{accIdentifier}")
    public Map<String, String> getBalance(@PathVariable String accIdentifier) {
        return service.getBalance(accIdentifier);
    }
}
