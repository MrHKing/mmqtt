package org.monkey.mmq.config.driver;

import org.junit.Test;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class InfluxDB1XDriverTest {
    @Test
    public void testStringTemplate() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "hhh");
        map.put("age", "15");
        Map<String, Object> map1 = new HashMap<>();
        map1.put("5566", 33);
        map1.put("12312", 441);
        map.put("codes", map1);
    ST hello = new ST("Hello, <json.codes:{x|<x>};separator=\",\">");
        hello.add("json", map);
        System.out.println(hello.render());
    }

}