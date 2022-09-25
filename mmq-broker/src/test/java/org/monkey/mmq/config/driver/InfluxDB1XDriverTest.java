package org.monkey.mmq.config.driver;

import org.junit.Test;
import org.stringtemplate.v4.ST;

import java.util.*;

import static org.junit.Assert.*;

public class InfluxDB1XDriverTest {
    @Test
    public void testStringTemplate() {
        Map<String, Object> map = new HashMap<>();
        map.put("device", "hhh");

        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "flow");
        map1.put("value", 441);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "flow");
        map2.put("value", 441);
        List<Map> mapList = new ArrayList<>();
        mapList.add(map1);
        mapList.add(map2);
        map.put("list", mapList);
    ST hello =
        new ST(
            "[<json.list:{x | {measurement: h2o_feet, tags: {device: <json.device>, name: <x.name> \\},fields: { value: <x.value>\\}\\}}; separator=\",\">]");
        hello.add("json", map);
        System.out.println(hello.render());
    }

}