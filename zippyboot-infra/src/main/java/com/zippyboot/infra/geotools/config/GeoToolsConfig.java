package com.zippyboot.infra.geotools.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zippyboot.infra.geotools")
public class GeoToolsConfig {

    private boolean enabled = true;
    private String defaultSourceEpsg = "EPSG:4326";
    private String defaultTargetEpsg = "EPSG:3857";
}
