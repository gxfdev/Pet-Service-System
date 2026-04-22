package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petservice.common.BusinessException;
import com.petservice.common.Result;
import com.petservice.entity.IncomeRecord;
import com.petservice.entity.OrderInfo;
import com.petservice.entity.Schedule;
import com.petservice.entity.User;
import com.petservice.mapper.ScheduleMapper;
import com.petservice.service.IncomeRecordService;
import com.petservice.service.OrderService;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收益管理系统（双薪资方案 + 实时收入）
 * 
 * 薪资方案：
 * 1. 提成制 (salaryMode=0): 每单固定提成 + 底薪
 * 2. 时薪制 (salaryMode=1): 正常时薪18元/h + 加班时薪25元/h
 */
@RestController
@RequestMapping("/api/income")
public class IncomeController {

    @Autowired
    private IncomeRecordService incomeRecordService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private ScheduleMapper scheduleMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("18");
    private static final BigDecimal DEFAULT_OVERTIME_RATE = new BigDecimal("25");

    // ==================== 实时收入（今日） ====================

    /**
     * 获取今日实时收入（店长和店员共用）
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN','SHOP_OWNER','SHOP_STAFF')")
    public Result<Map<String, Object>> todayIncome(Authentication auth) {
        User user = (User) auth.getPrincipal();
        boolean isOwner = isShopOwner(user);
        Map<String, Object> result = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atTime(0, 0);
        LocalDateTime dayEnd = today.atTime(23, 59);

        if (isOwner) {
            // 店长：今日店铺总收入
            List<OrderInfo> todayOrders = orderService.list(
                new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getProviderId, user.getId())
                    .ge(OrderInfo::getCreateTime, dayStart)
                    .le(OrderInfo::getCreateTime, dayEnd)
            );
            BigDecimal completed = BigDecimal.ZERO;
            BigDecimal pending = BigDecimal.ZERO;
            int completedCount = 0;
            int pendingCount = 0;
            for (OrderInfo o : todayOrders) {
                if (o.getStatus() == 3) {
                    completed = completed.add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
                    completedCount++;
                } else if (o.getStatus() == 1 || o.getStatus() == 2) {
                    pending = pending.add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
                    pendingCount++;
                }
            }
            result.put("completedIncome", completed.setScale(2, RoundingMode.HALF_UP));
            result.put("pendingIncome", pending.setScale(2, RoundingMode.HALF_UP));
            result.put("totalIncome", completed.add(pending).setScale(2, RoundingMode.HALF_UP));
            result.put("completedCount", completedCount);
            result.put("pendingCount", pendingCount);
            result.put("totalOrders", todayOrders.size());
            result.put("role", "owner");
        } else {
            // 店员：今日个人收入
            List<OrderInfo> myOrders = orderService.list(
                new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getStaffId, user.getId())
                    .ge(OrderInfo::getCreateTime, dayStart)
                    .le(OrderInfo::getCreateTime, dayEnd)
            );

            int completedCount = 0;
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (OrderInfo o : myOrders) {
                if (o.getStatus() == 3) completedCount++;
                orderTotal = orderTotal.add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            }

            // 计算今日工时和收入
            double todayHours = calcTodayHours(user.getId(), today);
            BigDecimal todayEarnings = calcStaffEarnings(user, todayHours);

            result.put("todayHours", Math.round(todayHours * 10) / 10.0);
            result.put("todayEarnings", todayEarnings.setScale(2, RoundingMode.HALF_UP));
            result.put("completedCount", completedCount);
            result.put("totalOrders", myOrders.size());
            result.put("orderTotal", orderTotal.setScale(2, RoundingMode.HALF_UP));
            result.put("salaryMode", user.getSalaryMode() != null ? user.getSalaryMode() : 0);
            result.put("hourlyRate", user.getHourlyRate() != null ? user.getHourlyRate() : DEFAULT_HOURLY_RATE);
            result.put("overtimeRate", user.getOvertimeRate() != null ? user.getOvertimeRate() : DEFAULT_OVERTIME_RATE);
            result.put("role", "staff");
        }

        return Result.success(result);
    }

    // ==================== 收入统计 ====================

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN','SHOP_OWNER','SHOP_STAFF')")
    public Result<Map<String, Object>> statistics(Authentication auth) {
        User user = (User) auth.getPrincipal();
        Long providerId = user.getId();
        boolean isOwner = isShopOwner(user);
        Map<String, Object> stats = new HashMap<>();

        if (!isOwner) {
            // ===== 店员视角 =====
            List<IncomeRecord> myRecords = incomeRecordService.list(
                new LambdaQueryWrapper<IncomeRecord>()
                    .eq(IncomeRecord::getStaffId, user.getId())
            );
            BigDecimal totalCommission = BigDecimal.ZERO;
            BigDecimal settledCommission = BigDecimal.ZERO;
            int completedOrders = 0;
            for (IncomeRecord r : myRecords) {
                BigDecimal commission = r.getStaffShare() != null ? r.getStaffShare() : BigDecimal.ZERO;
                totalCommission = totalCommission.add(commission);
                if (r.getStatus() == 2) { settledCommission = settledCommission.add(commission); completedOrders++; }
            }

            // 根据薪资模式计算
            int salaryMode = user.getSalaryMode() != null ? user.getSalaryMode() : 1;
            if (salaryMode == 1) {
                // 时薪制
                double monthHours = calcMonthHours(user.getId(), YearMonth.now());
                BigDecimal earnings = calcStaffEarnings(user, monthHours);
                stats.put("salaryMode", 1);
                stats.put("salaryModeLabel", "时薪制");
                stats.put("monthHours", Math.round(monthHours * 10) / 10.0);
                stats.put("hourlyRate", user.getHourlyRate() != null ? user.getHourlyRate() : DEFAULT_HOURLY_RATE);
                stats.put("overtimeRate", user.getOvertimeRate() != null ? user.getOvertimeRate() : DEFAULT_OVERTIME_RATE);
                stats.put("totalIncome", earnings.setScale(2, RoundingMode.HALF_UP));
            } else if (salaryMode == 2) {
                // 固定月薪
                BigDecimal fixedSalary = user.getFixedMonthlySalary() != null ? user.getFixedMonthlySalary() : BigDecimal.ZERO;
                stats.put("salaryMode", 2);
                stats.put("salaryModeLabel", "固定月薪");
                stats.put("fixedMonthlySalary", fixedSalary.setScale(2, RoundingMode.HALF_UP));
                stats.put("totalIncome", fixedSalary.setScale(2, RoundingMode.HALF_UP));
            } else {
                // 提成制：无底薪，仅按单提成
                stats.put("salaryMode", 0);
                stats.put("salaryModeLabel", "提成制");
                stats.put("commissionRate", user.getCommissionRate() != null ? user.getCommissionRate() : BigDecimal.ZERO);
                stats.put("totalIncome", totalCommission.setScale(2, RoundingMode.HALF_UP));
            }
            stats.put("totalCommission", totalCommission.setScale(2, RoundingMode.HALF_UP));
            stats.put("settledCommission", settledCommission.setScale(2, RoundingMode.HALF_UP));
            stats.put("completedOrders", completedOrders);
            stats.put("viewScope", "personal");
            stats.put("staffName", user.getRealName() != null ? user.getRealName() : user.getUsername());
        } else {
            // ===== 店长视角 =====
            List<IncomeRecord> shopRecords = incomeRecordService.list(
                new LambdaQueryWrapper<IncomeRecord>().eq(IncomeRecord::getProviderId, providerId)
            );
            BigDecimal totalOrderAmount = BigDecimal.ZERO, totalShopIncome = BigDecimal.ZERO, totalStaffCost = BigDecimal.ZERO;
            long completedOrders = orderService.count(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getProviderId, providerId).eq(OrderInfo::getStatus, 3)
            );
            for (IncomeRecord r : shopRecords) {
                totalOrderAmount = totalOrderAmount.add(r.getOrderAmount() != null ? r.getOrderAmount() : BigDecimal.ZERO);
                totalShopIncome = totalShopIncome.add(r.getShopShare() != null ? r.getShopShare() : BigDecimal.ZERO);
                totalStaffCost = totalStaffCost.add(r.getStaffShare() != null ? r.getStaffShare() : BigDecimal.ZERO);
            }
            List<User> staffs = userService.list(
                new LambdaQueryWrapper<User>().eq(User::getParentProviderId, providerId).eq(User::getStaffRole, 2)
            );
            stats.put("totalRevenue", totalOrderAmount.setScale(2, RoundingMode.HALF_UP));
            stats.put("shopIncome", totalShopIncome.setScale(2, RoundingMode.HALF_UP));
            stats.put("totalStaffCost", totalStaffCost.setScale(2, RoundingMode.HALF_UP));
            stats.put("completedOrders", completedOrders);
            stats.put("staffCount", staffs.size());
            stats.put("viewScope", "shop");
        }
        return Result.success(stats);
    }

    // ==================== 收入记录 ====================

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN','SHOP_OWNER','SHOP_STAFF')")
    public Result<?> listRecords(Authentication auth,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) Integer status) {
        User user = (User) auth.getPrincipal();
        Page<IncomeRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IncomeRecord> wrapper = new LambdaQueryWrapper<>();
        if (!isShopOwner(user)) wrapper.eq(IncomeRecord::getStaffId, user.getId());
        else wrapper.eq(IncomeRecord::getProviderId, user.getId());
        if (status != null) wrapper.eq(IncomeRecord::getStatus, status);
        wrapper.orderByDesc(IncomeRecord::getCreateTime);
        Page<IncomeRecord> result = incomeRecordService.page(pageParam, wrapper);
        return Result.success(result);
    }

    // ==================== 分配收入 ====================

    @PostMapping("/allocate")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<IncomeRecord> allocate(@RequestBody AllocateDTO dto, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        OrderInfo order = orderService.getById(dto.getOrderId());
        if (order == null) return Result.error("订单不存在");
        if (!order.getProviderId().equals(owner.getId())) return Result.error("非本店订单");
        if (order.getStatus() < 1) return Result.error("该订单尚未支付");
        if (incomeRecordService.count(new LambdaQueryWrapper<IncomeRecord>().eq(IncomeRecord::getOrderId, dto.getOrderId())) > 0)
            return Result.error("该订单已分配过收入");

        User staff = userService.getById(dto.getStaffId());
        if (staff == null) return Result.error("员工不存在");
        if (!owner.getId().equals(staff.getParentProviderId()) && !owner.getId().equals(staff.getId()))
            return Result.error("该员工不属于您的店铺");

        int salaryMode = staff.getSalaryMode() != null ? staff.getSalaryMode() : 1;
        BigDecimal staffShare, shopShare;

        Map<String, Object> calcDetail = new LinkedHashMap<>();
        calcDetail.put("orderAmount", order.getTotalAmount());
        calcDetail.put("salaryMode", salaryMode);
        calcDetail.put("staffName", staff.getRealName() != null ? staff.getRealName() : staff.getUsername());

        if (salaryMode == 1) {
            // 时薪制：根据实际工作时长计算
            double hours = 1.0;
            if (order.getStartTime() != null && order.getEndTime() != null) {
                hours = parseTime(order.getEndTime()) - parseTime(order.getStartTime());
            }
            boolean isOvertime = isOvertime(order);
            BigDecimal rate = isOvertime
                ? (staff.getOvertimeRate() != null ? staff.getOvertimeRate() : DEFAULT_OVERTIME_RATE)
                : (staff.getHourlyRate() != null ? staff.getHourlyRate() : DEFAULT_HOURLY_RATE);
            staffShare = rate.multiply(new BigDecimal(hours)).setScale(2, RoundingMode.HALF_UP);
            shopShare = order.getTotalAmount().subtract(staffShare).setScale(2, RoundingMode.HALF_UP);
            calcDetail.put("hours", hours);
            calcDetail.put("isOvertime", isOvertime);
            calcDetail.put("rate", rate);
            calcDetail.put("formula", String.format("员工收益 = %s时薪 ¥%s × %.1fh = ¥%s", isOvertime?"加班":"正常", rate, hours, staffShare));
        } else {
            // 提成制
            BigDecimal commissionPerOrder = dto.getCommissionPerOrder();
            if (commissionPerOrder == null)
                commissionPerOrder = staff.getCommissionRate() != null ? staff.getCommissionRate() : BigDecimal.ZERO;
            staffShare = commissionPerOrder;
            shopShare = order.getTotalAmount().subtract(staffShare).setScale(2, RoundingMode.HALF_UP);
            calcDetail.put("commissionPerOrder", commissionPerOrder);
            calcDetail.put("formula", String.format("员工收益 = 固定提成 ¥%s", commissionPerOrder));
        }
        calcDetail.put("staffShare", staffShare);
        calcDetail.put("shopShare", shopShare);

        String calcDetailJson;
        try { calcDetailJson = objectMapper.writeValueAsString(calcDetail); } catch (Exception e) { calcDetailJson = "{}"; }

        IncomeRecord record = new IncomeRecord();
        record.setOrderId(dto.getOrderId());
        record.setProviderId(owner.getId());
        record.setStaffId(dto.getStaffId());
        record.setOrderAmount(order.getTotalAmount());
        record.setCommissionPerOrder(dto.getCommissionPerOrder());
        record.setStaffShare(staffShare);
        record.setShopShare(shopShare);
        record.setStatus(1);
        record.setRemark(dto.getRemark());
        record.setAssignedBy(owner.getId());
        record.setCalcDetail(calcDetailJson);
        incomeRecordService.save(record);
        return Result.success(record);
    }

    // ==================== 批量结算 ====================

    @PutMapping("/settle")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Map<String, Object>> batchSettle(@RequestBody List<Long> ids, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        int settled = 0;
        for (Long id : ids) {
            IncomeRecord record = incomeRecordService.getById(id);
            if (record != null && record.getProviderId().equals(owner.getId()) && record.getStatus() == 1) {
                record.setStatus(2);
                incomeRecordService.updateById(record);
                settled++;
            }
        }
        return Result.success(Map.of("settledCount", settled));
    }

    // ==================== 员工薪资配置（双方案） ====================

    @GetMapping("/staff-list")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<List<User>> getStaffList(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        List<User> staffs = userService.list(
            new LambdaQueryWrapper<User>().eq(User::getParentProviderId, owner.getId()).eq(User::getStaffRole, 2)
        );
        staffs.forEach(s -> s.setPassword(null));
        return Result.success(staffs);
    }

    /**
     * 设置员工薪资方案
     * salaryMode=0 提成制: baseSalary + commissionRate
     * salaryMode=1 时薪制: hourlyRate(18/h) + overtimeRate(25/h)
     * salaryMode=2 固定月薪: fixedMonthlySalary
     */
    @PutMapping("/staff-salary")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> updateStaffSalary(@RequestBody SalaryDTO dto, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        User staff = userService.getById(dto.getStaffId());
        if (staff == null) return Result.error("员工不存在");
        if (!owner.getId().equals(staff.getParentProviderId())) return Result.error("该员工不属于您的店铺");

        staff.setSalaryMode(dto.getSalaryMode() != null ? dto.getSalaryMode() : 0);

        if (dto.getSalaryMode() != null && dto.getSalaryMode() == 1) {
            // 时薪制
            staff.setHourlyRate(dto.getHourlyRate() != null ? dto.getHourlyRate() : DEFAULT_HOURLY_RATE);
            staff.setOvertimeRate(dto.getOvertimeRate() != null ? dto.getOvertimeRate() : DEFAULT_OVERTIME_RATE);
        } else if (dto.getSalaryMode() != null && dto.getSalaryMode() == 2) {
            // 固定月薪
            staff.setFixedMonthlySalary(dto.getFixedMonthlySalary());
        } else {
            // 提成制
            staff.setBaseSalary(dto.getBaseSalary());
            staff.setCommissionRate(dto.getCommissionRate());
        }
        userService.updateById(staff);
        return Result.success();
    }

    // ==================== 月度汇总 ====================

    @GetMapping("/monthly-summary")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN','SHOP_OWNER','SHOP_STAFF')")
    public Result<?> monthlySummary(Authentication auth,
                                     @RequestParam(required = false) Integer year,
                                     @RequestParam(required = false) Integer month,
                                     @RequestParam(required = false) Long staffId) {
        User user = (User) auth.getPrincipal();
        boolean isOwner = isShopOwner(user);
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        List<Map<String, Object>> summaryList = new ArrayList<>();
        if (!isOwner) {
            summaryList.add(buildStaffMonthlySummary(user.getId(), year, month));
        } else {
            List<User> staffs;
            if (staffId != null) staffs = Collections.singletonList(userService.getById(staffId));
            else staffs = userService.list(new LambdaQueryWrapper<User>().eq(User::getParentProviderId, user.getId()).eq(User::getStaffRole, 2));
            for (User s : staffs) { if (s != null) summaryList.add(buildStaffMonthlySummary(s.getId(), year, month)); }
        }
        return Result.success(summaryList);
    }

    private Map<String, Object> buildStaffMonthlySummary(Long staffId, int year, int month) {
        User staff = userService.getById(staffId);
        if (staff == null) {
            throw new BusinessException(404, "员工不存在");
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        String staffName = (staff.getRealName() != null ? staff.getRealName() : staff.getUsername());
        int salaryMode = staff.getSalaryMode() != null ? staff.getSalaryMode() : 1;
        YearMonth ym = YearMonth.of(year, month);

        List<IncomeRecord> records = incomeRecordService.list(
            new LambdaQueryWrapper<IncomeRecord>()
                .eq(IncomeRecord::getStaffId, staffId)
                .ge(IncomeRecord::getCreateTime, ym.atDay(1).atStartOfDay())
                .le(IncomeRecord::getCreateTime, ym.atEndOfMonth().atTime(23, 59, 59))
        );

        BigDecimal totalCommission = BigDecimal.ZERO;
        for (IncomeRecord r : records) {
            totalCommission = totalCommission.add(r.getStaffShare() != null ? r.getStaffShare() : BigDecimal.ZERO);
        }

        double monthHours = calcMonthHours(staffId, ym);
        BigDecimal totalIncome;

        if (salaryMode == 1) {
            BigDecimal hourlyRate = staff.getHourlyRate() != null ? staff.getHourlyRate() : DEFAULT_HOURLY_RATE;
            BigDecimal overtimeRate = staff.getOvertimeRate() != null ? staff.getOvertimeRate() : DEFAULT_OVERTIME_RATE;
            totalIncome = calcStaffEarnings(staff, monthHours);
            summary.put("salaryModeLabel", "时薪制");
            summary.put("monthHours", Math.round(monthHours * 10) / 10.0);
            summary.put("hourlyRate", hourlyRate);
            summary.put("overtimeRate", overtimeRate);
        } else if (salaryMode == 2) {
            BigDecimal fixedSalary = staff.getFixedMonthlySalary() != null ? staff.getFixedMonthlySalary() : BigDecimal.ZERO;
            totalIncome = fixedSalary;
            summary.put("salaryModeLabel", "固定月薪");
            summary.put("fixedMonthlySalary", fixedSalary);
        } else {
            // 提成制：无底薪
            totalIncome = totalCommission;
            summary.put("salaryModeLabel", "提成制");
            summary.put("commissionRate", staff.getCommissionRate());
        }

        summary.put("staffId", staffId);
        summary.put("staffName", staffName);
        summary.put("year", year);
        summary.put("month", month);
        summary.put("salaryMode", salaryMode);
        summary.put("orderCount", records.size());
        summary.put("totalCommission", totalCommission.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    // ==================== 待分配订单 ====================

    @GetMapping("/unallocated-orders")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<?> unallocatedOrders(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        List<OrderInfo> completedOrders = orderService.list(
            new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getProviderId, owner.getId()).ge(OrderInfo::getStatus, 1).orderByDesc(OrderInfo::getCreateTime)
        );
        Set<Long> allocated = incomeRecordService.list(
            new LambdaQueryWrapper<IncomeRecord>().eq(IncomeRecord::getProviderId, owner.getId()).select(IncomeRecord::getOrderId)
        ).stream().map(IncomeRecord::getOrderId).collect(Collectors.toSet());
        return Result.success(completedOrders.stream().filter(o -> !allocated.contains(o.getId())).collect(Collectors.toList()));
    }

    // ==================== 辅助方法 ====================

    private boolean isShopOwner(User user) {
        if (user.getRole() == 2) return true;
        return user.getRole() == 1 && (user.getStaffRole() == null || user.getStaffRole() == 1);
    }

    private boolean isOvertime(OrderInfo order) {
        if (order.getAppointmentTime() == null) return false;
        int hour = order.getAppointmentTime().getHour();
        int dayOfWeek = order.getAppointmentTime().getDayOfWeek().getValue();
        // 周末算加班，工作日18:00后算加班
        return dayOfWeek >= 6 || hour >= 18;
    }

    private double parseTime(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) + Integer.parseInt(p[1]) / 60.0;
    }

    /** 计算店员某天排班工时 */
    private double calcTodayHours(Long staffId, LocalDate date) {
        String dateStr = date.toString();
        List<Schedule> schedules = scheduleMapper.findByStaffAndDateRange(staffId, dateStr, dateStr);
        double total = 0;
        for (Schedule s : schedules) {
            if (s.getStatus() == 1) total += (parseTime(s.getEndTime()) - parseTime(s.getStartTime()));
        }
        return total;
    }

    /** 计算店员月度排班工时 */
    private double calcMonthHours(Long staffId, YearMonth ym) {
        String start = ym.atDay(1).toString();
        String end = ym.atEndOfMonth().toString();
        List<Schedule> schedules = scheduleMapper.findByStaffAndDateRange(staffId, start, end);
        double total = 0;
        for (Schedule s : schedules) {
            if (s.getStatus() == 1) total += (parseTime(s.getEndTime()) - parseTime(s.getStartTime()));
        }
        return total;
    }

    /** 根据薪资模式计算店员收入 */
    private BigDecimal calcStaffEarnings(User staff, double totalHours) {
        int salaryMode = staff.getSalaryMode() != null ? staff.getSalaryMode() : 1;
        if (salaryMode == 1) {
            BigDecimal hourlyRate = staff.getHourlyRate() != null ? staff.getHourlyRate() : DEFAULT_HOURLY_RATE;
            BigDecimal overtimeRate = staff.getOvertimeRate() != null ? staff.getOvertimeRate() : DEFAULT_OVERTIME_RATE;
            // 标准工时 8h/天 × 工作天数，超出算加班
            int workDays = YearMonth.now().lengthOfMonth();
            double standardHours = Math.min(totalHours, 8.0 * workDays);
            double overtimeHours = Math.max(0, totalHours - standardHours);
            return hourlyRate.multiply(new BigDecimal(standardHours))
                .add(overtimeRate.multiply(new BigDecimal(overtimeHours)))
                .setScale(2, RoundingMode.HALF_UP);
        } else if (salaryMode == 2) {
            return staff.getFixedMonthlySalary() != null ? staff.getFixedMonthlySalary() : BigDecimal.ZERO;
        } else {
            // 提成制：无底薪，仅按单提成（提成金额在 allocate 时计算）
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    // ==================== DTO ====================

    public static class AllocateDTO {
        private Long orderId;
        private Long staffId;
        private BigDecimal commissionPerOrder;
        private String remark;
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public BigDecimal getCommissionPerOrder() { return commissionPerOrder; }
        public void setCommissionPerOrder(BigDecimal commissionPerOrder) { this.commissionPerOrder = commissionPerOrder; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class SalaryDTO {
        private Long staffId;
        private Integer salaryMode;         // 0-提成制, 1-时薪制, 2-固定月薪
        private BigDecimal baseSalary;
        private BigDecimal commissionRate;
        private BigDecimal hourlyRate;
        private BigDecimal overtimeRate;
        private BigDecimal fixedMonthlySalary;
        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public Integer getSalaryMode() { return salaryMode; }
        public void setSalaryMode(Integer salaryMode) { this.salaryMode = salaryMode; }
        public BigDecimal getBaseSalary() { return baseSalary; }
        public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
        public BigDecimal getCommissionRate() { return commissionRate; }
        public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
        public BigDecimal getOvertimeRate() { return overtimeRate; }
        public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }
        public BigDecimal getFixedMonthlySalary() { return fixedMonthlySalary; }
        public void setFixedMonthlySalary(BigDecimal fixedMonthlySalary) { this.fixedMonthlySalary = fixedMonthlySalary; }
    }
}
