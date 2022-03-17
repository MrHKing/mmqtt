package org.monkey.mmq.config.modules.auth;

import org.monkey.mmq.config.driver.DriverFactory;
import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.config.matedata.ResourceEnum;
import org.monkey.mmq.config.modules.BaseModule;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassNameSqlServerAuthModule
 * @Description
 * @Author Solley
 * @Date2021/12/27 11:36
 * @Version V1.0
 **/
@Component
public class AuthModule extends BaseModule<AuthParam> {

    private static final String RESOURCE_TYPE = "resourceType";

    private static final String RESOURCE_ID = "resourceId";

    private static final String TABLE = "table";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    private static final String sqlFormat = "select count(*) as total from %s where %s='%s' and %s='%s'";

    @PostConstruct
    private void init() {
        modelMateData = new ModelMateData();
        modelMateData.setKey("AUTH-MODULE");
        modelMateData.setModelEnum(ModelEnum.AUTH);
        Map<String, Object> map = new HashMap<>();
        map.put(RESOURCE_TYPE, "");
        map.put(RESOURCE_ID,"");
        map.put(TABLE,"");
        map.put(USERNAME,"");
        map.put(PASSWORD,"");
        modelMateData.setConfigs(map);
        modelMateData.setDescription("MQTT 连接客户端账号密码认证模块，支持SQL SERVER、Mysql、Postgresql等关系型数据库");
        modelMateData.setEnable(false);
        modelMateData.setIcon("auth-icon");
        modelMateData.setModuleName("认证鉴权");
    }

    @Override
    public boolean handle(AuthParam authParam) throws MmqException {
        if (!this.modelMateData.getEnable()) return true;

        if (authParam == null) return false;

        // 获得资源类型配置
        String resourceType = (String) this.modelMateData.getConfigs().get(RESOURCE_TYPE);
        ResourceEnum resourceEnum = Enum.valueOf(ResourceEnum.class, resourceType);
        if (resourceEnum == null) return false;

        // 获取资源id
        String resourceId = (String) this.modelMateData.getConfigs().get(RESOURCE_ID);
        if (StringUtils.isEmpty(resourceId)) return false;

        // 表名
        String table = (String) this.modelMateData.getConfigs().get(TABLE);
        if (StringUtils.isEmpty(table)) return false;

        // 账户字段
        String username = (String) this.modelMateData.getConfigs().get(USERNAME);
        if (StringUtils.isEmpty(username)) return false;

        // 密码字段
        String password = (String) this.modelMateData.getConfigs().get(PASSWORD);
        if (StringUtils.isEmpty(password)) return false;
        // 查询SQL
        Connection connection = null;
        Statement stmt = null;
        try {
            // 获取连接
            connection = (Connection) DriverFactory.getResourceDriverByEnum(resourceEnum).getDriver(resourceId);
            if (connection == null) return false;

            String sql = String.format(sqlFormat, table,
                    username, authParam.getUsername(), password, authParam.getPassword());
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = stmt.executeQuery(sql);
            resultSet.last();
            boolean ret = resultSet.getLong("total") > 0;

            // 返回结果
            if (ret) return true;
            return false;
        } catch (Exception e) {
            throw new MmqException(e.hashCode(), e.getMessage());
        } finally {
            try {
                stmt.close();
                connection.close();
            } catch (SQLException throwables) {
                throw new MmqException(throwables.hashCode(), throwables.getMessage());
            }
        }
    }
}
