package com.zyn.api.sys.client;

import com.zyn.api.sys.dto.user.UserDTO;
import com.zyn.api.sys.vo.UserInfoVO;
import com.zyn.infra.discovery.ServiceClient;
import com.zyn.kit.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@ServiceClient("sys")
@HttpExchange("/api/v1/user")
public interface RemoteUserService {

    @GetExchange("/{id}")
    ApiResponse<UserDTO> getById(@PathVariable String id);

    @GetExchange("/info")
    ApiResponse<UserInfoVO> getUserInfo();
}
