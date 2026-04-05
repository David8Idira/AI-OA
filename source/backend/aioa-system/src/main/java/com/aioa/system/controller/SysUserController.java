package com.aioa.system.controller;

import com.aioa.common.annotation.Login;
import com.aioa.common.result.Result;
import com.aioa.system.dto.LoginDTO;
import com.aioa.system.dto.RegisterDTO;
import com.aioa.system.dto.UpdatePasswordDTO;
import com.aioa.system.entity.SysUser;
import com.aioa.system.service.SysUserService;
import com.aioa.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
public class SysUserController {

    private final SysUserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success(userVO);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public Result<SysUser> register(@Valid @RequestBody RegisterDTO registerDTO) {
        SysUser user = userService.register(
            registerDTO.getUsername(),
            registerDTO.getPassword(),
            registerDTO.getNickname()
        );
        return Result.success(user);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current user info")
    @Login
    public Result<UserVO> getCurrentUser(@RequestAttribute("userId") String userId) {
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @PutMapping("/password")
    @Operation(summary = "Update password")
    @Login
    public Result<Void> updatePassword(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody UpdatePasswordDTO dto) {
        boolean success = userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return success ? Result.success() : Result.error();
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get user permissions")
    @Login
    public Result<?> getUserPermissions(@RequestAttribute("userId") String userId) {
        return Result.success(userService.getUserPermissions(userId));
    }

    @GetMapping("/menus")
    @Operation(summary = "Get user menus")
    @Login
    public Result<?> getUserMenus(@RequestAttribute("userId") String userId) {
        return Result.success(userService.getUserMenus(userId));
    }
}
