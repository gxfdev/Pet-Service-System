package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservice.common.Result;
import com.petservice.entity.User;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店主管理控制器
 * 
 * 店长(SHOP_OWNER)拥有以下权限：
 * 1. 添加店员（注册账号并分配到本店）
 * 2. 添加客户（快速注册客户账号）
 * 3. 查看本店所有用户（店员+客户）
 * 4. 设置/修改员工薪资（底薪+每单提成）
 * 5. 删除用户（仅限本店管理的用户）
 */
@RestController
@RequestMapping("/api/shop")
public class ShopOwnerController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 用户列表 ====================

    /**
     * 获取本店全部用户列表（含店员和客户）
     * 支持按角色筛选、关键词搜索、分页
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Page<User>> listShopUsers(Authentication auth,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer roleFilter) {
        User owner = (User) auth.getPrincipal();
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 本店的：parent_provider_id = owner.id 或 id = owner.id
        wrapper.and(w -> w.eq(User::getParentProviderId, owner.getId()).or().eq(User::getId, owner.getId()));

        // 角色筛选
        if (roleFilter != null) {
            if (roleFilter == 2) {
                // 筛选店员: parent_provider_id=owner AND staff_role=2
                wrapper.and(w -> w.eq(User::getParentProviderId, owner.getId()).eq(User::getStaffRole, 2));
            } else if (roleFilter == 0) {
                // 筛选客户: role=0 且非本店人员
                wrapper.eq(User::getRole, 0).ne(User::getParentProviderId, owner.getId());
            }
        }

        // 关键词搜索（用户名/手机号/姓名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword.trim())
                    .or().like(User::getPhone, keyword.trim())
                    .or().like(User::getRealName, keyword.trim()));
        }

        // 排除密码
        Page<User> result = userService.page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));

        return Result.success(result);
    }

    /**
     * 快速获取本店成员概览（用于侧边栏或仪表盘）
     */
    @GetMapping("/users-summary")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Map<String, Object>> getUsersSummary(Authentication auth) {
        User owner = (User) auth.getPrincipal();

        long staffCount = userService.count(
            new LambdaQueryWrapper<User>()
                .eq(User::getParentProviderId, owner.getId())
                .eq(User::getStaffRole, 2)
        );
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("staffCount", staffCount);

        return Result.success(summary);
    }

    // ==================== 添加店员 ====================

    /**
     * 添加店员 - 店长为店铺招聘新员工
     * 自动设置: parent_provider_id=owner.id, staff_role=2, role=1
     */
    @PostMapping("/add-staff")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> addStaff(@RequestBody AddMemberDTO dto, Authentication auth) {
        User owner = (User) auth.getPrincipal();

        // 检查重复
        Long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) return Result.error("该用户名已存在");
        count = userService.count(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (count > 0) return Result.error("该手机号已被注册");

        User staff = new User();
        staff.setUsername(dto.getUsername());
        staff.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        staff.setPhone(dto.getPhone());
        staff.setRealName(dto.getRealName());
        staff.setRole(1);           // 服务商角色
        staff.setStaffRole(2);       // 店员
        staff.setParentProviderId(owner.getId());  // 归属本店
        staff.setStatus(1);          // 正常
        // 默认时薪制：正常18元/h + 加班25元/h
        Integer salaryMode = dto.getSalaryMode() != null ? dto.getSalaryMode() : 1;
        staff.setSalaryMode(salaryMode);
        if (salaryMode == 1) {
            staff.setHourlyRate(dto.getHourlyRate() != null ? dto.getHourlyRate() : new BigDecimal("18"));
            staff.setOvertimeRate(dto.getOvertimeRate() != null ? dto.getOvertimeRate() : new BigDecimal("25"));
        } else if (salaryMode == 2) {
            staff.setFixedMonthlySalary(dto.getFixedMonthlySalary() != null ? dto.getFixedMonthlySalary() : new BigDecimal("3000"));
        } else {
            staff.setCommissionRate(dto.getCommissionRate() != null ? dto.getCommissionRate() : new BigDecimal("15"));
        }

        userService.save(staff);
        return Result.success();
    }

    // ==================== 添加客户 ====================

    /**
     * 添加客户 - 店长为客户创建快捷账号
     * 自动设置: role=0 (普通客户)
     */
    @PostMapping("/add-customer")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> addCustomer(@RequestBody AddMemberDTO dto, Authentication auth) {
        // 检查重复
        Long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) return Result.error("该用户名已存在");
        count = userService.count(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (count > 0) return Result.error("该手机号已被注册");

        User customer = new User();
        customer.setUsername(dto.getUsername());
        customer.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        customer.setPhone(dto.getPhone());
        customer.setRealName(dto.getRealName());
        customer.setRole(0);          // 客户角色
        customer.setStatus(1);
        userService.save(customer);
        return Result.success();
    }

    // ==================== 更新用户信息 ====================

    /**
     * 更新用户基本信息（姓名、电话等）
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @RequestBody UpdateUserDTO dto,
                                   Authentication auth) {
        User owner = (User) auth.getPrincipal();
        User target = userService.getById(id);
        if (target == null) return Result.error("用户不存在");

        // 只能操作本店的用户
        if (!isOwnedByShop(target, owner)) return Result.error("无权操作此用户");

        if (dto.getRealName() != null) target.setRealName(dto.getRealName());
        if (dto.getPhone() != null) target.setPhone(dto.getPhone());
        if (dto.getStatus() != null) target.setStatus(dto.getStatus());

        userService.updateById(target);
        return Result.success();
    }

    // ==================== 设置薪资（复用 IncomeController 的接口，这里提供快捷方式） ====================

    /**
     * 快捷设置员工薪资方案
     * salaryMode=0 提成制: commissionRate
     * salaryMode=1 时薪制: hourlyRate(默认18/h) + overtimeRate(默认25/h)
     * salaryMode=2 固定月薪: fixedMonthlySalary
     */
    @PutMapping("/staff/{staffId}/salary")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> setStaffSalary(@PathVariable Long staffId,
                                        @RequestBody SalaryConfigDTO dto,
                                        Authentication auth) {
        User owner = (User) auth.getPrincipal();
        User staff = userService.getById(staffId);
        if (staff == null) return Result.error("员工不存在");
        if (!owner.getId().equals(staff.getParentProviderId())) return Result.error("该员工不属于您的店铺");

        Integer salaryMode = dto.getSalaryMode() != null ? dto.getSalaryMode() : 1;
        staff.setSalaryMode(salaryMode);

        if (salaryMode == 1) {
            // 时薪制：正常18元/h + 加班25元/h
            staff.setHourlyRate(dto.getHourlyRate() != null ? dto.getHourlyRate() : new BigDecimal("18"));
            staff.setOvertimeRate(dto.getOvertimeRate() != null ? dto.getOvertimeRate() : new BigDecimal("25"));
            staff.setCommissionRate(null);
            staff.setFixedMonthlySalary(null);
        } else if (salaryMode == 2) {
            // 固定月薪
            staff.setFixedMonthlySalary(dto.getFixedMonthlySalary() != null ? dto.getFixedMonthlySalary() : new BigDecimal("3000"));
            staff.setHourlyRate(null);
            staff.setOvertimeRate(null);
            staff.setCommissionRate(null);
        } else {
            // 提成制
            staff.setCommissionRate(dto.getCommissionRate() != null ? dto.getCommissionRate() : new BigDecimal("15"));
            staff.setHourlyRate(null);
            staff.setOvertimeRate(null);
            staff.setFixedMonthlySalary(null);
        }

        userService.updateById(staff);
        return Result.success();
    }

    // ==================== 删除用户 ====================

    /**
     * 删除用户（软删除/禁用）- 只能删除/禁用本店的店员或客户
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public Result<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        User target = userService.getById(id);
        if (target == null) return Result.error("用户不存在");
        if (target.getId().equals(owner.getId())) return Result.error("不能删除自己");
        if (!isOwnedByShop(target, owner)) return Result.error("无权操作此用户");

        // 禁用用户（不物理删除，保留数据完整性）
        target.setStatus(0);
        userService.updateById(target);
        return Result.success();
    }

    // ==================== 获取店铺可用店员列表（供下单时选择） ====================

    /**
     * 获取本店所有可用店员列表（供客户端选择指定服务店员）
     */
    @GetMapping("/available-staff")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN','SHOP_OWNER','SHOP_STAFF','USER')")
    public Result<List<User>> getAvailableStaff(Authentication auth,
                                                 @RequestParam(required = false) Long providerId) {
        User user = (User) auth.getPrincipal();
        Long targetProviderId;

        if (providerId != null) {
            // 客户选择服务商后，查看该店铺的店员
            targetProviderId = providerId;
        } else if (user.getRole() == 1 || user.getRole() == 2) {
            // 服务商/管理员视角
            targetProviderId = isShopOwner(user) ? user.getId() : user.getParentProviderId();
        } else {
            return Result.success(List.of());
        }

        List<User> staffs = userService.list(
            new LambdaQueryWrapper<User>()
                .eq(User::getParentProviderId, targetProviderId)
                .eq(User::getStaffRole, 2)
                .eq(User::getStatus, 1)
        );
        staffs.forEach(s -> s.setPassword(null));
        return Result.success(staffs);
    }

    // ==================== 辅助方法 ====================

    /** 判断目标用户是否属于本店 */
    private boolean isOwnedByShop(User target, User owner) {
        // 目标是本店的店员
        if (owner.getId().equals(target.getParentProviderId()) && target.getStaffRole() != null && target.getStaffRole() == 2) {
            return true;
        }
        return false;
    }

    /** 判断是否为店长 */
    private boolean isShopOwner(User u) {
        if (u.getRole() == 2) return true;
        return u.getRole() == 1 && (u.getStaffRole() == null || u.getStaffRole() == 1);
    }

    // ==================== DTO ====================

    /** 添加成员请求 DTO（通用） */
    public static class AddMemberDTO {
        private String username;
        private String password;
        private String phone;
        private String realName;
        private Integer salaryMode;          // 0-提成制, 1-时薪制, 2-固定月薪
        private BigDecimal commissionRate;   // 提成制
        private BigDecimal hourlyRate;       // 时薪制
        private BigDecimal overtimeRate;     // 时薪制
        private BigDecimal fixedMonthlySalary; // 固定月薪

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public Integer getSalaryMode() { return salaryMode; }
        public void setSalaryMode(Integer salaryMode) { this.salaryMode = salaryMode; }
        public BigDecimal getCommissionRate() { return commissionRate; }
        public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
        public BigDecimal getOvertimeRate() { return overtimeRate; }
        public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }
        public BigDecimal getFixedMonthlySalary() { return fixedMonthlySalary; }
        public void setFixedMonthlySalary(BigDecimal fixedMonthlySalary) { this.fixedMonthlySalary = fixedMonthlySalary; }
    }

    /** 更新用户信息 DTO */
    public static class UpdateUserDTO {
        private String realName;
        private String phone;
        private Integer status;

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }

    /** 薪资配置 DTO */
    public static class SalaryConfigDTO {
        private Integer salaryMode;          // 0-提成制, 1-时薪制, 2-固定月薪
        private BigDecimal commissionRate;
        private BigDecimal hourlyRate;
        private BigDecimal overtimeRate;
        private BigDecimal fixedMonthlySalary;

        public Integer getSalaryMode() { return salaryMode; }
        public void setSalaryMode(Integer salaryMode) { this.salaryMode = salaryMode; }
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
