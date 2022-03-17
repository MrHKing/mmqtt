package org.monkey.mmq.notifier;

import org.apache.commons.text.StringSubstitutor;
import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RuleEngineActorTest {
    @Test
    public void testSpelExpressionParser() {
        String phoneNo = "13812341234";
        String smsTemplate = "验证码:#{[payload][previous][gpsSpeed]},您正在登录管理后台，5分钟内输入有效。";
        Map valuesMap = new HashMap<String, Object>() {
            {
                put("deviceNum", "12131221");
                put("payload", new HashMap<String, Object>() {
                            {
                                put("previous", new HashMap<String, Object>() {{
                                    put("gpsSpeed", 22.3);
                                    put("oilTemperature", 34.5);
                                }});
                                put("current", new HashMap<String, Object>() {{
                                    put("gpsSpeed", 34.3);
                                    put("oilTemperature", 45.5);
                                }});
                            }
                        }
                );
            }
        };

        ExpressionParser parser = new SpelExpressionParser();
        TemplateParserContext parserContext = new TemplateParserContext();
        String content = parser.parseExpression(smsTemplate,parserContext).getValue(valuesMap, String.class);

        System.out.println(content);
    }

    @Test
    public void testApacheCommons() {
        Map valuesMap = new HashMap<String, Object>() {
            {
                put("deviceNum", "12131221");
                put("payload", new HashMap<String, Object>() {
                            {
                                put("previous", new HashMap<String, Object>() {{
                                    put("gpsSpeed", 22.3);
                                    put("oilTemperature", 34.5);
                                }});
                                put("current", new HashMap<String, Object>() {{
                                    put("gpsSpeed", 34.3);
                                    put("oilTemperature", 45.5);
                                }});
                            }
                        }
                );
            }
        };
//        valuesMap.put("code", 1234);
        System.out.println(valuesMap.get("deviceNum"));
        String templateString = "验证码:${payload.get('previous').get('gpsSpeed')}.${previous},您正在登录管理后台，5分钟内输入有效。";
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String content= sub.replace(templateString);
        System.out.println(content);
    }
}