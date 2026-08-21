package com.example.serverinventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;

@Component
public class InetAddressPingClient implements PingClient {

    private static final Logger log = LoggerFactory.getLogger(InetAddressPingClient.class);

    @Override
    public boolean isReachable(String ipAddress, int timeoutMillis) {
        try {
            return InetAddress.getByName(ipAddress).isReachable(timeoutMillis);
        } catch (IOException e) {
            log.debug("Ping failed for {}: {}", ipAddress, e.getMessage());
            return false;
        }
    }
}
