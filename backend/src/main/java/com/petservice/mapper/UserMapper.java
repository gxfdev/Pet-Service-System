package com.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
