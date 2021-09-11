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

package org.monkey.mmq.rule.engine;

import net.sf.jsqlparser.statement.select.PlainSelect;
import org.monkey.mmq.rule.engine.feature.Feature;
import org.monkey.mmq.rule.engine.feature.FeatureId;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 元数据,用于管理特性,进行配置等操作
 *
 * @author solley
 * @see org.monkey.mmq.rule.engine.supports.DefaultReactorQLMetadata
 * @since 1.0.0
 */
public interface ReactorQLMetadata {

    /**
     * 获取特性
     *
     * @param featureId 特性ID
     * @param <T>       特性类型
     * @return 特性
     */
    <T extends Feature> Optional<T> getFeature(FeatureId<T> featureId);

    /**
     * 获取设置
     *
     * @param key key
     * @return 设置内容
     */
    Optional<Object> getSetting(String key);

    /**
     * 自定义设置
     *
     * @param key   key
     * @param value value
     * @return this
     */
    ReactorQLMetadata setting(String key, Object value);

    /**
     * 获取原始SQL
     *
     * @return SQL
     */
    PlainSelect getSql();

    /**
     * 获取特性,如果不存在则抛出异常
     *
     * @param featureId 特性ID
     * @param <T>       特性类型
     * @return 特性
     */
    default <T extends Feature> T getFeatureNow(FeatureId<T> featureId) {
        return getFeatureNow(featureId, featureId::getId);
    }

    /**
     * 获取特性,如果特性不存在则使用指定等错误消息抛出异常
     *
     * @param featureId    特性ID
     * @param errorMessage 错误消息
     * @param <T>          特性类型
     * @return 特性
     */
    default <T extends Feature> T getFeatureNow(FeatureId<T> featureId, Supplier<String> errorMessage) {
        return getFeature(featureId)
                .orElseThrow(() -> new UnsupportedOperationException("unsupported feature: " + errorMessage.get()));
    }


}
