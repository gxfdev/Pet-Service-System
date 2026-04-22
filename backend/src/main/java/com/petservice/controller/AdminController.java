package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservice.common.Result;
import com.petservice.entity.OrderInfo;
import com.petservice.entity.User;
import com.petservice.service.OrderService;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) wrapper.eq(User::getRole, role);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(User::getUsername, keyword).or().like(User::getPhone, keyword);
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userService.page(pageParam, wrapper));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changeRole(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setRole(body.getOrDefault("role", 0));
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<OrderInfo>> listAllOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<OrderInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(OrderInfo::getStatus, status);
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return Result.success(orderService.page(pageParam, wrapper));
    }

    @PutMapping("/orders/{id}/intervene")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> interveneOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        OrderInfo order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        String action = body.get("action");
        if ("cancel".equals(action)) {
            order.setStatus(4); // 已取消
        } else if ("refund".equals(action)) {
            order.setStatus(5); // 退款中
        }
        order.setCancelReason(body.getOrDefault("reason", "管理员操作"));
        orderService.updateById(order);
        return Result.success();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> statistics() {
        long totalUsers = userService.count();
        long totalOrders = orderService.count();
        List<User> providers = userService.list(new LambdaQueryWrapper<User>().eq(User::getRole, 1));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalOrders", totalOrders);
        stats.put("providerCount", providers.size());
        return Result.success(stats);
    }

    @GetMapping("/audit/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> pendingProviders() {
        return Result.success(userService.list(new LambdaQueryWrapper<User>().eq(User::getRole, 1)));
    }

    @PutMapping("/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditProvider(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        String action = body.get("action");
        if ("reject".equals(action)) {
            user.setStatus(0);
        } else {
            user.setStatus(1);
        }
        userService.updateById(user);
        return Result.success();
    }
}
