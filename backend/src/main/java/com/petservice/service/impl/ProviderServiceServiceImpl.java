package com.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petservice.entity.ProviderService;
import com.petservice.mapper.ProviderServiceMapper;
import com.petservice.service.ProviderServiceService;
import org.springframework.stereotype.Service;

@Service
public class ProviderServiceServiceImpl extends ServiceImpl<ProviderServiceMapper, ProviderService> implements ProviderServiceService {
}
