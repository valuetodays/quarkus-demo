package cn.valuetodays.module.spider.controller;

import cn.valuetodays.api2.web.common.GithubComponent;
import cn.valuetodays.module.spider.client.persist.WxmpArticleImagePersist;
import cn.valuetodays.module.spider.service.WxmpArticleImageService;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import cn.vt.R;
import cn.vt.exception.AssertUtils;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.List;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-05-25
 */
@RequestScoped
@Path("/wxmpArticleImage")
public class WxmpArticleImageController
    extends BaseCrudController<
    Long,
    WxmpArticleImagePersist,
    WxmpArticleImageService
    > {
    @Inject
    ManagedExecutor managedExecutor;
    @Inject
    GithubComponent githubComponent;

    @Path("/scheduleDownloadImage")
    @POST
    public R<String> scheduleDownloadImage() {
        managedExecutor.execute(() -> service.scheduleDownloadImage());
        return R.success("SUCCESS");
    }

    @Path("/uploadImageByWxmpUrl.do")
    @POST
    public List<String> uploadImageByWxmpUrl(SimpleTypesReq req) {
        String url = req.getText();
        AssertUtils.assertStringNotBlank(url, "text不能为空");

        return githubComponent.uploadImageByWxmpUrl(url).getItem2();
    }
}
