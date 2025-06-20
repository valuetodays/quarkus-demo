package cn.valuetodays.api2.module.fortune.controller;

import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.enums.StockSubjectEnums;
import cn.valuetodays.api2.module.fortune.persist.StockPO;
import cn.valuetodays.api2.module.fortune.persist.StockSubjectPO;
import cn.valuetodays.api2.module.fortune.reqresp.EtfsRealtimeEtfsQuoteReq;
import cn.valuetodays.api2.module.fortune.reqresp.EtfsRealtimeEtfsQuoteResp;
import cn.valuetodays.api2.module.fortune.reqresp.StockReaRltimeCompareReq;
import cn.valuetodays.api2.module.fortune.reqresp.StockRealtimeCompareResp;
import cn.valuetodays.api2.module.fortune.service.StockMinutePriceServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockSubjectServiceImpl;
import cn.valuetodays.api2.module.fortune.service.module.StockRealtimeCompareComponent;
import cn.valuetodays.quarkus.commons.base.PageQueryReqIO;
import cn.vt.rest.third.eastmoney.EastMoneyStockModule;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailDataTyped;
import cn.vt.rest.third.eastmoney.vo.StockDetailVO;
import cn.vt.rest.third.xueqiu.StockRealtimeQuoteComponent;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteData;
import cn.vt.web.RestPageImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author lei.liu
 * @since 2023-05-05 11:48
 */
@Path("/stockRealtime")
public class StockRealtimeController {

    @Inject
    private StockServiceImpl stockService;
    @Inject
    private StockMinutePriceServiceImpl stockMinutePriceService;
    @Inject
    private StockSubjectServiceImpl stockSubjectService;

    @Path("/realtimeEtfsQuote.do")
    @POST
    public List<EtfsRealtimeEtfsQuoteResp> realtimeEtfsQuote(EtfsRealtimeEtfsQuoteReq req) {
        List<XueQiuStockRealtimeQuoteData> list = StockRealtimeQuoteComponent.doGet(req.getCodes());
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream().map(e -> {
            EtfsRealtimeEtfsQuoteResp resp = new EtfsRealtimeEtfsQuoteResp();
            resp.setCode(e.getSymbol());
            resp.setCurrent(e.getCurrent());
            resp.setPercent(e.getPercent());
            return resp;
        }).toList();
    }


    @Path("/stockRealtimeCompare.do")
    @POST
    public StockRealtimeCompareResp stockRealtimeCompare(StockReaRltimeCompareReq req) {
        // 选定交易对象后，我会关注两者之间涨幅差，
        // 涨幅差距拉大到1%以上开始启动切换，从涨幅高的向低的切换。
        // 每次涨幅差距拉大到更大幅度就增加一次切换，涨幅差距缩小差价盈利超过1%以上的换回来，
        // 比如：今天早上招行涨1.2%的时候宁波银行还是绿的，我就卖出2000股招行换成宁波银行，
        // 后来宁波涨幅超过招行了我再卖出宁波银行，换回等量的招行。
        // 所以，我只需要关注涨幅差就可以了，并不关注股票本身的涨幅
//        作者：ice_招行谷子地
//        链接：https://xueqiu.com/1821992043/199200699
        return StockRealtimeCompareComponent.doCompare(req);
    }

    @Path(value = "suggest.do")
    @POST
    public RestPageImpl<StockDetailVO> suggest(PageQueryReqIO pageQueryReqIO) {
        List<StockSubjectPO> subjects = stockSubjectService.findAllByType(StockSubjectEnums.Type.IN_OUT_STRATEGY);
        List<String> codes = subjects.stream().map(StockSubjectPO::getCode).toList();
        List<StockPO> stockListToUse = stockService.findAllByCodes(codes);

        List<StockDetailVO> list = stockListToUse.stream()
            .filter(Objects::nonNull)
            .map(e -> EastMoneyStockModule.getStockDetail(e.getCode()))
            .map(EastMoneyStockDetailDataTyped::toStockDetailVO)
            .peek(StockDetailVO::computeSuggest)
            .toList();
        RestPageImpl<StockDetailVO> restPageImpl = new RestPageImpl<>();
        restPageImpl.setContent(list);
        restPageImpl.setPage(0);
        restPageImpl.setSize(list.size());
        restPageImpl.setTotal(list.size());
        restPageImpl.setTotalElements(list.size());
        restPageImpl.setTotalPages(1);
        return restPageImpl;
    }

    /**
     * 偏离率.
     *
     * @return 返回的数除以100.0即是最终结果
     */
    @Path(value = "biasPercentage.do")
    @POST
    public Integer biasPercentage(String codeWithRegion) {
        return stockMinutePriceService.biasPercentage(codeWithRegion);
    }

}
