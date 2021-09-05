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

package org.monkey.mmq.service;


import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.metadata.KeyBuilder;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.metadata.UtilsAndCommons;
import org.monkey.mmq.metadata.message.MessageIdMateData;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 消息ID生成
 */
@Service
public class MessageIdService implements RecordListener<MessageIdMateData> {

	private final int MIN_MSG_ID = 1;

	private final int MAX_MSG_ID = 65535;

	private final int lock = 0;

	MessageIdMateData messageIdMateData = new MessageIdMateData();

	@Resource(name = "mqttPersistentConsistencyServiceDelegate")
	private ConsistencyService consistencyService;

	private int nextMsgId = MIN_MSG_ID - 1;

	public int getNextMessageId() {
		try {
			while (true) {

				int nextMsgId = messageIdMateData.getValue().addAndGet(1) % MAX_MSG_ID;
				consistencyService.put(UtilsAndCommons.MESSAGE_Id_STORE, messageIdMateData);
				if (nextMsgId > 0) {
					return nextMsgId;
				}
			}
		} catch (Exception e) {
			Loggers.BROKER_SERVER.error("listen subscribe service failed.", e);
		}
		return 0;
	}

	/**
	 * 每次重启的时候初始化
	 */
	@PostConstruct
	public void init() {
		try {
			consistencyService.listen(KeyBuilder.getMessageIdStoreKey(), this);
			consistencyService.put(UtilsAndCommons.MESSAGE_Id_STORE, new MessageIdMateData());
		} catch (MmqException e) {
			Loggers.BROKER_SERVER.error("listen subscribe service failed.", e);
		}

	}

	@Override
	public boolean interests(String key) {
		return KeyBuilder.matchMessageIdKey(key);
	}

	@Override
	public boolean matchUnlistenKey(String key) {
		return KeyBuilder.matchMessageIdKey(key);
	}

	@Override
	public void onChange(String key, MessageIdMateData value) throws Exception {
		messageIdMateData = value;
	}

	@Override
	public void onDelete(String key) throws Exception {
		messageIdMateData = null;
	}
}
