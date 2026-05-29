package com.zippyboot.netty.service;

import com.zippyboot.netty.config.NettyServerProperties;
import com.zippyboot.netty.server.NettyServer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServerManager implements ApplicationRunner {

    private final List<NettyServer> servers;
    private final NettyServerProperties properties;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        for (NettyServer server : servers) {
            if ("tcp".equals(server.protocol()) && properties.getTcp().isEnabled()) {
                server.start();
            }
            if ("udp".equals(server.protocol()) && properties.getUdp().isEnabled()) {
                server.start();
            }
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tcpEnabled", properties.getTcp().isEnabled());
        map.put("udpEnabled", properties.getUdp().isEnabled());
        for (NettyServer server : servers) {
            map.put(server.protocol() + "Running", server.isRunning());
        }
        return map;
    }

    @PreDestroy
    public void shutdown() {
        for (NettyServer server : servers) {
            try {
                server.stop();
            } catch (Exception ex) {
                log.warn("Failed to stop {} server", server.protocol(), ex);
            }
        }
    }
}
