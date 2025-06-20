package cn.valuetodays.api2.module.fortune.channel;


import java.util.ArrayList;
import java.util.List;

import cn.valuetodays.api2.module.fortune.channel.haitong.HaitongBankSecurityTradeParser;
import cn.valuetodays.api2.module.fortune.channel.huabao_xinyong.HuabaoXinyongBankSecurityTradeParser;


/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-18
 */
public final class BankSecurityTradeParserFactory {
    private BankSecurityTradeParserFactory() {
    }

    public static BankSecurityTradeParser choose(String text) {
        List<BankSecurityTradeParser> list = new ArrayList<>();
        list.add(new HaitongBankSecurityTradeParser());
        list.add(new HuabaoXinyongBankSecurityTradeParser());
        for (BankSecurityTradeParser p : list) {
            if (p.isSupported(text)) {
                return p;
            }
        }
        return null;
    }
}
