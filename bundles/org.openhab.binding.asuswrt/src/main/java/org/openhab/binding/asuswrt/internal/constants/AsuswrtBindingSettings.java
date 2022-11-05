/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.asuswrt.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AsuswrtBindingSettings} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtBindingSettings {

    // Binding Settings
    public static final Integer HTTP_MAX_CONNECTIONS = 10; // setMaxConnectionsPerDestination for HTTP-Client
    public static final Integer HTTP_MAX_QUEUED_REQUESTS = 10; // setMaxRequestsQueuedPerDestination for HTTP-Client
    public static final Integer HTTP_TIMEOUT_MS = 5000; // http request timeout
    public static final Integer HTTP_QUERY_MIN_GAP_MS = 5000; // http minimun gap between query data requests
    public static final String HTTP_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String HTTP_USER_AGENT = "asusrouter-Android-DUTUtil-1.0.0.3.58-163";
    public static final String HTTP_CONTENT_CHARSET = "utf-8";
    public static final String HTTP_PROTOCOL = "http://";

    public static final Integer COOKIE_LIFETIME_S = 3600; // lifetime of login-cookie
    public static final Integer POLLING_INTERVAL_S_MIN = 10; // minimum polling interval
    public static final Integer POLLING_INTERVAL_S_DEFAULT = 30; // default polling interval
    public static final Integer DISCOVERY_TIMEOUT_S = 10; // discovery service timeout

    // List of device commands
    public static final String CMD_GET_SYSINFO = "nvram_get(productid);nvram_get(firmver);nvram_get(buildno);nvram_get(extendno);nvram_get(lan_hwaddr);";
    public static final String CMD_GET_LANINFO = "nvram_get(lan_hwaddr);nvram_get(lan_ipaddr);nvram_get(lan_proto);nvram_get(lan_netmask);nvram_get(lan_gateway);";
    public static final String CMD_GET_WANINFO = "wanlink(status);wanlink(type);wanlink(ipaddr);wanlink(netmask);wanlink(gateway);wanlink(dns);wanlink(lease);wanlink(expires);";
    public static final String CMD_GET_CLIENTLIST = "get_clientlist();";
    public static final String CMD_GET_TRAFFIC = "netdev(appobj);";
    public static final String CMD_GET_UPTIME = "uptime();";
    public static final String CMD_GET_MEMUSAGE = "memory_usage();";
    public static final String CMD_GET_CPUUSAGE = "cpu_usage();";
}
