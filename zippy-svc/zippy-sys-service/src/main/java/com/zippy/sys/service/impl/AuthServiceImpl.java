package com.zippy.sys.service.impl;

import com.zippy.api.sys.dto.user.LoginUserDTO;
import com.zippy.api.sys.dto.user.UserDTO;
import com.zippy.api.sys.vo.LoginVO;
import com.zippy.api.sys.vo.UserInfoVO;
import com.zippy.api.sys.dto.permission.MenuTreeDTO;
import com.zippy.infra.satoken.utils.LoginHelper;
import com.zippy.kit.exception.BaseException;
import com.zippy.kit.util.BeanUtils;
import com.zippy.kit.util.IdUtils;
import com.zippy.sys.entity.SysPermission;
import com.zippy.sys.entity.SysUser;
import com.zippy.sys.manager.PermissionManager;
import com.zippy.sys.manager.UserManager;
import com.zippy.sys.service.AuthService;
import com.zippy.sys.service.SysPermissionService;
import com.zippy.sys.service.SysUserService;
import com.zippy.sys.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final SysPermissionService permissionService;

    @Override
    public LoginVO login(String username, String password) {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw BaseException.badRequest("用户名或密码错误");
        }
        if (!PasswordUtils.matches(password, user.getPassword())) {
            throw BaseException.badRequest("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw BaseException.badRequest("账号已被停用或锁定");
        }

        LoginUserDTO loginUserDTO = userManager.getLoginUser(user.getId());
        LoginHelper.login(user.getId(), null, loginUserDTO);

        UserDTO userDTO = BeanUtils.copy(user, UserDTO.class);
        String token = cn.dev33.satoken.stp.StpUtil.getTokenValue();
        return LoginVO.builder().token(token).userInfo(userDTO).build();
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        LoginUserDTO loginUser = LoginHelper.getLoginUser();
        if (loginUser == null) {
            throw BaseException.badRequest("未登录");
        }

        SysUser user = userService.getById(loginUser.getUserId());
        UserDTO userDTO = BeanUtils.copy(user, UserDTO.class);

        List<SysPermission> menus = permissionService.getPermsByUserId(loginUser.getUserId());
        List<MenuTreeDTO> menuTree = menus.stream()
                .filter(p -> p.getPermType() <= 2)
                .map(p -> MenuTreeDTO.builder()
                        .id(p.getId())
                        .parentId(p.getParentId())
                        .permName(p.getPermName())
                        .permType(p.getPermType())
                        .path(p.getPath())
                        .component(p.getComponent())
                        .icon(p.getIcon())
                        .sort(p.getSort())
                        .visible(p.getVisible())
                        .build())
                .collect(Collectors.toList());

        return UserInfoVO.builder()
                .user(userDTO)
                .roles(loginUser.getRoleCodes().stream().sorted().collect(Collectors.toList()))
                .permissions(loginUser.getPermCodes().stream().sorted().collect(Collectors.toList()))
                .menus(menuTree)
                .build();
    }
}
