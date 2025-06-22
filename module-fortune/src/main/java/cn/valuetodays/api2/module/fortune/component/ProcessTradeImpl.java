package cn.valuetodays.api2.module.fortune.component;

import java.math.BigDecimal;

import cn.valuetodays.api2.module.fortune.client.QmtConstants;
import cn.valuetodays.api2.module.fortune.client.reqresp.OrderInfo;
import cn.valuetodays.api2.module.fortune.client.util.PriceUtilsEx;
import cn.valuetodays.api2.web.module.fortune.IProcessTrade;
import cn.vt.rest.third.utils.StockCodeUtils;
import cn.vt.util.JsonUtils;
import io.smallrye.mutiny.tuples.Tuple4;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-22
 */
@ApplicationScoped
@Slf4j
public class ProcessTradeImpl implements IProcessTrade {
    @Inject
    QmtComponent qmtComponent;

    private static Tuple4<QmtConstants.TradeType, String, Integer, QmtConstants.TradeType> order_type_to_str(Integer order_type) {
        if (QmtConstants.STOCK_BUY == order_type) {
            return Tuple4.of(QmtConstants.TradeType.BUY, "买入", QmtConstants.STOCK_SELL, QmtConstants.TradeType.SELL);

        } else if (QmtConstants.STOCK_SELL == order_type) {
            return Tuple4.of(QmtConstants.TradeType.SELL, "卖出", QmtConstants.STOCK_BUY, QmtConstants.TradeType.BUY);
        }
        return null;
    }

    @Override
    public void processTrade(String tradeMsg) {
        OrderInfo orderInfo = JsonUtils.toObject(tradeMsg, OrderInfo.class);
        try {
            Integer orderType = orderInfo.getOrder_type();
            Tuple4<QmtConstants.TradeType, String, Integer, QmtConstants.TradeType> t4 = order_type_to_str(orderType.intValue());
            QmtConstants.TradeType order_type_enum = t4.getItem1();
            String im_text = "you " + order_type_enum.name() + " " + orderInfo.getTraded_volume() + " shares of "
                + StockCodeUtils.convert_code_for_market_code(orderInfo.getStock_code()) + " at "
                + PriceUtilsEx.convertPriceDown(orderInfo.getTraded_price(), BigDecimal.ZERO) + " yuan"
                + ", order_id=" + orderInfo.getOrder_sysid();
            // tdoo push to vocechat
//            PushVocechatTextReq req = new PushVocechatTextReq();
//            req.setToGroupId(1);
//            req.setPlainText(true);
//            req.setContent(im_text);
//            vocechatService.pushVocechatText(req);
            // 交易成功的通知，推送到im中
        } catch (Exception e) {
            log.warn(" error", e);
        }

        try {
            qmtComponent.markAsTraded(orderInfo);
        } catch (Exception e) {
            log.warn(" error markAsTraded()", e);
        }
    }
}
