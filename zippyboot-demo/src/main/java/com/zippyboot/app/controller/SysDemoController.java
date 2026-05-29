package com.zippyboot.app.controller;

import com.zippyboot.api.sys.client.RemoteUserService;
import com.zippyboot.api.sys.dto.user.UserDTO;
import com.zippyboot.api.sys.vo.UserInfoVO;
import com.zippyboot.kit.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sys-demo")
public class SysDemoController {

    private final RemoteUserService remoteUserService;

    @GetMapping("/user/{id}")
    public ApiResponse<UserDTO> getUser(@PathVariable String id) {
        return remoteUserService.getById(id);
    }

    @GetMapping("/user/info")
    public ApiResponse<UserInfoVO> getUserInfo() {
        return remoteUserService.getUserInfo();
    }
}
