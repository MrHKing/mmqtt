/**
 * Copyright (c) 2020, Solley (hkk@yanboo.com.cn) All rights reserved.
 */

package org.monkey.mmq.auth;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;

/**
 * 用户名和密码认证服务
 */
@Service
public class AuthService implements IAuthService {

	private RSAPrivateKey privateKey;

	@Override
	public boolean checkValid(String username, String password) {
		if (StrUtil.isBlank(username)) return false;
		if (StrUtil.isBlank(password)) return false;
//		RSA rsa = new RSA(privateKey, null);
//		String value = rsa.encryptBcd(username, KeyType.PrivateKey);
//		return value.equals(password) ? true : false;
		return true;
	}

	@PostConstruct
	public void init() {
		//privateKey = IoUtil.readObj(AuthService.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
	}

}
