package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import cn.vt.rest.third.utils.NumberUtils;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-02 18:41
 */
@Data
public class AShareLatestTurnAmountResp implements Serializable {
    private SingleRecord realtime;
    private List<SingleRecord> list;

    @Data
    public static class SingleRecord implements Serializable {

        private int minTime;
        private Long sh;
        private Double shByYi;
        private Long sz;
        private Double szByYi;
        private Long totalAShare;
        private Double totalAShareByYi;
        private Long bz50;
        private Double bz50ByYi;

        private Double shTotalMarketCapByWanYi;
        private Double szTotalMarketCapByWanYi;
        private Double bzTotalMarketCapByWanYi;

        public void setSh(Long sh) {
            this.sh = sh;
            this.shByYi = 1.0 * sh / NumberUtils.YI.doubleValue();
        }

        public void setSz(Long sz) {
            this.sz = sz;
            this.szByYi = 1.0 * sz / NumberUtils.YI.doubleValue();
        }

        public void setTotalAShare(Long totalAShare) {
            this.totalAShare = totalAShare;
            this.totalAShareByYi = 1.0 * totalAShare / NumberUtils.YI.doubleValue();
        }

        public void setBz50(Long bz50) {
            this.bz50 = bz50;
            this.bz50ByYi = 1.0 * bz50 / NumberUtils.YI.doubleValue();
        }
    }

}
