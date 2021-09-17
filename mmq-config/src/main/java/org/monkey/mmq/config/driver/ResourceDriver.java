package org.monkey.mmq.config.driver;

import java.sql.SQLException;
import java.util.Map;

public interface ResourceDriver<T> {

    void init(Map<String, Object> resource);

    T getDriver() throws Exception;
}
