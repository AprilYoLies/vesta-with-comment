package com.robert.vesta.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

// 这才是获取 ip 的合理方式，相对于美团的 leaf，优势要好很多
public class IpUtils {
    private static final Logger log = LoggerFactory.getLogger(IpUtils.class);

    public static String getHostIp() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();    // 这里是获取所有的可用网卡信息
            while (en.hasMoreElements()) {  // 对每一个网卡进行处理
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();  // 获取当前网卡绑定的 ip
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr
                            .nextElement();
                    if (!inetAddress.isLoopbackAddress()    // 查看当前 ip 是否是本地回环地址
                            && !inetAddress.isLinkLocalAddress()    // 链路本地地址
                            && inetAddress.isSiteLocalAddress()) {  // 这个算是公网地址？？
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Fail to get IP address.", e);
        }
        return ip;
    }

    public static String getHostName() {
        String hostName = null;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr
                            .nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()
                            && inetAddress.isSiteLocalAddress()) {
                        hostName = inetAddress.getHostName();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Fail to get host name.", e);
        }
        return hostName;
    }
}
