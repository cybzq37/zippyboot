package com.zippy.netty.controller;

import com.zippy.kit.response.ApiResponse;
import com.zippy.netty.service.NettyServerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/netty")
public class NettyServerController {

    private final NettyServerManager nettyServerManager;

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.ok(nettyServerManager.status());
    }
}
