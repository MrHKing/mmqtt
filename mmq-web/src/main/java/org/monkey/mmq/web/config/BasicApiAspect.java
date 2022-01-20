package org.monkey.mmq.web.config;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.config.modules.IModule;
import org.monkey.mmq.config.modules.ModuleFactory;
import org.monkey.mmq.config.modules.api.ApiParam;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.monkey.mmq.web.security.MmqAuthConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;

/**
 * @ClassNameBasicApiAspect
 * @Description
 * @Author Solley
 * @Date2022/1/19 12:44
 * @Version V1.0
 **/
@Aspect
@Component
public class BasicApiAspect {

    private static final String TOKEN_PREFIX = "Basic ";

    private final Base64.Decoder decoder = Base64.getDecoder();

    IModule authModule = ModuleFactory.getResourceDriverByEnum(ModelEnum.API);

    // 定义切点Pointcut
    @Pointcut("@annotation(org.monkey.mmq.web.config.BasicApi)")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object[] params = pjp.getArgs();
            for (Object param : params) {
                if (param instanceof HttpServletRequest) {
                    String bearerToken = ((HttpServletRequest) param).getHeader(MmqAuthConfig.AUTHORIZATION_HEADER);
                    if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
                        String token = bearerToken.substring(TOKEN_PREFIX.length());
                        String userStr = new String(decoder.decode(token), "UTF-8");
                        String[] userInfo = userStr.split(":");
                        ApiParam apiParam = new ApiParam();
                        apiParam.setAppId(userInfo[0]);
                        apiParam.setAppSecret(userInfo[1]);
                        if (authModule.handle(apiParam)) {
                            return pjp.proceed();
                        } else {
                            return RestResultUtils.failed(HttpStatus.UNAUTHORIZED.value(), null, "Unauthorized");
                        }
                    }
                }
            }
        } catch (Exception e) {
            return RestResultUtils.failed(HttpStatus.UNAUTHORIZED.value(), null, e.getMessage());
        }
        return RestResultUtils.failed(HttpStatus.UNAUTHORIZED.value(), null, "Unauthorized");
    }
}
