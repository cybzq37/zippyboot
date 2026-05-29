package com.zippy.api.sys.client;

import com.zippy.api.sys.dto.user.UserDTO;
import com.zippy.api.sys.vo.UserInfoVO;
import com.zippy.kit.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/v1/user")
public interface RemoteUserService {

    @GetExchange("/{id}")
    ApiResponse<UserDTO> getById(@PathVariable String id);

    @GetExchange("/info")
    ApiResponse<UserInfoVO> getUserInfo();
}
