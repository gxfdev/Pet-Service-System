package com.petservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("income_record")
public class IncomeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;           // 关联订单ID
    private Long providerId;        // 所属店铺（服务商）ID
    private Long staffId;           // 执行员工ID
    private BigDecimal orderAmount; // 订单总金额
    private BigDecimal commissionPerOrder; // 本单固定提成金额
    private BigDecimal staffShare;  // 员工总收益 = 提成金额(本单)
    private BigDecimal shopShare;   // 店铺收入 = 订单金额 - 提成
    private Integer status;         // 0-待分配, 1-已分配, 2-已结算
    private String remark;
    private Long assignedBy;        // 分配人（店长）ID
    /** 提成计算明细（JSON格式，用于前端展示计算过程） */
    private String calcDetail;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getCommissionPerOrder() { return commissionPerOrder; }
    public void setCommissionPerOrder(BigDecimal commissionPerOrder) { this.commissionPerOrder = commissionPerOrder; }
    public BigDecimal getStaffShare() { return staffShare; }
    public void setStaffShare(BigDecimal staffShare) { this.staffShare = staffShare; }
    public BigDecimal getShopShare() { return shopShare; }
    public void setShopShare(BigDecimal shopShare) { this.shopShare = shopShare; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }
    public String getCalcDetail() { return calcDetail; }
    public void setCalcDetail(String calcDetail) { this.calcDetail = calcDetail; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
