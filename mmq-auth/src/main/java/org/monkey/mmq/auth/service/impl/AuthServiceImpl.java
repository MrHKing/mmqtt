/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.monkey.mmq.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import org.monkey.mmq.auth.exception.AccessException;
import org.monkey.mmq.auth.model.Permission;
import org.monkey.mmq.auth.model.User;
import org.monkey.mmq.auth.service.IAuthService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;

/**
 * 用户名和密码认证服务
 */
@Service("authService")
public class AuthServiceImpl implements IAuthService {

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

	@Override
	public User login(Object request) throws AccessException {
		return null;
	}

	@Override
	public User loginRemote(Object request) throws AccessException {
		return null;
	}

	@Override
	public void auth(Permission permission, User user) throws AccessException {

	}

	@PostConstruct
	public void init() {
		//privateKey = IoUtil.readObj(AuthService.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
	}

}
