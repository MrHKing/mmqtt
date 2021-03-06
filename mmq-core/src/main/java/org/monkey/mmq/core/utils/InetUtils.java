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

package org.monkey.mmq.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.monkey.mmq.core.env.Constants;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.SlowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import static org.monkey.mmq.core.env.Constants.*;


/**
 * Network card operation tool class.
 *
 * @author solley
 */
public class InetUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(InetUtils.class);
    
    private static String selfIP;
    
    private static boolean useOnlySiteLocalInterface = false;
    
    private static boolean preferHostnameOverIP = false;
    
    private static final List<String> PREFERRED_NETWORKS = new ArrayList<String>();
    
    private static final List<String> IGNORED_INTERFACES = new ArrayList<String>();
    
    static {
        NotifyCenter.registerToSharePublisher(IPChangeEvent.class);
        
        useOnlySiteLocalInterface = Boolean.parseBoolean(EnvUtil.getProperty(USE_ONLY_SITE_INTERFACES));
        
        List<String> networks = EnvUtil.getPropertyList(Constants.PREFERRED_NETWORKS);
        PREFERRED_NETWORKS.addAll(networks);
        
        List<String> interfaces = EnvUtil.getPropertyList(Constants.IGNORED_INTERFACES);
        IGNORED_INTERFACES.addAll(interfaces);
        
        final long delayMs = Long.getLong("mmq.core.inet.auto-refresh", 30_000L);
        
        Runnable ipAutoRefresh = new Runnable() {
            @Override
            public void run() {
                String mmqIP = System.getProperty(MMQ_SERVER_IP);
                if (StringUtils.isBlank(mmqIP)) {
                    mmqIP = EnvUtil.getProperty(IP_ADDRESS);
                }
                if (!StringUtils.isBlank(mmqIP)) {
                    if (!(InternetAddressUtil.isIP(mmqIP) || InternetAddressUtil.isDomain(mmqIP))) {
                        throw new RuntimeException("mmq address " + mmqIP + " is not ip");
                    }
                }
                String tmpSelfIP = mmqIP;
                if (StringUtils.isBlank(tmpSelfIP)) {
                    preferHostnameOverIP = Boolean.getBoolean(SYSTEM_PREFER_HOSTNAME_OVER_IP);
                    
                    if (!preferHostnameOverIP) {
                        preferHostnameOverIP = Boolean.parseBoolean(EnvUtil.getProperty(PREFER_HOSTNAME_OVER_IP));
                    }
                    
                    if (preferHostnameOverIP) {
                        InetAddress inetAddress;
                        try {
                            inetAddress = InetAddress.getLocalHost();
                            if (inetAddress.getHostName().equals(inetAddress.getCanonicalHostName())) {
                                tmpSelfIP = inetAddress.getHostName();
                            } else {
                                tmpSelfIP = inetAddress.getCanonicalHostName();
                            }
                        } catch (UnknownHostException ignore) {
                            LOG.warn("Unable to retrieve localhost");
                        }
                    } else {
                        tmpSelfIP = Objects.requireNonNull(findFirstNonLoopbackAddress()).getHostAddress();
                    }
                }
                if (InternetAddressUtil.PREFER_IPV6_ADDRESSES && !tmpSelfIP.startsWith(InternetAddressUtil.IPV6_START_MARK) && !tmpSelfIP
                        .endsWith(InternetAddressUtil.IPV6_END_MARK)) {
                    tmpSelfIP = InternetAddressUtil.IPV6_START_MARK + tmpSelfIP + InternetAddressUtil.IPV6_END_MARK;
                    if (StringUtils.contains(tmpSelfIP, InternetAddressUtil.PERCENT_SIGN_IN_IPV6)) {
                        tmpSelfIP = tmpSelfIP.substring(0, tmpSelfIP.indexOf(InternetAddressUtil.PERCENT_SIGN_IN_IPV6))
                                + InternetAddressUtil.IPV6_END_MARK;
                    }
                }
                if (!Objects.equals(selfIP, tmpSelfIP) && Objects.nonNull(selfIP)) {
                    IPChangeEvent event = new IPChangeEvent();
                    event.setOldIP(selfIP);
                    event.setNewIP(tmpSelfIP);
                    NotifyCenter.publishEvent(event);
                }
                selfIP = tmpSelfIP;
            }
        };
        
        ipAutoRefresh.run();
    }
    
    public static String getSelfIP() {
        return selfIP;
    }
    
    /**
     * findFirstNonLoopbackAddress.
     *
     * @return {@link InetAddress}
     */
    public static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    LOG.debug("Testing interface: " + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }
                    
                    if (!ignoreInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                            InetAddress address = addrs.nextElement();
                            boolean isLegalIpVersion = InternetAddressUtil.PREFER_IPV6_ADDRESSES ? address instanceof Inet6Address
                                    : address instanceof Inet4Address;
                            if (isLegalIpVersion && !address.isLoopbackAddress() && isPreferredAddress(address)) {
                                LOG.debug("Found non-loopback interface: " + ifc.getDisplayName());
                                result = address;
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error("Cannot get first non-loopback address", ex);
        }
        
        if (result != null) {
            return result;
        }
        
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOG.warn("Unable to retrieve localhost");
        }
        
        return null;
    }
    
    private static boolean isPreferredAddress(InetAddress address) {
        if (useOnlySiteLocalInterface) {
            final boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
                LOG.debug("Ignoring address: " + address.getHostAddress());
            }
            return siteLocalAddress;
        }
        if (PREFERRED_NETWORKS.isEmpty()) {
            return true;
        }
        for (String regex : PREFERRED_NETWORKS) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean ignoreInterface(String interfaceName) {
        for (String regex : IGNORED_INTERFACES) {
            if (interfaceName.matches(regex)) {
                LOG.debug("Ignoring interface: " + interfaceName);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"PMD.ClassNamingShouldBeCamelRule", "checkstyle:AbbreviationAsWordInName"})
    public static class IPChangeEvent extends SlowEvent {
        
        private String oldIP;
        
        private String newIP;
        
        public String getOldIP() {
            return oldIP;
        }
        
        public void setOldIP(String oldIP) {
            this.oldIP = oldIP;
        }
        
        public String getNewIP() {
            return newIP;
        }
        
        public void setNewIP(String newIP) {
            this.newIP = newIP;
        }
        
        @Override
        public String toString() {
            return "IPChangeEvent{" + "oldIP='" + oldIP + '\'' + ", newIP='" + newIP + '\'' + '}';
        }
    }
    
}
