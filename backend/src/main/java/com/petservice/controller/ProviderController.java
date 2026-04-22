package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petservice.common.Result;
import com.petservice.entity.OrderInfo;
import com.petservice.entity.ProviderService;
import com.petservice.entity.User;
import com.petservice.service.OrderService;
import com.petservice.service.ProviderServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    @Autowired
    private ProviderServiceService providerServiceService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/services")
    public Result<List<ProviderService>> myServices(Authentication auth) {
        Long providerId = ((User) auth.getPrincipal()).getId();
        List<ProviderService> list = providerServiceService.list(
            new LambdaQueryWrapper<ProviderService>().eq(ProviderService::getProviderId, providerId)
        );
        return Result.success(list);
    }

    @PostMapping("/services")
    public Result<Void> addService(@RequestBody ProviderService ps, Authentication auth) {
        Long providerId = ((User) auth.getPrincipal()).getId();
        ps.setProviderId(providerId);
        ps.setStatus(1);
        providerServiceService.save(ps);
        return Result.success();
    }

    @PutMapping("/services/{id}/toggle")
    public Result<Void> toggleService(@PathVariable Long id, Authentication auth) {
        Long providerId = ((User) auth.getPrincipal()).getId();
        ProviderService existing = providerServiceService.getById(id);
        if (existing == null || !existing.getProviderId().equals(providerId)) return Result.error("无权操作");
        existing.setStatus(existing.getStatus() == 1 ? 0 : 1);
        providerServiceService.updateById(existing);
        return Result.success();
    }

    @GetMapping("/orders")
    public Result<?> myOrders(Authentication auth,
                              @RequestParam(required = false) Integer status) {
        User user = (User) auth.getPrincipal();
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<OrderInfo>();

        if (user.getStaffRole() != null && user.getStaffRole() == 2) {
            // 店员：查看分配给自己的 或 本店铺的订单
            wrapper.and(w -> w
                .eq(OrderInfo::getStaffId, user.getId())
                .or()
                .eq(OrderInfo::getProviderId, user.getParentProviderId()));
        } else {
            // 店长/管理员：查看本店铺所有订单
            wrapper.eq(OrderInfo::getProviderId, user.getId());
        }
        if (status != null) wrapper.eq(OrderInfo::getStatus, status);
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return Result.success(orderService.list(wrapper));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(Authentication auth) {
        User user = (User) auth.getPrincipal();
        boolean owner = isShopOwner(user);

        long completedOrders;
        long pendingOrders;
        BigDecimal totalIncome = BigDecimal.ZERO;

        if (!owner) {
            // ===== 店员视角：查询分配给自己的订单 =====
            completedOrders = orderService.count(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getStaffId, user.getId()).eq(OrderInfo::getStatus, 3));
            pendingOrders = orderService.count(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getStaffId, user.getId()).in(OrderInfo::getStatus, 1, 2));
            // 预估收入：已完成订单金额汇总（时薪/提成模式下展示预估）
            List<OrderInfo> myCompletedOrders = orderService.list(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getStaffId, user.getId()).eq(OrderInfo::getStatus, 3));
            for (OrderInfo o : myCompletedOrders) {
                if (o.getTotalAmount() != null) totalIncome = totalIncome.add(o.getTotalAmount());
            }
        } else {
            // ===== 店长视角：查询本店铺所有订单 =====
            completedOrders = orderService.count(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getProviderId, user.getId()).eq(OrderInfo::getStatus, 3));
            pendingOrders = orderService.count(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getProviderId, user.getId()).in(OrderInfo::getStatus, 1, 2));
            for (OrderInfo o : orderService.list(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getProviderId, user.getId()).eq(OrderInfo::getStatus, 3))) {
                if (o.getTotalAmount() != null) totalIncome = totalIncome.add(o.getTotalAmount());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("completedOrders", completedOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP));
        stats.put("isOwner", owner);
        return Result.success(stats);
    }

    private boolean isShopOwner(User u) {
        if (u.getRole() == 2) return true;
        return u.getRole() == 1 && (u.getStaffRole() == null || u.getStaffRole() == 1);
    }
}
