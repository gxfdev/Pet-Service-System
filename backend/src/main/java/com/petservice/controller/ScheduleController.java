package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petservice.common.Result;
import com.petservice.entity.Schedule;
import com.petservice.entity.User;
import com.petservice.mapper.ScheduleMapper;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserService userService;

    // ==================== 店长：排班管理 ====================

    /**
     * 创建排班
     */
    @PostMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> createSchedule(@RequestBody ScheduleDTO dto, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        // 验证店员属于本店
        User staff = userService.getById(dto.getStaffId());
        if (staff == null || !owner.getId().equals(staff.getParentProviderId())) {
            return Result.error("该店员不属于您的店铺");
        }

        Schedule schedule = new Schedule();
        schedule.setStaffId(dto.getStaffId());
        schedule.setProviderId(owner.getId());
        schedule.setShiftDate(LocalDate.parse(dto.getShiftDate()));
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setShiftType(dto.getShiftType() != null ? dto.getShiftType() : 0);
        schedule.setRemark(dto.getRemark());
        schedule.setStatus(1);

        scheduleMapper.insert(schedule);
        return Result.success();
    }

    /**
     * 批量创建排班（一周排班）
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> batchCreateSchedule(@RequestBody BatchScheduleDTO dto, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        // 验证店员属于本店
        User staff = userService.getById(dto.getStaffId());
        if (staff == null || !owner.getId().equals(staff.getParentProviderId())) {
            return Result.error("该店员不属于您的店铺");
        }

        for (String dateStr : dto.getShiftDates()) {
            // 检查是否已有排班
            Long count = scheduleMapper.selectCount(
                new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getStaffId, dto.getStaffId())
                    .eq(Schedule::getShiftDate, LocalDate.parse(dateStr))
                    .eq(Schedule::getStatus, 1)
            );
            if (count > 0) continue; // 跳过已有排班的日期

            Schedule schedule = new Schedule();
            schedule.setStaffId(dto.getStaffId());
            schedule.setProviderId(owner.getId());
            schedule.setShiftDate(LocalDate.parse(dateStr));
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            schedule.setShiftType(dto.getShiftType() != null ? dto.getShiftType() : 0);
            schedule.setRemark(dto.getRemark());
            schedule.setStatus(1);
            scheduleMapper.insert(schedule);
        }
        return Result.success();
    }

    /**
     * 更新排班
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> updateSchedule(@PathVariable Long id, @RequestBody ScheduleDTO dto, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        Schedule existing = scheduleMapper.selectById(id);
        if (existing == null) return Result.error("排班记录不存在");
        if (!existing.getProviderId().equals(owner.getId())) return Result.error("无权操作此排班");

        if (dto.getShiftDate() != null) existing.setShiftDate(LocalDate.parse(dto.getShiftDate()));
        if (dto.getStartTime() != null) existing.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) existing.setEndTime(dto.getEndTime());
        if (dto.getShiftType() != null) existing.setShiftType(dto.getShiftType());
        if (dto.getRemark() != null) existing.setRemark(dto.getRemark());

        scheduleMapper.updateById(existing);
        return Result.success();
    }

    /**
     * 删除排班（软删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> deleteSchedule(@PathVariable Long id, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        Schedule existing = scheduleMapper.selectById(id);
        if (existing == null) return Result.error("排班记录不存在");
        if (!existing.getProviderId().equals(owner.getId())) return Result.error("无权操作此排班");

        existing.setStatus(0);
        scheduleMapper.updateById(existing);
        return Result.success();
    }

    /**
     * 店长查看本店排班（支持按周/月筛选）
     */
    @GetMapping("/provider")
    @PreAuthorize("hasAnyRole('SHOP_OWNER','ADMIN')")
    public Result<List<Schedule>> getProviderSchedules(Authentication auth,
                                                        @RequestParam(defaultValue = "week") String view,
                                                        @RequestParam(required = false) String date,
                                                        @RequestParam(required = false) Long staffId) {
        User owner = (User) auth.getPrincipal();
        Long providerId = owner.getRole() == 2 ? owner.getId() : owner.getId();

        String[] range = getDateRange(view, date);
        List<Schedule> schedules = scheduleMapper.findByProviderAndDateRange(providerId, range[0], range[1]);

        // 按店员筛选
        if (staffId != null) {
            schedules = schedules.stream().filter(s -> staffId.equals(s.getStaffId())).collect(Collectors.toList());
        }
        return Result.success(schedules);
    }

    /**
     * 店员查看自己的排班
     */
    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('SHOP_OWNER','SHOP_STAFF','PROVIDER')")
    public Result<List<Schedule>> getStaffSchedules(Authentication auth,
                                                     @RequestParam(defaultValue = "week") String view,
                                                     @RequestParam(required = false) String date) {
        User user = (User) auth.getPrincipal();
        Long staffId = user.getStaffRole() != null && user.getStaffRole() == 2 ? user.getId() : null;

        if (staffId == null) {
            // 店长查看全部
            return getProviderSchedules(auth, view, date, null);
        }

        String[] range = getDateRange(view, date);
        List<Schedule> schedules = scheduleMapper.findByStaffAndDateRange(staffId, range[0], range[1]);
        return Result.success(schedules);
    }

    /**
     * 排班统计：每位店员的累计工作时长
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<List<Map<String, Object>>> getScheduleStats(Authentication auth,
                                                               @RequestParam(defaultValue = "month") String view,
                                                               @RequestParam(required = false) String date,
                                                               @RequestParam(defaultValue = "hours") String sortBy) {
        User owner = (User) auth.getPrincipal();
        String[] range = getDateRange(view, date);
        List<Schedule> schedules = scheduleMapper.findByProviderAndDateRange(owner.getId(), range[0], range[1]);

        // 按店员分组统计
        Map<Long, List<Schedule>> grouped = schedules.stream()
            .filter(s -> s.getStatus() == 1)
            .collect(Collectors.groupingBy(Schedule::getStaffId));

        List<Map<String, Object>> stats = new ArrayList<>();
        for (Map.Entry<Long, List<Schedule>> entry : grouped.entrySet()) {
            Long sId = entry.getKey();
            List<Schedule> items = entry.getValue();
            User staff = userService.getById(sId);

            double totalHours = 0;
            for (Schedule s : items) {
                totalHours += calcHours(s.getStartTime(), s.getEndTime());
            }

            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("staffId", sId);
            stat.put("staffName", staff != null ? staff.getRealName() : "未知");
            stat.put("staffPhone", staff != null ? staff.getPhone() : "");
            stat.put("shiftCount", items.size());
            stat.put("totalHours", Math.round(totalHours * 10) / 10.0);
            stats.add(stat);
        }

        // 排序
        if ("hours".equals(sortBy)) {
            stats.sort((a, b) -> Double.compare((Double) b.get("totalHours"), (Double) a.get("totalHours")));
        } else if ("hours_asc".equals(sortBy)) {
            stats.sort((a, b) -> Double.compare((Double) a.get("totalHours"), (Double) b.get("totalHours")));
        } else if ("count".equals(sortBy)) {
            stats.sort((a, b) -> Integer.compare((Integer) b.get("shiftCount"), (Integer) a.get("shiftCount")));
        }

        return Result.success(stats);
    }

    // ==================== 辅助方法 ====================

    private double calcHours(String start, String end) {
        try {
            String[] sp = start.split(":");
            String[] ep = end.split(":");
            double s = Integer.parseInt(sp[0]) + Integer.parseInt(sp[1]) / 60.0;
            double e = Integer.parseInt(ep[0]) + Integer.parseInt(ep[1]) / 60.0;
            return Math.max(0, e - s);
        } catch (Exception ex) {
            return 0;
        }
    }

    private String[] getDateRange(String view, String dateStr) {
        LocalDate base = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        String start, end;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (view) {
            case "day":
                start = end = base.format(fmt);
                break;
            case "week":
                LocalDate weekStart = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                start = weekStart.format(fmt);
                end = weekEnd.format(fmt);
                break;
            case "month":
            default:
                start = base.withDayOfMonth(1).format(fmt);
                end = base.withDayOfMonth(base.lengthOfMonth()).format(fmt);
                break;
        }
        return new String[]{start, end};
    }

    // ==================== DTO ====================

    public static class ScheduleDTO {
        private Long staffId;
        private String shiftDate;
        private String startTime;
        private String endTime;
        private Integer shiftType;
        private String remark;

        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public String getShiftDate() { return shiftDate; }
        public void setShiftDate(String shiftDate) { this.shiftDate = shiftDate; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Integer getShiftType() { return shiftType; }
        public void setShiftType(Integer shiftType) { this.shiftType = shiftType; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class BatchScheduleDTO {
        private Long staffId;
        private List<String> shiftDates;
        private String startTime;
        private String endTime;
        private Integer shiftType;
        private String remark;

        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public List<String> getShiftDates() { return shiftDates; }
        public void setShiftDates(List<String> shiftDates) { this.shiftDates = shiftDates; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Integer getShiftType() { return shiftType; }
        public void setShiftType(Integer shiftType) { this.shiftType = shiftType; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
