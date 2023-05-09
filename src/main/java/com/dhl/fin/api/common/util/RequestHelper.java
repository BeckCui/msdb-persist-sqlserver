package com.dhl.fin.api.common.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

/**
 * Request helper
 */
public class RequestHelper {
    private static final String UNKNOWN_IP = "unknown";
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-FORWARDED-FOR",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    private static String getLocalhostIpAddress(String fallbackIpAddress) {
        String localhostIp = fallbackIpAddress;
        try {
            localhostIp = getLocalHostLANAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return localhostIp;
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     * Ref: https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
     */
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException, SocketException {
        InetAddress candidateAddress = null;
        // Iterate all NICs (network interface cards)...
        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
            // Iterate all IP addresses assigned to each card...
            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                if (!inetAddr.isLoopbackAddress()) {

                    if (inetAddr.isSiteLocalAddress()) {
                        // Found non-loopback site-local address. Return it immediately...
                        return inetAddr;
                    } else if (candidateAddress == null) {
                        // Found non-loopback address, but not necessarily site-local.
                        // Store it as a candidate to be returned if site-local address is not subsequently found...
                        candidateAddress = inetAddr;
                        // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                        // only the first. For subsequent iterations, candidate will be non-null.
                    }
                }
            }
        }

        if (candidateAddress != null) {
            // We did not find a site-local address, but we found some other non-loopback address.
            // Server might have a non-site-local address assigned to its NIC (or it might be running
            // IPv6 which deprecates the "site-local" concept).
            // Return this non-loopback candidate address...
            return candidateAddress;
        }
        // At this point, we did not find a non-loopback address.
        // Fall back to returning whatever InetAddress.getLocalHost() returns...
        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        if (jdkSuppliedAddress == null) {
            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
        }

        return jdkSuppliedAddress;
    }

    private static boolean isLocalhostIpAddress(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }

        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            // LoopbackAddress == 127.0.0.1
            return ipAddress.isAnyLocalAddress() || ipAddress.isLoopbackAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getClientIpAddress(ServerHttpRequest request) {
        if (request == null) {
            return "";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            List<String> headers = request.getHeaders().get(header);
            String ip = headers != null && !headers.isEmpty() ? headers.get(0) : "";
            if (ip != null && ip.length() != 0 && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddress().toString();
    }

    public static String getRequestPayload(HttpServletRequest request) throws IOException {
        String payload = null;
        ContentCachingRequestWrapper wrapper =
                WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            payload = IOUtils.toString(wrapper.getContentAsByteArray(), "UTF-8");
        }

        return payload;
    }
}
