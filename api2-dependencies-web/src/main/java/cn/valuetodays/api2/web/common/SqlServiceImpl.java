package cn.valuetodays.api2.web.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-11-01
 */
@ApplicationScoped
@Slf4j
public class SqlServiceImpl {

    @Inject
    DataSource dataSource;

    @Transactional
    public AffectedRowsResp saveBySqls(List<String> sqls) {
        if (CollectionUtils.isEmpty(sqls)) {
            return AffectedRowsResp.empty();
        }
        for (String sql : sqls) {
            saveBySql(sql);
        }
        int sum = sqls.stream().mapToInt(this::saveBySql).sum();
        return AffectedRowsResp.of(sum);
    }

    private int saveBySql(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        } catch (Exception e) {
            log.error("error when saveBySql", e);
        }
        return 0;
    }

    private final QueryRunner runner = new QueryRunner();

    public <T> T queryForObject(String sql, Class<T> clazz, final Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            return runner.query(conn, sql, new BeanHandler<T>(clazz), params);
        } catch (Exception e) {
            log.error("error when queryForObject", e);
        }
        return null;
    }

    public <T> List<T> queryForList(String sql, Class<T> clazz, final Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            return runner.query(conn, sql, new BeanListHandler<T>(clazz), params);
        } catch (Exception e) {
            log.error("error when queryForObject", e);
        }
        return List.of();
    }

    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        try (Connection conn = dataSource.getConnection()) {
            return runner.batch(conn, sql, batchArgs.toArray(new Object[0][]));
        } catch (Exception e) {
            log.error("error when queryForObject", e);
        }
        return new int[0];
    }
}
