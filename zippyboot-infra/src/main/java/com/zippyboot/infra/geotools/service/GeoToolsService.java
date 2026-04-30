package com.zippyboot.infra.geotools.service;

import com.zippyboot.infra.geotools.config.GeoToolsConfig;
import lombok.RequiredArgsConstructor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoToolsService {

    private final GeoToolsConfig config;

    public String toWkt(String epsgCode) throws Exception {
        CoordinateReferenceSystem crs = CRS.decode(epsgCode, true);
        return crs.toWKT();
    }

    public String defaultTargetWkt() throws Exception {
        return toWkt(config.getDefaultTargetEpsg());
    }
}
