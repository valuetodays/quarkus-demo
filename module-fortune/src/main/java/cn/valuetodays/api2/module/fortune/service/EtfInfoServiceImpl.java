package cn.valuetodays.api2.module.fortune.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.EtfInfoDAO;
import cn.valuetodays.api2.module.fortune.dao.StockDAO;
import cn.valuetodays.api2.module.fortune.persist.EtfInfoPO;
import cn.valuetodays.api2.module.fortune.persist.StockPO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.eastmoney.EastMoneyStockModule;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailDataTyped;
import cn.vt.util.ConvertUtils;
import cn.vt.util.HttpClient4Utils;
import cn.vt.util.VtObjectUtils;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * @author lei.liu
 * @since 2019-10-22 16:32
 */
@ApplicationScoped
@Slf4j
public class EtfInfoServiceImpl extends BaseCrudService<Long, EtfInfoPO, EtfInfoDAO> {
    @Inject
    StockDAO stockDAO;

    public static String formatATag(String managerStr) {
        if (!managerStr.contains("<a")) {
            return managerStr;
        }
        int pos1 = managerStr.indexOf(">");
        return managerStr.substring(pos1 + ">".length(), managerStr.indexOf("</a", pos1));
    }

    public static String formatNumberRadio(String radioStr) {
        String nullStr = "---";
        if (radioStr.contains(nullStr)) {
            return "0";
        }
        String str = "%";
        return radioStr.substring(0, radioStr.indexOf(str));
    }

    public static String formatScale(String scaleStr) {
        String str = "亿元";
        return scaleStr.substring(0, scaleStr.indexOf(str));
    }

    public static String replaceHanziInDateStr(String dateStr) {
        String tmpStr = dateStr;
        List<Pair<String, String>> pairs = Arrays.asList(
            ImmutablePair.of("年", "-"),
            ImmutablePair.of("月", "-"),
            ImmutablePair.of("日", "")
        );
        for (Pair<String, String> pair : pairs) {
            tmpStr = StringUtils.replace(tmpStr, pair.getKey(), pair.getValue());
        }
        return tmpStr;
    }

    //    @Async
    public void refresh() {

        doIgnoreException(this::step1GatherChangNeiFundList);
        doIgnoreException(this::step1GatherEtfList);
        doIgnoreException(this::step2FillLastTradeAmount);
    }

    public void step2FillLastTradeAmount() {
        int pn = 0;
        final int size = 10;
        while (true) {
            Page page = Page.of(pn, size);
            List<EtfInfoPO> content = getRepository().pageQuery(page, Sort.ascending("id"));
            if (CollectionUtils.isEmpty(content)) {
                break;
            }

            Map<String, EtfInfoPO> codeMap = content.stream().collect(Collectors.toMap(EtfInfoPO::getCode, e -> e));
            List<StockPO> stocks = stockDAO.findAllByCodeIn(new ArrayList<>(codeMap.keySet()));
            for (StockPO stock : stocks) {
                String code = stock.getCode();
                EtfInfoPO exist = codeMap.get(code);
                if (Objects.isNull(exist)) {
                    continue;
                }
                EastMoneyStockDetailDataTyped stockDetail = EastMoneyStockModule.getStockDetail(code);
                exist.setHuanShouPtg(stockDetail.getHuanShouPtg());
                LocalDateTime statDateTime = stockDetail.getStatDateTime();
            }
            getRepository().persist(content);
            pn++;
        }
    }

    private void step1GatherChangNeiFundList() {
        final String url = "http://fund.eastmoney.com/cnjy_jzzzl.html";
        String pageSource = HttpClient4Utils.doGet(url, null, Charset.forName("gb2312").name());
        Document document = Jsoup.parse(pageSource);
        Elements tableDivEle = document.select("#tableDiv");
        Elements trList = tableDivEle.select("tr");
        List<Element> etfList = trList.subList(2, trList.size() - 1);
        for (Element etf : etfList) {
            String code = etf.childNode(3).childNode(0).toString();
            String fundType = etf.childNode(5).childNode(0).toString();
            try {
                EtfInfoPO etfInfoPO = step2GetInfo0(code);
                if (Objects.isNull(etfInfoPO)) {
                    continue;
                }
                EtfInfoPO db = getRepository().findByCode(code);
                if (Objects.isNull(db)) {
                    db = new EtfInfoPO();
                    ConvertUtils.convertObj2(etfInfoPO, db);
                    db.initCreateUserIdAndUpdateUserId(1L);
                } else {
                    db.setShortName(etfInfoPO.getShortName());
                    db.setReleaseDate(etfInfoPO.getReleaseDate());
                    db.setScale(etfInfoPO.getScale());
                    db.setFenhong(etfInfoPO.getFenhong());
                    db.setManageRadio(etfInfoPO.getManageRadio());
                    db.setHolderRadio(etfInfoPO.getHolderRadio());
                    db.setSellRadio(etfInfoPO.getSellRadio());
                    db.setBusiCompareBase(etfInfoPO.getBusiCompareBase());
                    db.setFollowIndex(etfInfoPO.getFollowIndex());
                }
                db.setType(fundType);
                db.setUpdateTime(LocalDateTime.now());
                save(db);
                log.info("code of {} is done", code);
            } catch (Exception e) {
                log.error("error when save etf_info {}", code, e);
            }
        }

    }

    private void step1GatherEtfList() {
        final String url = "http://fund.eastmoney.com/ETFN_jzzzl.html";
        String pageSource = HttpClient4Utils.doGet(url, null, Charset.forName("gb2312").name());
        Document document = Jsoup.parse(pageSource);
        Elements tableDivEle = document.select("#tableDiv");
        Elements trList = tableDivEle.select("tr");
        List<Element> etfList = trList.subList(2, trList.size() - 1);
        for (Element etf : etfList) {
            String code = etf.childNode(3).childNode(0).toString();
            try {
                EtfInfoPO etfInfoPO = step2GetInfo0(code);
                if (Objects.isNull(etfInfoPO)) {
                    continue;
                }
                EtfInfoPO db = getRepository().findByCode(code);
                if (Objects.isNull(db)) {
                    db = new EtfInfoPO();
                    ConvertUtils.convertObj2(etfInfoPO, db);
                    db.initCreateUserIdAndUpdateUserId(1L);
                } else {
                    db.setShortName(etfInfoPO.getShortName());
                    db.setReleaseDate(etfInfoPO.getReleaseDate());
                    db.setScale(etfInfoPO.getScale());
                    db.setFenhong(etfInfoPO.getFenhong());
                    db.setManageRadio(etfInfoPO.getManageRadio());
                    db.setHolderRadio(etfInfoPO.getHolderRadio());
                    db.setSellRadio(etfInfoPO.getSellRadio());
                    db.setBusiCompareBase(etfInfoPO.getBusiCompareBase());
                    db.setFollowIndex(etfInfoPO.getFollowIndex());
                }
                EtfInfoPO finalDb = db;
                VtObjectUtils.doIfBlankString(db.getType(), () -> finalDb.setType("etf"));
                finalDb.setUpdateTime(LocalDateTime.now());
                save(finalDb);
                log.info("code of {} is done", code);
            } catch (Exception e) {
                log.error("error when save etf_info {}", code, e);
            }
        }

    }

    private EtfInfoPO step2GetInfo0(String code) {
        String infoUrl = "http://fundf10.eastmoney.com/jbgk_" + code + ".html";
        String pageSource = HttpClient4Utils.doGet(infoUrl, null, StandardCharsets.UTF_8.name());
        Document document = Jsoup.parse(pageSource);
        Elements infoOfFund = document.select("table.info");
        List<Node> trList = infoOfFund.get(0).childNode(0).childNodes();
        String shortName = trList.get(0).childNode(3).childNode(0).toString();
        String releaseDate = null;
        try {
            releaseDate = trList.get(2).childNode(1).childNode(0).toString();
        } catch (Exception ignored) {
        }
        if (Objects.isNull(releaseDate)) {
            return null;
        }
        String scale = trList.get(3).childNode(1).childNode(0).toString();
        String fenhong = trList.get(5).childNode(3).childNode(0).toString();

        String manageRadio = trList.get(6).childNode(1).childNode(0).toString();
        String holderRadio = trList.get(6).childNode(3).childNode(0).toString();
        String sellRadio = trList.get(7).childNode(1).childNode(0).toString();
        String busiBase = trList.get(9).childNode(1).childNode(0).toString(); // 业绩比较基准
        String followIndex = trList.get(9).childNode(3).childNode(0).toString(); // 跟踪标的
        EtfInfoPO etfInfo = new EtfInfoPO();
        etfInfo.setCode(code);
        etfInfo.setShortName(shortName);
        etfInfo.setReleaseDate(replaceHanziInDateStr(releaseDate));
        etfInfo.setScale(formatScale(scale));
        etfInfo.setFenhong(formatATag(fenhong));
        etfInfo.setManageRadio(formatNumberRadio(manageRadio));
        etfInfo.setHolderRadio(formatNumberRadio(holderRadio));
        etfInfo.setSellRadio(formatNumberRadio(sellRadio));
        etfInfo.setBusiCompareBase(busiBase);
        etfInfo.setFollowIndex(followIndex);
        return etfInfo;
    }

}
