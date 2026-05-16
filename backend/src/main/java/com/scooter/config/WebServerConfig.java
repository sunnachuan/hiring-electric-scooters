package com.scooter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;

@Component
public class WebServerConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final Logger log = LoggerFactory.getLogger(WebServerConfig.class);
    private static final int PORT_SCAN_RANGE = 10;

    @Value("${server.port:8080}")
    private int configuredPort;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if (configuredPort <= 0) {
            return;
        }

        int availablePort = findAvailablePort(configuredPort);
        if (availablePort != configuredPort) {
            log.warn("端口 {} 已被占用，自动切换至端口 {}", configuredPort, availablePort);
            factory.setPort(availablePort);
        }
    }

    private int findAvailablePort(int startPort) {
        for (int port = startPort; port < startPort + PORT_SCAN_RANGE; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return startPort + PORT_SCAN_RANGE;
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}