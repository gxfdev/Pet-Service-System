package com.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservice.entity.IncomeRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IncomeRecordMapper extends BaseMapper<IncomeRecord> {
}
