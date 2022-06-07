package com.mxny.ss.util;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;

/**
 * 网络工具
 * @description:
 * @author: WM
 * @time: 2020/9/28 16:46
 */
public class NetworkUtils {
    /**
     * 获取当前机器端口号
     *
     */
    public static String getLocalPort() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("*:type=Connector,*"), null);
            if (objectNames == null || objectNames.size() <= 0) {
                throw new IllegalStateException("Cannot get the names of MBeans controlled by the MBean server.");
            }
            for (ObjectName objectName : objectNames) {
                String protocol = String.valueOf(mBeanServer.getAttribute(objectName, "protocol"));
                String port = String.valueOf(mBeanServer.getAttribute(objectName, "port"));
                // windows下属性名称为HTTP/1.1, linux下为org.apache.coyote.http11.Http11NioProtocol
                if (protocol.equals("HTTP/1.1") || protocol.equals("org.apache.coyote.http11.Http11NioProtocol")) {
                    return port;
                }
            }
            return "";
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get the HTTP port of the current server,"+e.getMessage());
        }
    }

    /**
     * 获取当前机器的IP
     */
    public static String getLocalIP(){
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to get the IP of the current server,"+e.getMessage());
        }
        byte[] ipAddr = addr.getAddress();
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    /**
     * 代码来自rocketmq的工具类
     * @return
     */
    public static String getLocalAddress() {
        try {
            // Traversal Network interface to get the first non-loopback and non-private address
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList<String> ipv4Result = new ArrayList<String>();
            ArrayList<String> ipv6Result = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        } else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }
            // prefer ipv4
            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }
                    return ip;
                }
                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }
            //If failed to find,fall back to localhost
            final InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        } catch (Exception e) {
            System.out.println("Failed to obtain local address,"+e.getMessage());
        }
        return null;
    }

    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }

}
