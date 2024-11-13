package test.nationale.nationale.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequest {
    private static final String PLN_CURRENCY = "pln";
    private static final String USD_CURRENCY = "usd";

    @JsonProperty(required = true)
    private String accountIdentifier;
    @JsonProperty(required = true)
    private String currency;
    @Positive
    @Pattern(regexp = "\\d+.\\d{2}", message = "Amount should be formatted x{1,}.xx, example 1014.23")
    @JsonProperty(required = true)
    private String amount;

    public boolean isPlnExchange() {
        return PLN_CURRENCY.equals(currency);
    }

    public boolean isUsdExchange() {
        return USD_CURRENCY.equals(currency);
    }
}
