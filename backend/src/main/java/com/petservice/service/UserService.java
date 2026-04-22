package com.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petservice.dto.LoginDTO;
import com.petservice.dto.RegisterDTO;
import com.petservice.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getCurrentUser(Long userId);
}
