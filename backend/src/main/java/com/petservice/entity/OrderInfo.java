package com.petservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("order_info")
public class OrderInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long providerId;
    private Long staffId;
    private Long serviceId;
    private Long petId;
    private LocalDateTime appointmentTime;
    private BigDecimal totalAmount;
    private Integer status = 0;
    private String remark;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
    /** 预约时间段 如 09:00-10:00 */
    private String timeSlot;
    /** 预约开始时间 HH:mm */
    private String startTime;
    /** 预约结束时间 HH:mm */
    private String endTime;
    /** 是否自动分配 */
    private Integer autoAssigned;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String petName;
    @TableField(exist = false)
    private String serviceName;
    @TableField(exist = false)
    private String shopName;
    @TableField(exist = false)
    private String providerPhone;
    @TableField(exist = false)
    private String userPhone;
    @TableField(exist = false)
    private String staffName;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getProviderId() { return providerId; } public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getStaffId() { return staffId; } public void setStaffId(Long staffId) { this.staffId = staffId; }
    public Long getServiceId() { return serviceId; } public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; } public void setAppointmentTime(LocalDateTime t) { this.appointmentTime = t; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public String getCancelReason() { return cancelReason; } public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getPayTime() { return payTime; } public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getCompleteTime() { return completeTime; } public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public String getTimeSlot() { return timeSlot; } public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getStartTime() { return startTime; } public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; } public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getAutoAssigned() { return autoAssigned; } public void setAutoAssigned(Integer autoAssigned) { this.autoAssigned = autoAssigned; }
    public String getUserName() { return userName; } public void setUserName(String userName) { this.userName = userName; }
    public String getPetName() { return petName; } public void setPetName(String petName) { this.petName = petName; }
    public String getServiceName() { return serviceName; } public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getShopName() { return shopName; } public void setShopName(String shopName) { this.shopName = shopName; }
    public String getProviderPhone() { return providerPhone; } public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public String getUserPhone() { return userPhone; } public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getStaffName() { return staffName; } public void setStaffName(String staffName) { this.staffName = staffName; }
}
