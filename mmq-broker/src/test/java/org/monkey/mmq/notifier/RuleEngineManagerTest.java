package org.monkey.mmq.notifier;

import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RuleEngineManagerTest {
    @Test
    public void testSpelExpressionParser() {
        String phoneNo = "13812341234";
        String smsTemplate = "验证码:#{[code]},您正在登录管理后台，5分钟内输入有效。";
        Map<String, Object> params = new HashMap<>();
        params.put("code", 1234);;

        ExpressionParser parser = new SpelExpressionParser();
        TemplateParserContext parserContext = new TemplateParserContext();
        String content = parser.parseExpression(smsTemplate,parserContext).getValue(params, String.class);

        System.out.println(content);
    }
}