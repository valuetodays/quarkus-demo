package cn.valuetodays.trade;

import java.util.Map;
import java.util.Properties;

import cn.vt.test.TestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-09
 */
@Slf4j
public class MainTradeTest extends TestBase {

    @Test
    @Disabled
    public void doTrade() {
        Map<String, String> env = System.getenv();
//        getLogger().info("env: {}", env);
        Properties properties = System.getProperties();
//        getLogger().info("properties: {}", properties);

        BaseHttpAutoTrade httpAutoTrade = new HaitongHttpAutoTrade();
        httpAutoTrade.doAutoTrade();
    }

}
