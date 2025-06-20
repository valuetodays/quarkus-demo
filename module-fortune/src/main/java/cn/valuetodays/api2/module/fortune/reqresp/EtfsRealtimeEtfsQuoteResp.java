package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
public class EtfsRealtimeEtfsQuoteResp implements Serializable {
    @Schema(description = "股票代号", example = "SH510310")
    private String code;
    @Schema(description = "当前股价", example = "1.888")
    private Double current;
    @Schema(description = "涨跌百分比，不需要再除以100", example = "-0.21")
    private Double percent;
}
