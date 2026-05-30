package com.zyn.sys.controller;

import com.zyn.api.sys.dto.user.LoginUserDTO;
import com.zyn.api.sys.dto.user.UserDTO;
import com.zyn.api.sys.vo.LoginVO;
import com.zyn.api.sys.vo.UserInfoVO;
import cn.dev33.satoken.stp.StpUtil;
import com.zyn.kit.response.ApiResponse;
import com.zyn.sys.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestParam String username, @RequestParam String password) {
        LoginVO loginVO = authService.login(username, password);
        return ApiResponse.ok(loginVO);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.ok(null);
    }

    @GetMapping("/info")
    public ApiResponse<UserInfoVO> info() {
        UserInfoVO userInfo = authService.getCurrentUserInfo();
        return ApiResponse.ok(userInfo);
    }
}
