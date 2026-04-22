package com.petservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("`user`")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String phone;
    private Integer role;
    private String avatar;
    private String realName;
    private String idCard;
    private String shopName;
    private Integer status = 1;
    private Long parentProviderId;
    private Integer staffRole;
    /** 月底薪（仅店员有效，由店长设置） */
    private BigDecimal baseSalary;
    /** 每单固定提成金额（仅店员有效，提成制） */
    private BigDecimal commissionRate;
    /** 薪资模式: 0-提成制, 1-固定薪资(时薪制) */
    private Integer salaryMode;
    /** 正常时薪(元/小时) - 时薪制 */
    private BigDecimal hourlyRate;
    /** 加班时薪(元/小时) - 时薪制 */
    private BigDecimal overtimeRate;
    /** 固定月薪 - 固定薪资制 */
    private BigDecimal fixedMonthlySalary;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getParentProviderId() { return parentProviderId; }
    public void setParentProviderId(Long parentProviderId) { this.parentProviderId = parentProviderId; }
    public Integer getStaffRole() { return staffRole; }
    public void setStaffRole(Integer staffRole) { this.staffRole = staffRole; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public Integer getSalaryMode() { return salaryMode; }
    public void setSalaryMode(Integer salaryMode) { this.salaryMode = salaryMode; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getOvertimeRate() { return overtimeRate; }
    public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }
    public BigDecimal getFixedMonthlySalary() { return fixedMonthlySalary; }
    public void setFixedMonthlySalary(BigDecimal fixedMonthlySalary) { this.fixedMonthlySalary = fixedMonthlySalary; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
