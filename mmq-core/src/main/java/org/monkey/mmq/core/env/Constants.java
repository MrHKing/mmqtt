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

package org.monkey.mmq.core.env;

/**
 * MMQ common constants.
 *
 * @author solley
 */
public interface Constants {
    
    /**
     * Spring Profile : "standalone".
     */
    String STANDALONE_SPRING_PROFILE = "standalone";
    
    /**
     * The System property name of  Standalone mode.
     */
    String STANDALONE_MODE_PROPERTY_NAME = "mmq.standalone";
    
    /**
     * The System property name of  Function mode.
     */
    String FUNCTION_MODE_PROPERTY_NAME = "mmq.functionMode";
    
    /**
     * The System property name of prefer hostname over ip.
     */
    String PREFER_HOSTNAME_OVER_IP_PROPERTY_NAME = "mmq.preferHostnameOverIp";
    
    /**
     * the root context path.
     */
    String ROOT_WEB_CONTEXT_PATH = "/";
    
    String MMQ_SERVER_IP = "mmq.server.ip";
    
    String USE_ONLY_SITE_INTERFACES = "mmq.inetutils.use-only-site-local-interfaces";
    String PREFERRED_NETWORKS = "mmq.inetutils.preferred-networks";
    String IGNORED_INTERFACES = "mmq.inetutils.ignored-interfaces";
    String IP_ADDRESS = "mmq.inetutils.ip-address";
    String PREFER_HOSTNAME_OVER_IP = "mmq.inetutils.prefer-hostname-over-ip";
    String SYSTEM_PREFER_HOSTNAME_OVER_IP = "mmq.preferHostnameOverIp";
    String WEB_CONTEXT_PATH = "server.servlet.context-path";
    String COMMA_DIVISION = ",";
    
    String MMQ_SERVER_HEADER = "MMQ-Server";
    
    String REQUEST_PATH_SEPARATOR = "-->";
    
    String AVAILABLE_PROCESSORS_BASIC = "mmq.core.sys.basic.processors";
}
