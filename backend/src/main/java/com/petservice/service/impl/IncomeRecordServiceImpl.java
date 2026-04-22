package com.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petservice.entity.IncomeRecord;
import com.petservice.mapper.IncomeRecordMapper;
import com.petservice.service.IncomeRecordService;
import org.springframework.stereotype.Service;

@Service
public class IncomeRecordServiceImpl extends ServiceImpl<IncomeRecordMapper, IncomeRecord> implements IncomeRecordService {
}
