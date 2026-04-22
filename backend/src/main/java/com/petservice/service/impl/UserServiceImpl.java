package com.petservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petservice.common.BusinessException;
import com.petservice.common.ErrorCode;
import com.petservice.dto.LoginDTO;
import com.petservice.dto.RegisterDTO;
import com.petservice.entity.User;
import com.petservice.mapper.UserMapper;
import com.petservice.security.JwtUtil;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        if (user.getStatus() != null && user.getStatus() == 0) throw new BusinessException(ErrorCode.USER_DISABLED);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());
        result.put("staffRole", user.getStaffRole());
        result.put("shopName", user.getShopName());
        result.put("baseSalary", user.getBaseSalary());
        result.put("commissionRate", user.getCommissionRate());
        result.put("realName", user.getRealName());
        result.put("idCard", user.getIdCard());
        result.put("parentProviderId", user.getParentProviderId());
        return result;
    }

    @Override
    public void register(RegisterDTO dto) {
        Long count = count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        count = count(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (count > 0) throw new BusinessException(ErrorCode.PHONE_EXISTS);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : 0);
        user.setStatus(1);
        save(user);
    }

    @Override
    public User getCurrentUser(Long userId) {
        return getById(userId);
    }
}
