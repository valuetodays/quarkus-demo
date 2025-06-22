package cn.valuetodays.api2.basic.controller;

import cn.valuetodays.api2.basic.service.SmsServiceImpl;
import cn.valuetodays.api2.basic.vo.SendSmsIO;
import cn.valuetodays.api2.basic.vo.SmsResultVO;
import cn.vt.R;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2022-04-23
 */
@Path("/common/sms")
public class SmsController {

    @Inject
    SmsServiceImpl smsService;

    @Path("sendSms")
    @POST
    public R<SmsResultVO> sendSms(SendSmsIO io) {
        io.makeIgnore();
        return R.success(smsService.sendSms(io));
    }
}
