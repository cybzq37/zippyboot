package com.zippy.sys.service;

import com.zippy.api.sys.vo.LoginVO;
import com.zippy.api.sys.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(String username, String password);

    UserInfoVO getCurrentUserInfo();
}
