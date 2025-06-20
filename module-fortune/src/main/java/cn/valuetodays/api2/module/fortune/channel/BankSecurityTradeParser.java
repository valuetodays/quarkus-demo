package cn.valuetodays.api2.module.fortune.channel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.enums.BankSecuritiesTradeEnums;
import cn.valuetodays.api2.module.fortune.persist.BankSecuritiesTradePersist;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.text.FieldAndTile;
import cn.vt.text.TitleAndBodyTextParser;
import cn.vt.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-19
 */
public abstract class BankSecurityTradeParser {
    public abstract boolean isSupported(String text);

    public abstract FortuneCommonEnums.Channel channel();

    public abstract Map<String, BankSecuritiesTradeEnums.Direction> getDirectionMap();

    public abstract List<FieldAndTile> getFieldAndTitleList();

    public abstract boolean isTitleLine(String line);

    public abstract String successTradeText();

    public final List<BankSecuritiesTradePersist> parse(String text) {
        final String[] lines = StringUtils.split(text, "\r\n");
        String headerLine = null;
        final List<String> bodyLines = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (isTitleLine(line)) {
                headerLine = line;
                continue;
            }
            if (Objects.nonNull(headerLine)) {
                bodyLines.add(line);
            }
        }

        List<Map<String, String>> maps = TitleAndBodyTextParser.parseAsMap(
            headerLine, bodyLines, " ", getFieldAndTitleList());

        FortuneCommonEnums.Channel channel = channel();
        final Map<String, BankSecuritiesTradeEnums.Direction> directionMap = getDirectionMap();
        final String successTradeText = successTradeText();

        return maps.stream()
            .map(e -> {
                String status = e.get("status");
                if (!StringUtils.equals(successTradeText, status)) {
                    return null;
                }
                String dateString = e.get("date"); // 20240326
                String timeString = e.get("time"); // 09:29:34
                String dateFormat = DateUtils.DEFAULT_DATE_FORMAT.replace("-", "");
                String dateTimeFormat = dateFormat + "-" + DateUtils.DEFAULT_TIME_FORMAT;
                LocalDateTime ldt = DateUtils.getDate(dateString + "-" + timeString, dateTimeFormat);

                BankSecuritiesTradePersist po = new BankSecuritiesTradePersist();
                po.setId(null);
                po.setChannel(channel);
                po.setOperateTime(ldt);
                po.setDirection(directionMap.get(e.get("direction")));
                po.setMoney(new BigDecimal(e.get("money")));
                return po;
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
