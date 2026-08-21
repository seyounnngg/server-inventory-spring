package com.example.serverinventory.service;

public interface PingClient {

    boolean isReachable(String ipAddress, int timeoutMillis);
}
