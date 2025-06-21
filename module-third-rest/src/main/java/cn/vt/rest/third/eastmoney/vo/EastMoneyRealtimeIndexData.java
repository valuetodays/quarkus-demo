package cn.vt.rest.third.eastmoney.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-29
 */
@Data
public class EastMoneyRealtimeIndexData {
    private List<Diff> diff;

    @Data
    public static class Diff {
        @JsonProperty("f12")
        private String code;
        @JsonProperty("f14")
        private String name;
        @JsonProperty("f6")
        private String busiMoney;
    }

}
