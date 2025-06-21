package cn.vt.rest.third.eastmoney.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 指数历史k线数据
 */
@Data
public class QuoteHistoryIndexRootData implements Serializable {
    private DataBean data;

    @Data
    public static class DataBean implements Serializable {
        private CandleBean candle;

        @Data
        public static class CandleBean implements Serializable {
            private List<String> fields;
            private List<List<String>> history;
        }
    }

}
