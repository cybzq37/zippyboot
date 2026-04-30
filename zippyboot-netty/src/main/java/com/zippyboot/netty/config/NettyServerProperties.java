package com.zippyboot.netty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.netty")
public class NettyServerProperties {

    private Tcp tcp = new Tcp();
    private Udp udp = new Udp();

    @Data
    public static class Tcp {
        private boolean enabled = true;
        private String host = "0.0.0.0";
        private int port = 19090;
    }

    @Data
    public static class Udp {
        private boolean enabled = true;
        private String host = "0.0.0.0";
        private int port = 19091;
    }
}
