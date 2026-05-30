package com.zyn.sys.service;

import com.zyn.api.sys.vo.LoginVO;
import com.zyn.api.sys.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(String username, String password);

    UserInfoVO getCurrentUserInfo();
}
