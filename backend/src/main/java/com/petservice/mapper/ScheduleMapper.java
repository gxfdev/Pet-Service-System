package com.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservice.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Select("SELECT s.*, u.real_name as staff_name, u.phone as staff_phone " +
            "FROM schedule s LEFT JOIN user u ON s.staff_id = u.id " +
            "WHERE s.provider_id = #{providerId} AND s.shift_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY s.shift_date ASC, s.start_time ASC")
    List<Schedule> findByProviderAndDateRange(@Param("providerId") Long providerId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    @Select("SELECT s.*, u.real_name as staff_name, u.phone as staff_phone " +
            "FROM schedule s LEFT JOIN user u ON s.staff_id = u.id " +
            "WHERE s.staff_id = #{staffId} AND s.shift_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY s.shift_date ASC, s.start_time ASC")
    List<Schedule> findByStaffAndDateRange(@Param("staffId") Long staffId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);
}
