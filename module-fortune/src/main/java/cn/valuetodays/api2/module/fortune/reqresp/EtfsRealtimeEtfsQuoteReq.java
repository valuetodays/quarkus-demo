package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class EtfsRealtimeEtfsQuoteReq implements Serializable {
    private List<String> codes;
}
