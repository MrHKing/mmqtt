package org.monkey.mmq.config.driver;

import java.sql.SQLException;
import java.util.Map;

public interface ResourceDriver<T> {

    void addDriver(String resourceId, Map<String, Object> resource);

    T getDriver(String resourceId) throws Exception;
}
