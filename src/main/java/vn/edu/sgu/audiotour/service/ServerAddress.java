package vn.edu.sgu.audiotour.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Địa chỉ LAN của máy chủ, dùng để dựng URL trong mã QR cho điện thoại quét.
 */
@Slf4j
@Component
public class ServerAddress {

    public String detect() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        // Bỏ qua dải host-only của VirtualBox
                        if (!ip.startsWith("192.168.56.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không dò được IP LAN của máy chủ: {}", e.getMessage());
        }
        return "192.168.1.10";
    }
}
