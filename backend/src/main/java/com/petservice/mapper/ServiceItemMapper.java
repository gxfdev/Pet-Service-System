package com.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservice.entity.ServiceItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceItemMapper extends BaseMapper<ServiceItem> {
}
