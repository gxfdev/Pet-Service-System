package com.petservice.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservice.common.Result;
import com.petservice.dto.OrderCreateDTO;
import com.petservice.dto.ReviewDTO;
import com.petservice.entity.OrderInfo;
import com.petservice.entity.Review;
import com.petservice.entity.User;
import com.petservice.service.OrderService;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<OrderInfo> createOrder(@RequestBody OrderCreateDTO dto, Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return Result.success(orderService.createOrder(userId, dto));
    }

    @GetMapping
    public Result<Page<OrderInfo>> listOrders(
            Authentication auth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        User user = (User) auth.getPrincipal();
        Page<OrderInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();

        if (user.getRole() == 2) {
            // 管理员：查看所有订单，不限制userId/providerId
        } else if (user.getRole() == 1) {
            if (user.getStaffRole() != null && user.getStaffRole() == 2) {
                // 店员：查看分配给自己的订单（staffId = 当前用户ID）
                wrapper.and(w -> w
                    .eq(OrderInfo::getStaffId, user.getId())
                    .or()
                    .eq(OrderInfo::getProviderId, user.getParentProviderId())
                );
            } else {
                // 店长：查看店铺所有订单
                wrapper.eq(OrderInfo::getProviderId, user.getId());
            }
        } else {
            wrapper.eq(OrderInfo::getUserId, user.getId());
        }

        if (status != null) wrapper.eq(OrderInfo::getStatus, status);
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> result = orderService.page(pageParam, wrapper);

        // 补充关联字段
        List<OrderInfo> orders = result.getRecords();
        if (orders != null && !orders.isEmpty()) {
            Set<Long> userIds = new HashSet<>();
            Set<Long> staffIds = new HashSet<>();
            for (OrderInfo o : orders) {
                if (o.getUserId() != null) userIds.add(o.getUserId());
                if (o.getStaffId() != null) staffIds.add(o.getStaffId());
            }
            Map<Long, User> userMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u, (a,b)->a));
            Map<Long, User> staffMap = staffIds.isEmpty() ? Collections.emptyMap() :
                    userService.listByIds(staffIds).stream().collect(Collectors.toMap(User::getId, u -> u, (a,b)->a));
            for (OrderInfo o : orders) {
                User cu = userMap.get(o.getUserId());
                if (cu != null) {
                    o.setUserName(cu.getRealName() != null ? cu.getRealName() : cu.getUsername());
                    o.setUserPhone(cu.getPhone());
                }
                if (o.getStaffId() != null) {
                    User su = staffMap.get(o.getStaffId());
                    if (su != null) o.setStaffName(su.getRealName() != null ? su.getRealName() : su.getUsername());
                }
            }
        }
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<OrderInfo> getOrderDetail(@PathVariable Long id, Authentication auth) {
        OrderInfo order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        return Result.success(order);
    }

    @PutMapping("/{id}/pay")
    public Result<OrderInfo> payOrder(@PathVariable Long id, Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return Result.success(orderService.payOrder(id, userId));
    }

    @PutMapping("/{id}/accept")
    public Result<OrderInfo> acceptOrder(@PathVariable Long id, Authentication auth) {
        Long providerId = ((User) auth.getPrincipal()).getId();
        return Result.success(orderService.acceptOrder(id, providerId));
    }

    @PutMapping("/{id}/complete")
    public Result<OrderInfo> completeOrder(@PathVariable Long id, Authentication auth) {
        Long providerId = ((User) auth.getPrincipal()).getId();
        return Result.success(orderService.completeOrder(id, providerId));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderInfo> cancelOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        String reason = body != null ? body.get("reason") : "用户取消";
        return Result.success(orderService.cancelOrder(id, userId, reason));
    }

    @PostMapping("/{id}/review")
    public Result<Review> addReview(@PathVariable Long id, @RequestBody ReviewDTO dto, Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return Result.success(orderService.addReview(id, userId, dto));
    }
}
