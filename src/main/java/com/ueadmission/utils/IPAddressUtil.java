package com.ueadmission.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for getting client IP address information
 */
public class IPAddressUtil {
    private static final Logger LOGGER = Logger.getLogger(IPAddressUtil.class.getName());
    
    /**
     * Get the local IP address of the client machine
     * 
     * @return The IP address as a string, or "Unknown" if it cannot be determined
     */
    public static String getClientIPAddress() {
        try {
            // Try to get the primary non-loopback address
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // Skip inactive interfaces and loopback
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                
                // Look for a suitable IP address (prefer IPv4)
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress()) {
                        String ipAddress = address.getHostAddress();
                        // Prefer IPv4 addresses (they're more readable)
                        if (!ipAddress.contains(":")) {
                            LOGGER.info("Detected client IP address: " + ipAddress);
                            return ipAddress;
                        }
                    }
                }
            }
            
            // Fallback to localhost if no suitable address found
            return InetAddress.getLocalHost().getHostAddress();
            
        } catch (SocketException | java.net.UnknownHostException e) {
            LOGGER.log(Level.WARNING, "Failed to determine client IP address", e);
            return "Unknown";
        }
    }
    
    /**
     * Get the client's hostname
     * 
     * @return The hostname as a string, or "Unknown" if it cannot be determined
     */
    public static String getClientHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            LOGGER.log(Level.WARNING, "Failed to determine client hostname", e);
            return "Unknown";
        }
    }
}