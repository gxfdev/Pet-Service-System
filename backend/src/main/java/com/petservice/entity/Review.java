package com.petservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long providerId;
    private Long staffId;
    private Integer rating;
    private String content;
    private String images;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String petName;
    @TableField(exist = false)
    private String serviceName;
    @TableField(exist = false)
    private String staffName;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getProviderId() { return providerId; } public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public String getImages() { return images; } public void setImages(String images) { this.images = images; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getUserName() { return userName; } public void setUserName(String userName) { this.userName = userName; }
    public String getPetName() { return petName; } public void setPetName(String petName) { this.petName = petName; }
    public String getServiceName() { return serviceName; } public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public Long getStaffId() { return staffId; } public void setStaffId(Long staffId) { this.staffId = staffId; }
    public String getStaffName() { return staffName; } public void setStaffName(String staffName) { this.staffName = staffName; }
}
