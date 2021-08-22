package org.monkey.mmq.web.config;

import com.google.common.collect.Lists;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;


/**
 * 封装统一返回结果，仅对本项目<tt>com.canaan</tt>包下的方法进行返回结果封装，避免影响swagger相关请求及其他插件的请求
 * <ul>
 * 	<li>void类型会统一封装成{@link ResponseResult}</li>
 * 	<li>ModelAndView会原样返回</li>
 * 	<li>{@link ResponseResult}类型会进行判断是否抛出异常，如果抛出异常并且是<tt>Ajax</tt>请求，封装ModelAndView返回错误页面，否则原样返回</li>
 * 	<li> 其他类型封装成{@link ResponseResult}</li>
 * </ul>
 * @author solley
 * @date 2017年12月20日 上午9:28:34
 * @version V1.0
 */
@RestControllerAdvice
public class ReturnAdviceHandler implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		boolean bePrivate = Modifier.PRIVATE == returnType.getMethod().getModifiers();
		//private  方法直接跳过
		if (bePrivate) {
			return false;
		}
		NotWrap notWrap = returnType.getMethod().getAnnotation(NotWrap.class);
		if (notWrap != null) {
			return false;
		}
		String name = returnType.getMethod().getDeclaringClass().getName();
		//仅对本项目结果进行封装
		return name.startsWith("org.monkey.mmq.web");
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
		//封装返回结果
		if (body == null) {
			return RestResultUtils.success();
		}

		if (ModelAndView.class.isInstance(body)) {
			ModelAndView mv = (ModelAndView) body;
			return mv;
		}

		if (RestResultUtils.class.isInstance(body)) {
			return body;
		}

		if (Collection.class.isInstance(body)) {
			Collection<?> ction = (Collection<?>) body;
			List<?> list = null;
			if (ction instanceof List) {
				list = (List<?>) ction;
			} else {
				list = Lists.newArrayList(ction);
			}
			return RestResultUtils.success(list);
		}

		if("OPTIONS".equals(request.getMethod())) {
			response.setStatusCode(HttpStatus.OK);
    	}
		return RestResultUtils.success(body);
	}

}
