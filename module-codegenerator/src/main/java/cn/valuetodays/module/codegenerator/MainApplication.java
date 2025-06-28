package cn.valuetodays.module.codegenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import cn.hutool.core.util.ZipUtil;
import cn.valuetodays.module.codegenerator.advanced.config.Datasource;
import cn.valuetodays.module.codegenerator.advanced.config.configfile.CgConfig;
import cn.valuetodays.module.codegenerator.advanced.config.configfile.Files;
import cn.valuetodays.module.codegenerator.advanced.config.datasource.FileDatasource;
import cn.valuetodays.module.codegenerator.advanced.config.datasource.MysqlDatasource;
import cn.valuetodays.module.codegenerator.advanced.generate.BaseTemplate;
import cn.valuetodays.module.codegenerator.db.DatabaseFactory;
import cn.valuetodays.module.codegenerator.db.FileDatabase;
import cn.valuetodays.module.codegenerator.db.IDatabase;
import cn.valuetodays.module.codegenerator.entity.GenerateReq;
import cn.valuetodays.module.codegenerator.entity.GenerateResp;
import cn.valuetodays.module.codegenerator.pojo.TableClass;
import cn.valuetodays.module.codegenerator.pojo.TableClasses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * @author lei.liu
 * @since 2018-09-06 15:52:24
 */
@Slf4j
public class MainApplication {
    private CgConfig cgConfig;

    private MainApplication() {
    }

    public static GenerateResp doGenerate(GenerateReq generateReq) throws Exception {
        MainApplication ama = new MainApplication();
        ama.preInit();
        ama.init(generateReq);
        ama.postInit();
        ama.display();
        ama.checkDirNotExist();
        GenerateResp resp = new GenerateResp();
        ama.doProcess(resp);
        ama.doPostProcess(resp);
        return resp;
    }

    private void preInit() {

    }

    private void postInit() {
        cgConfig.afterPropertiesSet();
    }

    /**
     * 填充configFile对象
     *
     * @param generateReq req
     * @see #cgConfig
     */
    private void init(GenerateReq generateReq) {
        cgConfig = generateReq.toConfigFile();
    }

    private void display() {
    }

    private void checkDirNotExist() {
        log.info("project-debug: {}", cgConfig.getProject().isDebug());
        String targetBaseDir = cgConfig.getProject().getTargetBaseDir();
        String projectName = cgConfig.getProject().getFormattedName();
        log.info("targetBaseDir: {}, projectName: {}", targetBaseDir, projectName);
        File file = new File(targetBaseDir, projectName);
        if (file.exists()) {
            throw new RuntimeException("file exists, please delete it first: " + file.getAbsolutePath());
        }
    }

    private void doProcess(GenerateResp resp) throws IOException {
        TableClasses tableClasses = null;
        IDatabase database = null;

        Datasource activeDatasource = cgConfig.getActiveDatasource();
        Datasource.Type type = activeDatasource.getType();
        if (Datasource.Type.FILE == type) {
            FileDatasource fileDatasource = (FileDatasource) activeDatasource;

            boolean preferUseResource = fileDatasource.isPreferUseResource();
            if (preferUseResource) {
                String settingResource = fileDatasource.getSettingResource();
                Yaml yaml = new Yaml();
                String settingResourceWithoutClasspath = settingResource;
                if (settingResourceWithoutClasspath.startsWith("/")) {
                    settingResourceWithoutClasspath = settingResourceWithoutClasspath.substring("/".length());
                }
                try (InputStream in =
                         Thread.currentThread().getContextClassLoader().getResourceAsStream(settingResourceWithoutClasspath)) {
                    tableClasses = yaml.loadAs(in, TableClasses.class);
                    database = new FileDatabase(tableClasses);
                }
            } else {
                resp.setMsg("not support");
                return;
            }
        } else if (Datasource.Type.MYSQL == type) {
            MysqlDatasource mysqlDatasource = (MysqlDatasource) activeDatasource;
            String url = mysqlDatasource.getUrl();
            String username = mysqlDatasource.getUsername();
            String password = mysqlDatasource.getPassword();
            database = DatabaseFactory.createDatabase(Datasource.Type.MYSQL.name(), url, username, password);
            tableClasses = new TableClasses();
        } else {
            resp.setMsg("unknown type: " + type);
            return;
        }

        tableClasses.setDatabase(database);
        tableClasses.setCgConfig(cgConfig);
        tableClasses.afterPropertiesSet();

        if (CollectionUtils.isEmpty(tableClasses.getFinalTableClassList())) {
            resp.setMsg("warn: no table to process. please check your config file.");
            return;
        }
        Files files = cgConfig.getFiles();
        List<BaseTemplate> fileList = files.getFileList();
        if (CollectionUtils.isEmpty(fileList)) {
            resp.setMsg("no template files found");
            return;
        }
        log.info("try to process {} templates", fileList.size());
        for (TableClass tableClass : tableClasses.getFinalTableClassList()) {
            log.info("processing table: {}", tableClass.getTableName());
            for (BaseTemplate baseTemplate : fileList) {
                log.info("processing template: {}", baseTemplate.getSuffix());
                Map<String, Object> context = baseTemplate.getConfiguration().getContext();
                context.put("pojo", tableClass);
                context.put("properties", cgConfig.getProperties());
                context.put("project", cgConfig.getProject());
                context.put("tableNameFilter", cgConfig.getTableNameFilter());

                baseTemplate.doGenerate();
            }
        }
        if (cgConfig.getProject().isZip()) {
            final Path dir = Paths.get(
                cgConfig.getProject().getTargetBaseDir(),
                cgConfig.getProject().getFormattedName()
            );

            final File zipFile = new File(dir.toFile().getAbsolutePath() + ".zip");
            ZipUtil.zip(zipFile, StandardCharsets.UTF_8, true, dir.toFile());
            resp.setFile(zipFile);
        }
    }

    private void doPostProcess(GenerateResp resp) {
        resp.setOk(!StringUtils.isNotBlank(resp.getMsg()));
    }

}
