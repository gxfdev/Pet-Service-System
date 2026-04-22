package com.petservice.controller;

import com.petservice.common.Result;
import com.petservice.dto.LoginDTO;
import com.petservice.dto.RegisterDTO;
import com.petservice.entity.User;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        Map<String, Object> data = userService.login(dto);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/current")
    public Result<User> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // 清除敏感信息
        user.setPassword(null);
        return Result.success(user);
    }
}
