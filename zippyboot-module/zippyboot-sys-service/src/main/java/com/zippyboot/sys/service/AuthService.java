package com.zippyboot.sys.service;

import com.zippyboot.api.sys.vo.LoginVO;
import com.zippyboot.api.sys.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(String username, String password);

    UserInfoVO getCurrentUserInfo();
}
