package cn.valuetodays.api2.module.fortune.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import cn.vt.fortune.modestep.client.vo.HolderVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-10-18
 */
@Data
public class QmtAnalysisHoldersResp implements Serializable {
    private BigDecimal sum;
    private List<HolderVo> holders;

    @JsonIgnore
    public String buildResultMsg() {
        String infoString = this.getHolders().stream()
            .map(e -> e.getStock_code() + " -> " + e.getPercentage().doubleValue() + "%")
            .collect(Collectors.joining("\n"));
        return "\n== holders pct== \n" + infoString + "\n --- sum: " + this.getSum() + "---\n";
    }
}
