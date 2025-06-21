package cn.valuetodays.api2.web.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.service.EtfInfoServiceImpl;
import cn.vt.test.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-19 22:38
 */
class EtfInfoServiceImplMainIT extends TestBase {

    @Test
    public void formatATag() {
        String str = "<a href=\"//fund.eastmoney.com/manager/30725627.html\">田光远</a>";
        String result = EtfInfoServiceImpl.formatATag(str);
        Assertions.assertEquals("田光远", result);
        getLogger().info("result: [{}]", result);
    }

    @Test
    public void formatNumberRadio() {
        String str = "0.50%（每年）";
        String result = EtfInfoServiceImpl.formatNumberRadio(str);
        Assertions.assertEquals("0.50", result);
        getLogger().info("result: [{}]", result);
    }

    @Test
    public void formatNumberRadio2() {
        String str = "---（每年）";
        String result = EtfInfoServiceImpl.formatNumberRadio(str);
        Assertions.assertEquals("0", result);
        getLogger().info("result: [{}]", result);
    }

    @Test
    public void formatScale() {
        String str = "21.05亿元（截止至：2023年03月31日）";
        String result = EtfInfoServiceImpl.formatScale(str);
        Assertions.assertEquals("21.05", result);
        getLogger().info("result: [{}]", result);
    }

    @Test
    public void replaceHanziInDateStr() {
        String str = "2021年03月03日";
        String result = EtfInfoServiceImpl.replaceHanziInDateStr(str);
        Assertions.assertEquals("2021-03-03", result);
        getLogger().info("result: [{}]", result);
    }
}
