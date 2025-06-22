package cn.valuetodays.api2.basic.controller;

import cn.valuetodays.api2.basic.vo.EncryptPasswordReq;
import cn.valuetodays.api2.basic.vo.EncryptPasswordResp;
import cn.vt.R;
import cn.vt.encrypt.BCryptUtils;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-10
 */
@Tag(name = "密码工具")
@Path("/passwordTool")
public class PasswordToolController {

    @Operation(
        description = "加密密码"
    )
    @Path("/encryptPassword")
    @POST
    public R<EncryptPasswordResp> encryptPassword(EncryptPasswordReq encryptPasswordReq) {
        String hashpwed = BCryptUtils.hashpw(encryptPasswordReq.getRawPassword());
        EncryptPasswordResp resp = new EncryptPasswordResp();
        resp.setEncryptedPassword(hashpwed);
        return R.success(resp);
    }
}
