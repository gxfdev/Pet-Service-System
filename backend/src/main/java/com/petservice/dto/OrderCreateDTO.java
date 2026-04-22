package com.petservice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class OrderCreateDTO {
    @NotNull(message = "服务商不能为空")
    private Long providerId;
    /** 指定的执行店员ID（可选） */
    private Long staffId;
    @NotNull(message = "服务项目不能为空")
    private Long serviceId;
    @NotNull(message = "宠物不能为空")
    private Long petId;
    private LocalDateTime appointmentTime;
    private String remark;
    /** 预约时间段 如 "09:00-10:00" */
    private String timeSlot;
    /** 预约开始时间 HH:mm */
    private String startTime;
    /** 预约结束时间 HH:mm */
    private String endTime;

    public Long getProviderId() { return providerId; } public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getStaffId() { return staffId; } public void setStaffId(Long staffId) { this.staffId = staffId; }
    public Long getServiceId() { return serviceId; } public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; } public void setAppointmentTime(LocalDateTime t) { this.appointmentTime = t; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public String getTimeSlot() { return timeSlot; } public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getStartTime() { return startTime; } public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; } public void setEndTime(String endTime) { this.endTime = endTime; }
}
