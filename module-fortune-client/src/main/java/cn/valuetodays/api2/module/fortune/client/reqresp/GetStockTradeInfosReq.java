package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import cn.vt.web.req.BaseAccountableReq;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-03-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetStockTradeInfosReq extends BaseAccountableReq {
    private String hedgeIds;
    private String vsIds;

    @JsonIgnore
    public List<Long> toIdList() {
        return Stream.concat(
                Arrays.stream(StringUtils.split(hedgeIds, ",")),
                Arrays.stream(StringUtils.split(vsIds, ","))
            )
            .filter(StringUtils::isNotBlank)
            .mapToLong(Long::valueOf)
            .distinct()
            .boxed()
            .toList();

    }
}
