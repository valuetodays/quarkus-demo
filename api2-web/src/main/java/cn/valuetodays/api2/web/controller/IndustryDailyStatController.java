package cn.valuetodays.api2.web.controller;

import cn.valuetodays.api2.client.persist.IndustryDailyStatPersist;
import cn.valuetodays.api2.client.req.IndustryDailyStatDaysReq;
import cn.valuetodays.api2.web.service.IndustryDailyStatService;
import cn.vt.R;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-05-18
 */
@RequestScoped
@Path("/industryDailyStat")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class IndustryDailyStatController {
    @Inject
    private IndustryDailyStatService industryDailyStatService;

    @POST
    @Path("/getByLatestDays")
    public R<List<IndustryDailyStatPersist>> getByLatestDays(IndustryDailyStatDaysReq req) {
        int days = Math.max(req.getDays(), 30);
        LocalDate localDate = LocalDate.now().minusDays(days);
        return R.success(industryDailyStatService.getByStatDateGe(localDate));
    }

    @POST
    @Path("/getByStatDate")
    public R<List<IndustryDailyStatPersist>> getByStatDate(LocalDate localDate) {
        if (Objects.isNull(localDate)) {
            return R.fail("statDate should not be null");
        }
        return R.success(industryDailyStatService.getByStatDate(localDate));
    }

}
