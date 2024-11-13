package test.nationale.nationale.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserAccountRequest {
    @Size(min = 3)
    @Size(max = 30)
    @JsonProperty(required = true)
    private String firstName;
    @Size(min = 3)
    @Size(max = 30)
    @JsonProperty(required = true)
    private String lastName;
    @Positive
    @Pattern(regexp = "\\d+.\\d{2}", message = "PLN balance should be formatted x{1,}.xx, example 1014.23")
    @JsonProperty(required = true)
    private String plnBalance;
}
