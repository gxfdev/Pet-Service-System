package com.petservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;

@TableName("provider_service")
public class ProviderService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long providerId;
    private Long serviceId;
    private BigDecimal price;
    private Integer status = 1;

    @TableField(exist = false)
    private String serviceName;
    @TableField(exist = false)
    private Integer serviceCategory;
    @TableField(exist = false)
    private Integer duration;
    @TableField(exist = false)
    private String description;
    @TableField(exist = false)
    private String shopName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public Integer getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(Integer serviceCategory) { this.serviceCategory = serviceCategory; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
}
