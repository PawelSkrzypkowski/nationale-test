package test.nationale.nationale.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import test.nationale.nationale.dto.CreateUserAccountRequest;
import test.nationale.nationale.dto.ExchangeRequest;
import test.nationale.nationale.dto.NBPRateResponse;
import test.nationale.nationale.model.UserAccountEntity;
import test.nationale.nationale.repository.UserAccountRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountOperationsService {
    private static final String USD_RATE_URL = "https://api.nbp.pl/api/exchangerates/rates/a/usd";
    private final UserAccountRepository repository;
    private final RestTemplate restTemplate;

    public String createAccount(CreateUserAccountRequest createUserAccountRequest) {
        try {
            UserAccountEntity entity = UserAccountEntity.builder()
                    .accIdentifier(UUID.randomUUID().toString())
                    .firstName(createUserAccountRequest.getFirstName())
                    .lastName(createUserAccountRequest.getLastName())
                    .plnBalance(createUserAccountRequest.getPlnBalance())
                    .usdBalance("0.00")
                    .build();
            entity = repository.save(entity);
            return entity.getAccIdentifier();
        } catch (Exception e) {
            log.error("Exception during creating user account", e);
            return "Can't create user account.";
        }
    }

    public String exchange(ExchangeRequest exchangeRequest) {
        try {
            UserAccountEntity account = repository.findFirstByAccIdentifier(exchangeRequest.getAccountIdentifier());
            if (Objects.isNull(account)) {
                throw new BadRequestException("Can't find account by provided account identifier");
            }
            BigDecimal changeAmount = new BigDecimal(exchangeRequest.getAmount());
            BigDecimal plnBalance = new BigDecimal(account.getPlnBalance());
            BigDecimal usdBalance = new BigDecimal(account.getUsdBalance());
            validateExchange(exchangeRequest, plnBalance, usdBalance, changeAmount);
            processExchange(exchangeRequest, account, plnBalance, usdBalance, changeAmount);
            return "Exchange have been performed";
        } catch (Exception e) {
            log.error("Exception during exchange", e);
            return "Can't make exchange: " + e.getMessage();
        }
    }

    public Map<String, String> getBalance(String accIdentifier) {
        try {
            UserAccountEntity account = repository.findFirstByAccIdentifier(accIdentifier);
            if (Objects.isNull(account)) {
                throw new BadRequestException("Can't find account by provided account identifier");
            }
            return Map.of("name", account.getFirstName(), "lastName", account.getLastName(),
                    "plnBalance", account.getPlnBalance(), "usdBalance", account.getUsdBalance());
        } catch (Exception e) {
            log.error("Exception during checking balance", e);
            return Map.of("error","Can't get user balance");
        }
    }

    private void validateExchange(ExchangeRequest exchangeRequest, BigDecimal plnBalance, BigDecimal usdBalance,
                                  BigDecimal changeAmount) throws BadRequestException {
        if (exchangeRequest.isPlnExchange() && plnBalance.compareTo(changeAmount) < 0) {
            throw new BadRequestException("Pln balance is smaller than " + changeAmount);
        } else if (exchangeRequest.isUsdExchange() && usdBalance.compareTo(changeAmount) < 0) {
            throw new BadRequestException("Usd balance is smaller than " + changeAmount);
        } else if (!exchangeRequest.isPlnExchange() && !exchangeRequest.isUsdExchange()) {
            throw new BadRequestException("Currency should have value pln or usd");
        }
    }

    private void processExchange(ExchangeRequest exchangeRequest, UserAccountEntity account, BigDecimal plnBalance,
                                 BigDecimal usdBalance, BigDecimal changeAmount) {
        BigDecimal rate = getRate(exchangeRequest.isPlnExchange());
        BigDecimal amountToAdd = changeAmount.multiply(rate);
        if (exchangeRequest.isPlnExchange()) {
            usdBalance = usdBalance.add(amountToAdd);
            plnBalance = plnBalance.subtract(changeAmount);
        } else {
            plnBalance = plnBalance.add(amountToAdd);
            usdBalance = usdBalance.subtract(changeAmount);
        }
        account.setPlnBalance(String.format(Locale.US, "%.2f", plnBalance));
        account.setUsdBalance(String.format(Locale.US, "%.2f", usdBalance));
        repository.save(account);
    }

    private BigDecimal getRate(boolean divide) {
        try {
            NBPRateResponse rateResponse = restTemplate.getForObject(USD_RATE_URL, NBPRateResponse.class);
            String rateString = Optional.ofNullable(rateResponse)
                    .map(NBPRateResponse::getRates)
                    .map(List::getFirst)
                    .map(NBPRateResponse.Rate::getMid)
                    .orElse(null);
            if (Objects.isNull(rateString)) {
                throw new BadRequestException("Can't find latest usd rate");
            }
            BigDecimal bigDecimal = new BigDecimal(rateString);
            if (bigDecimal.compareTo(BigDecimal.ZERO) < 1) {
                throw new RuntimeException("Found rate is not positive " + rateString);
            }
            return divide ? BigDecimal.ONE.divide(bigDecimal, 4, RoundingMode.HALF_UP) : bigDecimal;
        } catch (Exception e) {
            throw new RuntimeException("Can't get latest usd rate", e);
        }
    }
}
