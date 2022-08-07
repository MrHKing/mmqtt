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

import com.sun.management.OperatingSystemMXBean;
import org.monkey.mmq.core.utils.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Its own configuration information manipulation tool class.
 *
 * @author solley
 */
public class EnvUtil {
    
    public static final String STANDALONE_MODE_ALONE = "standalone";
    
    public static final String STANDALONE_MODE_CLUSTER = "cluster";
    
    /**
     * The key of mmq home.
     */
    public static final String MMQ_HOME_KEY = "mmq.home";
    
    private static String localAddress = "";
    
    private static int port = -1;
    
    private static Boolean isStandalone = null;
    
    private static String functionModeType = null;
    
    private static String contextPath = null;
    
    private static final String FILE_PREFIX = "file:";
    
    private static final String SERVER_PORT_PROPERTY = "server.port";
    
    private static final int DEFAULT_SERVER_PORT = 8888;
    
    private static final String DEFAULT_WEB_CONTEXT_PATH = "/mmq";
    
    private static final String MEMBER_LIST_PROPERTY = "mmq.member.list";
    
    private static final String MMQ_HOME_PROPERTY = "user.home";
    
    private static final String CUSTOM_CONFIG_LOCATION_PROPERTY = "spring.config.additional-location";
    
    private static final String DEFAULT_CONFIG_LOCATION  = "application.properties";
    
    private static final String DEFAULT_RESOURCE_PATH = "/application.properties";
    
    private static final String DEFAULT_ADDITIONAL_PATH = "conf";

    private static final String DEFAULT_PLUGS_PATH = "plugs";
    
    private static final String DEFAULT_ADDITIONAL_FILE = "cluster.conf";
    
    private static final String MMQ_HOME_ADDITIONAL_FILEPATH = "mmq";
    
    private static final String MMQ_TEMP_DIR_1 = "data";
    
    private static final String MMQ_TEMP_DIR_2 = "tmp";

    private static String confPath = "";

    private static String plugsPath = "";

    private static String mmqHomePath = null;
    
    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = (com.sun.management.OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();
    
    private static ConfigurableEnvironment environment;
    
    public static ConfigurableEnvironment getEnvironment() {
        return environment;
    }
    
    public static void setEnvironment(ConfigurableEnvironment environment) {
        EnvUtil.environment = environment;
    }
    
    public static boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }
    
    public static String getProperty(String key) {
        return environment.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }
    
    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }
    
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }
    
    public static String getRequiredProperty(String key) throws IllegalStateException {
        return environment.getRequiredProperty(key);
    }
    
    public static <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return environment.getRequiredProperty(key, targetType);
    }
    
    public static String resolvePlaceholders(String text) {
        return environment.resolvePlaceholders(text);
    }
    
    public static String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return environment.resolveRequiredPlaceholders(text);
    }
    
    public static List<String> getPropertyList(String key) {
        List<String> valueList = new ArrayList<>();
        
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String value = environment.getProperty(key + "[" + i + "]");
            if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
                break;
            }
            
            valueList.add(value);
        }
        
        return valueList;
    }
    
    public static String getLocalAddress() {
        if (StringUtils.isBlank(localAddress)) {
            localAddress = InetUtils.getSelfIP() + ":" + getPort();
        }
        return localAddress;
    }
    
    public static void setLocalAddress(String localAddress) {
        EnvUtil.localAddress = localAddress;
    }
    
    public static int getPort() {
        if (port == -1) {
            port = getProperty(SERVER_PORT_PROPERTY, Integer.class, DEFAULT_SERVER_PORT);
        }
        return port;
    }
    
    public static void setPort(int port) {
        EnvUtil.port = port;
    }
    
    public static String getContextPath() {
        if (Objects.isNull(contextPath)) {
            contextPath = getProperty(Constants.WEB_CONTEXT_PATH, DEFAULT_WEB_CONTEXT_PATH);
            if (Constants.ROOT_WEB_CONTEXT_PATH.equals(contextPath)) {
                contextPath = StringUtils.EMPTY;
            }
        }
        return contextPath;
    }
    
    public static void setContextPath(String contextPath) {
        EnvUtil.contextPath = contextPath;
    }

    public static void setIsStandalone(Boolean isStandalone) {
        EnvUtil.isStandalone = isStandalone;
    }
    
    /**
     * Standalone mode or not.
     */
    public static boolean getStandaloneMode() {
        if (Objects.isNull(isStandalone)) {
            isStandalone = Boolean.getBoolean(Constants.STANDALONE_MODE_PROPERTY_NAME);
        }
        return isStandalone;
    }
    
    /**
     * server function mode.
     */
    public static String getFunctionMode() {
        if (StringUtils.isEmpty(functionModeType)) {
            functionModeType = System.getProperty(Constants.FUNCTION_MODE_PROPERTY_NAME);
        }
        return functionModeType;
    }
    
    private static String mmqTmpDir;
    
    public static String getMmqTmpDir() {
        if (StringUtils.isBlank(mmqTmpDir)) {
            mmqTmpDir = Paths.get(getMmqHome(), MMQ_TEMP_DIR_1, MMQ_TEMP_DIR_2).toString();
        }
        return mmqTmpDir;
    }
    
    public static String getMmqHome() {
        if (StringUtils.isBlank(mmqHomePath)) {
            String mmqHome = System.getProperty(MMQ_HOME_KEY);
            if (StringUtils.isBlank(mmqHome)) {
                mmqHome = Paths.get(System.getProperty(MMQ_HOME_PROPERTY), MMQ_HOME_ADDITIONAL_FILEPATH).toString();
            }
            return mmqHome;
        }
        // test-first
        return mmqHomePath;
    }

    public static void setMmqHomePath(String mmqHomePath) {
        EnvUtil.mmqHomePath = mmqHomePath;
    }
    
    public static List<String> getIPsBySystemEnv(String key) {
        String env = getSystemEnv(key);
        List<String> ips = new ArrayList<>();
        if (StringUtils.isNotEmpty(env)) {
            ips = Arrays.asList(env.split(","));
        }
        return ips;
    }
    
    public static String getSystemEnv(String key) {
        return System.getenv(key);
    }
    
    public static float getLoad() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getSystemLoadAverage();
    }
    
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static float getCPU() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getSystemCpuLoad();
    }
    
    public static float getMem() {
        return (float) (1
                - (double) OPERATING_SYSTEM_MX_BEAN.getFreePhysicalMemorySize() / (double) OPERATING_SYSTEM_MX_BEAN
                .getTotalPhysicalMemorySize());
    }

    public static String getPlugsPath() {
        if (StringUtils.isNotBlank(EnvUtil.plugsPath)) {
            return EnvUtil.plugsPath;
        }
        EnvUtil.plugsPath = Paths.get(getMmqHome(), DEFAULT_PLUGS_PATH).toString();
        return plugsPath;
    }

    public static String getConfPath() {
        if (StringUtils.isNotBlank(EnvUtil.confPath)) {
            return EnvUtil.confPath;
        }
        EnvUtil.confPath = Paths.get(getMmqHome(), DEFAULT_ADDITIONAL_PATH).toString();
        return confPath;
    }
    
    public static void setConfPath(final String confPath) {
        EnvUtil.confPath = confPath;
    }
    
    public static String getClusterConfFilePath() {
        return Paths.get(getMmqHome(), DEFAULT_ADDITIONAL_PATH, DEFAULT_ADDITIONAL_FILE).toString();
    }
    
    /**
     * read cluster.conf to ip list.
     *
     * @return ip list.
     * @throws IOException ioexception {@link IOException}
     */
    public static List<String> readClusterConf() throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(new File(getClusterConfFilePath())),
                StandardCharsets.UTF_8)) {
            return analyzeClusterConf(reader);
        } catch (FileNotFoundException ignore) {
            List<String> tmp = new ArrayList<>();
            String clusters = EnvUtil.getMemberList();
            if (StringUtils.isNotBlank(clusters)) {
                String[] details = clusters.split(",");
                for (String item : details) {
                    tmp.add(item.trim());
                }
            }
            return tmp;
        }
    }
    
    /**
     * read file stream to ip list.
     *
     * @param reader reader
     * @return ip list.
     * @throws IOException IOException
     */
    public static List<String> analyzeClusterConf(Reader reader) throws IOException {
        List<String> instanceList = new ArrayList<String>();
        List<String> lines = IoUtils.readLines(reader);
        String comment = "#";
        for (String line : lines) {
            String instance = line.trim();
            if (instance.startsWith(comment)) {
                // # it is ip
                continue;
            }
            if (instance.contains(comment)) {
                // 192.168.71.52:8888 # Instance A
                instance = instance.substring(0, instance.indexOf(comment));
                instance = instance.trim();
            }
            int multiIndex = instance.indexOf(Constants.COMMA_DIVISION);
            if (multiIndex > 0) {
                // support the format: ip1:port,ip2:port  # multi inline
                instanceList.addAll(Arrays.asList(instance.split(Constants.COMMA_DIVISION)));
            } else {
                //support the format: 192.168.71.52:8888
                instanceList.add(instance);
            }
        }
        return instanceList;
    }
    
    public static void writeClusterConf(String content) throws IOException {
        DiskUtils.writeFile(new File(getClusterConfFilePath()), content.getBytes(StandardCharsets.UTF_8), false);
    }
    
    public static String getMemberList() {
        String val;
        if (environment == null) {
            val = System.getenv(MEMBER_LIST_PROPERTY);
            if (StringUtils.isBlank(val)) {
                val = System.getProperty(MEMBER_LIST_PROPERTY);
            }
        } else {
            val = getProperty(MEMBER_LIST_PROPERTY);
        }
        return val;
    }
    
    /**
     * load resource to map.
     *
     * @param resource resource
     * @return Map&lt;String, Object&gt;
     * @throws IOException IOException
     */
    public static Map<String, ?> loadProperties(Resource resource) throws IOException {
        return new OriginTrackedPropertiesLoader(resource).load();
    }
    
    public static Resource getApplicationConfFileResource() {
        Resource customResource = getCustomFileResource();
        return customResource == null ? getDefaultResource() : customResource;
    }
    
    private static Resource getCustomFileResource() {
        String path = getProperty(CUSTOM_CONFIG_LOCATION_PROPERTY);
        if (StringUtils.isNotBlank(path) && path.contains(FILE_PREFIX)) {
            String[] paths = path.split(",", -1);
            path = paths[paths.length - 1].substring(FILE_PREFIX.length());
            return getRelativePathResource(path, DEFAULT_CONFIG_LOCATION);
        }
        return null;
    }
    
    private static Resource getRelativePathResource(String parentPath, String path) {
        try {
            InputStream inputStream = new FileInputStream(Paths.get(parentPath, path).toFile());
            return new InputStreamResource(inputStream);
        } catch (Exception ignore) {
        }
        return null;
    }
    
    private static Resource getDefaultResource() {
        InputStream inputStream = EnvUtil.class.getResourceAsStream(DEFAULT_RESOURCE_PATH);
        return new InputStreamResource(inputStream);
    }
    
    /**
     * Get available processor numbers from environment.
     *
     * <p>
     *     If there are setting of {@code mmq.core.sys.basic.processors} in config/JVM/system, use it.
     *     If no setting, use the one time {@code ThreadUtils.getSuitableThreadCount()}.
     * </p>
     *
     * @return available processor numbers from environment, will not lower than 1.
     */
    public static int getAvailableProcessors() {
        int result = getProperty(Constants.AVAILABLE_PROCESSORS_BASIC, int.class,
                ThreadUtils.getSuitableThreadCount(1));
        return result > 0 ? result : 1;
    }
    
    /**
     * Get a multiple time of available processor numbers from environment.
     *
     * @param multiple multiple of available processor numbers
     * @return available processor numbers from environment, will not lower than 1.
     */
    public static int getAvailableProcessors(int multiple) {
        if (multiple < 1) {
            throw new IllegalArgumentException("processors multiple must upper than 1");
        }
        Integer processor = getProperty(Constants.AVAILABLE_PROCESSORS_BASIC, Integer.class);
        return null != processor && processor > 0 ? processor * multiple : ThreadUtils.getSuitableThreadCount(multiple);
    }
    
    /**
     * Get a scale of available processor numbers from environment.
     *
     * @param scale scale from 0 to 1.
     * @return available processor numbers from environment, will not lower than 1.
     */
    public static int getAvailableProcessors(double scale) {
        if (scale < 0 || scale > 1) {
            throw new IllegalArgumentException("processors scale must between 0 and 1");
        }
        double result = getProperty(Constants.AVAILABLE_PROCESSORS_BASIC, int.class,
                ThreadUtils.getSuitableThreadCount(1)) * scale;
        return result > 1 ? (int) result : 1;
    }
}
