package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.util.Date;
import java.util.List;

import cn.vt.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 指数历史k线数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CpiResp extends EastmoneyBaseObjResp<CpiResp.CpiDataObj> {

    @Data
    public static class CpiDataObj {
        private int count;
        private int pages;
        private List<ValueObj> data;

        @Data
        public static class ValueObj {
            @JsonProperty("REPORT_DATE")
            @JsonFormat(pattern = DateUtils.DEFAULT_DATETIME_FORMAT)
            private Date reportDate;
            @JsonProperty("CITY_ACCUMULATE")
            private Double cityAccumulate;
            @JsonProperty("CITY_BASE")
            private Double cityBase;
            @JsonProperty("NATIONAL_ACCUMULATE")
            private Double nationalAccumulate;
            @JsonProperty("NATIONAL_BASE")
            private Double nationalBase;
            @JsonProperty("RURAL_ACCUMULATE")
            private Double ruralAccumulate;
            @JsonProperty("RURAL_BASE")
            private Double ruralBase;
        }
    }

}
