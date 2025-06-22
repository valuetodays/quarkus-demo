package cn.valuetodays.api2.module.fortune.channel;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.text.FieldAndTile;
import cn.vt.text.TitleAndBodyTextParser;
import cn.vt.util.DateUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-07
 */
public abstract class StockTradeLogParser {

    public abstract boolean isSupported(String text);

    public abstract FortuneCommonEnums.Channel channel();

    public abstract Map<String, FortuneCommonEnums.TradeType> getTradeTypeMap();

    public abstract List<FieldAndTile> getFieldAndTitleList();

    public abstract boolean isTitleLine(String line);

    public final List<StockTradePO> parse(String text,
                                          final List<String> excludedCodes,
                                          final List<String> excludedTypeCns) {
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
        List<String> excludedCodesToUse = ObjectUtils.defaultIfNull(excludedCodes, List.of());
        List<String> excludedTypeCnsToUse = ObjectUtils.defaultIfNull(excludedTypeCns, List.of());
        return maps.stream()
            .filter(e -> !excludedCodesToUse.contains(e.get("code")))
            .filter(e -> !excludedTypeCnsToUse.contains(e.get("typeCn")))
            .map(e -> {
                StockTradePO po = new StockTradePO();
                po.setCode(e.get("code"));
                po.setChannel(channel);
                po.setTradeType(toTradeTypeCode(e.get("typeCn")));
                po.setTradeDate(Integer.valueOf(e.get("trade_date")));
                String timeString = e.get("trade_time");
                if (StringUtils.isBlank(timeString)) {
                    timeString = "00:00:00";
                }
                po.setTradeTime(
                    LocalTime.parse(timeString, DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT))
                );
                po.setQuantity(Integer.parseInt(e.get("quantity")));
                po.setPrice(new BigDecimal(e.get("price")));
                po.setYongjinFee(new BigDecimal(e.get("yj")));
                po.setYinhuaFee(new BigDecimal(e.get("yh")));
                po.setGuohuFee(new BigDecimal(e.get("gh")));
                // 如果是红利，则订单编辑是【红利清算】或【红利】，此时需要调整
                if (po.getTradeType() == FortuneCommonEnums.TradeType.PASSIVE_SELL) {
                    po.setChannelOrderId("H" + po.getTradeDate()
                        + "-" + po.getCode() + "@" + po.getPrice() + "*" + po.getQuantity());
                } else {
                    po.setChannelOrderId(e.get("order_id"));
                }
                po.setHedgeId(0L);
                po.setId(null);
                return po;
            }).toList();
    }

    private FortuneCommonEnums.TradeType toTradeTypeCode(String tradeTypeString) {
        return getTradeTypeMap().get(tradeTypeString);
    }

}
