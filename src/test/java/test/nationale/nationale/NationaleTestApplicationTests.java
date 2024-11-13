package test.nationale.nationale;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import test.nationale.nationale.dto.CreateUserAccountRequest;
import test.nationale.nationale.dto.ExchangeRequest;

import java.util.Map;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class NationaleTestApplicationTests {
    @Value("${server.port}")
    private String port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }

    @Test
    void contextLoads() {

    }
    @Test
    void shouldNotCreateAccount() {
        //given
        CreateUserAccountRequest firstCreation = CreateUserAccountRequest.builder()
                .firstName("te")
                .lastName("xc")
                .plnBalance("abc")
                .build();
        CreateUserAccountRequest secondCreation = CreateUserAccountRequest.builder()
                .firstName("te")
                .lastName("xc")
                .plnBalance("-101.24")
                .build();
        CreateUserAccountRequest thirdCreation = CreateUserAccountRequest.builder()
                .firstName("test")
                .lastName("test")
                .plnBalance("-101.24")
                .build();

        //when
        String firstResponse = restTemplate.postForObject("http://localhost:" + port + "/create", firstCreation, String.class);
        String secondResponse = restTemplate.postForObject("http://localhost:" + port + "/create", secondCreation, String.class);
        String thirdResponse = restTemplate.postForObject("http://localhost:" + port + "/create", thirdCreation, String.class);

        //then
        Assertions.assertTrue(firstResponse.contains("firstName") && firstResponse.contains("lastName") &&
                firstResponse.contains("plnBalance"));
        Assertions.assertTrue(secondResponse.contains("firstName") && secondResponse.contains("lastName") &&
                secondResponse.contains("plnBalance"));
        Assertions.assertTrue(thirdResponse.contains("plnBalance"));
    }
    @Test
    void shouldCreateAccountAndMakeTwoSuccessOperationsAndTwoFailed() {
        //given
        CreateUserAccountRequest creation = CreateUserAccountRequest.builder()
                .firstName("test")
                .lastName("test")
                .plnBalance("125.00")
                .build();
        String accId = restTemplate.postForObject("http://localhost:" + port + "/create", creation, String.class);
        ExchangeRequest request1 = ExchangeRequest.builder()
                .accountIdentifier(accId)
                .currency("fail")
                .amount("10.00")
                .build();
        ExchangeRequest request2 = ExchangeRequest.builder()
                .accountIdentifier(accId)
                .currency("pln")
                .amount("125.10")
                .build();
        ExchangeRequest request3 = ExchangeRequest.builder()
                .accountIdentifier(accId)
                .currency("pln")
                .amount("60.50")
                .build();
        ExchangeRequest request4 = ExchangeRequest.builder()
                .accountIdentifier(accId)
                .currency("usd")
                .amount("10.10")
                .build();

        //when
        Map<String, String> firstBalance = restTemplate.getForObject("http://localhost:" + port + "/balance/" + accId, Map.class);
        String firstResponse = restTemplate.postForObject("http://localhost:" + port + "/exchange", request1, String.class);
        String secondResponse = restTemplate.postForObject("http://localhost:" + port + "/exchange", request2, String.class);
        Map<String, String> secondBalance = restTemplate.getForObject("http://localhost:" + port + "/balance/" + accId, Map.class);
        String thirdResponse = restTemplate.postForObject("http://localhost:" + port + "/exchange", request3, String.class);
        Map<String, String> thirdBalance = restTemplate.getForObject("http://localhost:" + port + "/balance/" + accId, Map.class);
        String fourthResponse = restTemplate.postForObject("http://localhost:" + port + "/exchange", request4, String.class);
        Map<String, String> fourthBalance = restTemplate.getForObject("http://localhost:" + port + "/balance/" + accId, Map.class);

        //then
        Assertions.assertEquals("125.00", firstBalance.get("plnBalance"));
        Assertions.assertEquals("0.00", firstBalance.get("usdBalance"));
        log.info("Start balance: {}", firstBalance);
        Assertions.assertEquals("Can't make exchange: Currency should have value pln or usd", firstResponse);
        Assertions.assertEquals("Can't make exchange: Pln balance is smaller than 125.10", secondResponse);
        Assertions.assertEquals("125.00", secondBalance.get("plnBalance"));
        Assertions.assertEquals("0.00", secondBalance.get("usdBalance"));
        Assertions.assertEquals("Exchange have been performed", thirdResponse);
        Assertions.assertEquals("64.50", thirdBalance.get("plnBalance"));
        Assertions.assertTrue(Double.parseDouble(thirdBalance.get("usdBalance")) > 11);
        log.info("After first success operation balance: {}", thirdBalance);
        Assertions.assertEquals("Exchange have been performed", fourthResponse);
        Assertions.assertTrue(Double.parseDouble(fourthBalance.get("plnBalance")) > 90);
        Assertions.assertTrue(Double.parseDouble(fourthBalance.get("usdBalance")) > 1 && Double.parseDouble(fourthBalance.get("usdBalance")) < 10);
        log.info("After second success operation balance: {}", fourthBalance);
    }

}
