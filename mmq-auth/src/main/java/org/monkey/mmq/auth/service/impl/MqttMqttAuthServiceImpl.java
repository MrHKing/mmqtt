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
import org.monkey.mmq.auth.service.IMqttAuthService;
import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.config.modules.IModule;
import org.monkey.mmq.config.modules.ModuleFactory;
import org.monkey.mmq.config.modules.auth.AuthParam;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.MmqException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;

/**
 * MQTT用户名和密码认证服务
 */
@Service("mqttAuthService")
public class MqttMqttAuthServiceImpl implements IMqttAuthService {

	private RSAPrivateKey privateKey;

	private static final String MMQ_BROKER_DEFAULT_USER = "mmq.broker.default.user";
	private static final String DEFAULT_MMQ_BROKER_DEFAULT_USER = "admin";

	private static final String MMQ_BROKER_DEFAULT_PASSWORD = "mmq.broker.default.password";
	private static final String DEFAULT_MMQ_BROKER_DEFAULT_PASSWORD = "admin@mmq";

	private static final String MMQ_BROKER_DEFAULT_ANONYMOUS = "mmq.broker.default.anonymous";
	private static final String DEFAULT_MMQ_BROKER_DEFAULT_ANONYMOUS = "true";

	public String getUser() {
		return EnvUtil.getProperty(MMQ_BROKER_DEFAULT_USER, DEFAULT_MMQ_BROKER_DEFAULT_USER);
	}

	public String getPassword() {
		return EnvUtil.getProperty(MMQ_BROKER_DEFAULT_PASSWORD, DEFAULT_MMQ_BROKER_DEFAULT_PASSWORD);
	}

	public String getAnonymous() {
		return EnvUtil.getProperty(MMQ_BROKER_DEFAULT_ANONYMOUS, DEFAULT_MMQ_BROKER_DEFAULT_ANONYMOUS);
	}

	@Override
	public boolean checkValid(String username, String password) throws MmqException {

		IModule authModule = ModuleFactory.getResourceDriverByEnum(ModelEnum.AUTH);
		if (authModule.getEnable()) {
			if (StrUtil.isBlank(username)) return false;
			if (StrUtil.isBlank(password)) return false;
			AuthParam authParam = new AuthParam();
			authParam.setPassword(password);
			authParam.setUsername(username);
			return authModule.handle(authParam);
		}

		if ("true".equals(this.getAnonymous())) return true;
		if (StrUtil.isBlank(username)) return false;
		if (StrUtil.isBlank(password)) return false;
		if (this.getUser().equals(username) && this.getPassword().equals(password)) return true;
//		RSA rsa = new RSA(privateKey, null);
//		String value = rsa.encryptBcd(username, KeyType.PrivateKey);
//		return value.equals(password) ? true : false;
		return false;
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
