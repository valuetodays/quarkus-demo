package cn.valuetodays.api2.module.fortune.channel.huabao_xinyong;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.valuetodays.api2.module.fortune.channel.BankSecurityTradeParser;
import cn.valuetodays.api2.module.fortune.enums.BankSecuritiesTradeEnums;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.text.FieldAndTile;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-10-14
 */
public class HuabaoXinyongBankSecurityTradeParser extends BankSecurityTradeParser {
    @Override
    public boolean isSupported(String text) {
        String tileLine = "转帐日期 转帐时间 银行代码  银行名称 转帐金额 银行余额 币种 状态说明   银行流水号   摘要";
        // this text aims to distinguish huabao or huabaoXinyong
        String xinyongBankText = "信用交行";
        String[] titleColumnName = StringUtils.split(tileLine + " " + xinyongBankText, " ");
        return Arrays.stream(titleColumnName).distinct().allMatch(e -> StringUtils.contains(text, e));
    }

    @Override
    public FortuneCommonEnums.Channel channel() {
        return FortuneCommonEnums.Channel.HUABAO_XIN;
    }

    @Override
    public Map<String, BankSecuritiesTradeEnums.Direction> getDirectionMap() {
        Map<String, BankSecuritiesTradeEnums.Direction> map = new HashMap<>();
        map.put("银行转证券", BankSecuritiesTradeEnums.Direction.B2S);
        map.put("证券转银行", BankSecuritiesTradeEnums.Direction.S2B);
        return map;
    }

    @Override
    public List<FieldAndTile> getFieldAndTitleList() {
        return Arrays.asList(
            FieldAndTile.of("direction", "摘要"),
            FieldAndTile.of("money", "转帐金额"),
            FieldAndTile.of("status", "状态说明"),
            FieldAndTile.of("bankTradeId", "银行流水号"),
            FieldAndTile.of("time", "转帐时间"),
            FieldAndTile.of("date", "转帐日期")
        );
    }

    @Override
    public boolean isTitleLine(String line) {
        return line.contains("银行代码") && line.startsWith("转帐日期");
    }

    @Override
    public String successTradeText() {
        return "交易成功";
    }
}
