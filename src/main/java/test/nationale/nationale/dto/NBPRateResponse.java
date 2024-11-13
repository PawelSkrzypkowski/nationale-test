package test.nationale.nationale.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NBPRateResponse {
    private String table;
    private String currency;
    private String code;
    private List<Rate> rates;

    @Data
    @NoArgsConstructor
    public static class Rate {
        private String no;
        private String effectiveDate;
        private String mid;
    }
}
