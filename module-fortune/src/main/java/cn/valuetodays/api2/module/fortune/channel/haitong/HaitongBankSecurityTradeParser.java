package cn.valuetodays.api2.module.fortune.channel.haitong;

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
 * @since 2024-08-19
 */
public class HaitongBankSecurityTradeParser extends BankSecurityTradeParser {
    @Override
    public boolean isSupported(String text) {
        List<String> mustExists = List.of("银行代码", "银行名称", "业务名称", "转帐金额", "摘要", "银行流水号",
            "存管交行人民币");
        return mustExists.stream().allMatch(e -> StringUtils.contains(text, e));
    }

    @Override
    public FortuneCommonEnums.Channel channel() {
        return FortuneCommonEnums.Channel.HAITONG;
    }

    @Override
    public Map<String, BankSecuritiesTradeEnums.Direction> getDirectionMap() {
        Map<String, BankSecuritiesTradeEnums.Direction> map = new HashMap<>();
        map.put("转入", BankSecuritiesTradeEnums.Direction.B2S);
        map.put("转出", BankSecuritiesTradeEnums.Direction.S2B);
        return map;
    }

    @Override
    public List<FieldAndTile> getFieldAndTitleList() {
        return Arrays.asList(
            FieldAndTile.of("direction", "业务名称"),
            FieldAndTile.of("money", "转帐金额"),
            FieldAndTile.of("status", "摘要"),
            FieldAndTile.of("bankTradeId", "银行流水号"),
            FieldAndTile.of("time", "转帐时间"),
            FieldAndTile.of("date", "转帐日期")
        );
    }

    @Override
    public boolean isTitleLine(String line) {
        return line.contains("银行名称") && line.startsWith("银行代码");
    }

    @Override
    public String successTradeText() {
        return "[BANK]交易成功";
    }
}
