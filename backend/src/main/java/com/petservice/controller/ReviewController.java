package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservice.common.Result;
import com.petservice.entity.OrderInfo;
import com.petservice.entity.ProviderService;
import com.petservice.entity.Review;
import com.petservice.entity.User;
import com.petservice.mapper.ProviderServiceMapper;
import com.petservice.mapper.ReviewMapper;
import com.petservice.service.OrderService;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProviderServiceMapper providerServiceMapper;

    @GetMapping
    public Result<List<Review>> listReviews(Authentication auth,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "50") Integer size) {
        User user = (User) auth.getPrincipal();
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        if (user.getRole() == 2) {
            // 管理员：查看所有评价
        } else if (user.getRole() == 1) {
            // 商家/店长/店员：查看本店铺的评价
            Long providerId = user.getId();
            // 店员看到的是分配给自己的订单的评价
            if (user.getStaffRole() != null && user.getStaffRole() == 2) {
                wrapper.and(w -> w
                    .eq(Review::getStaffId, user.getId())
                    .or()
                    .eq(Review::getProviderId, user.getParentProviderId()));
            } else {
                // 店长/管理员
                wrapper.eq(Review::getProviderId, providerId);
            }
        } else {
            // 客户：只能看到自己订单的评价
            wrapper.eq(Review::getUserId, user.getId());
        }

        wrapper.orderByDesc(Review::getCreateTime);
        Page<Review> pageParam = new Page<>(page, size);
        Page<Review> result = reviewMapper.selectPage(pageParam, wrapper);
        List<Review> reviews = result.getRecords();

        // 补充关联字段
        enrichReviews(reviews);

        return Result.success(reviews);
    }

    // 根据订单ID获取评价（用于客户视角）
    @GetMapping("/order/{orderId}")
    public Result<Review> getReviewByOrderId(@PathVariable Long orderId) {
        Review review = reviewMapper.selectOne(
            new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId)
        );
        if (review != null) {
            enrichReviews(Collections.singletonList(review));
        }
        return Result.success(review);
    }

    // 补充评价的关联信息（用户名、宠物名、服务名、店员名）
    private void enrichReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return;

        Set<Long> orderIds = reviews.stream().map(Review::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = reviews.stream().map(Review::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> staffIds = reviews.stream().map(Review::getStaffId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 查询订单获取服务信息
        Map<Long, OrderInfo> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderInfo> orders = orderService.listByIds(orderIds);
            orderMap = orders.stream().collect(Collectors.toMap(OrderInfo::getId, o -> o, (a, b) -> a));
        }

        // 查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        Map<Long, User> staffMap = new HashMap<>();
        Set<Long> allUserIds = new HashSet<>(userIds);
        allUserIds.addAll(staffIds);
        if (!allUserIds.isEmpty()) {
            List<User> users = userService.listByIds(allUserIds);
            for (User u : users) {
                if (userIds.contains(u.getId())) {
                    userMap.put(u.getId(), u);
                }
                if (staffIds.contains(u.getId())) {
                    staffMap.put(u.getId(), u);
                }
            }
        }

        // 查询服务名称
        Set<Long> serviceIds = orderMap.values().stream()
            .map(OrderInfo::getServiceId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, String> serviceNameMap = new HashMap<>();
        if (!serviceIds.isEmpty()) {
            List<ProviderService> services = providerServiceMapper.selectBatchIds(serviceIds);
            // 获取基础服务名称
            services.stream()
                .collect(Collectors.toMap(ProviderService::getServiceId, ProviderService::getServiceName, (a, b) -> a));
            
            // 对于ProviderService，需要获取对应的基础服务信息
            for (ProviderService ps : services) {
                serviceNameMap.put(ps.getId(), ps.getServiceName());
            }
        }

        // 填充数据
        for (Review review : reviews) {
            OrderInfo order = orderMap.get(review.getOrderId());
            if (order != null) {
                // 用户信息
                User customer = userMap.get(order.getUserId());
                if (customer != null) {
                    review.setUserName(customer.getRealName() != null ? customer.getRealName() : customer.getUsername());
                }
                // 服务名称
                if (order.getServiceId() != null) {
                    review.setServiceName(serviceNameMap.getOrDefault(order.getServiceId(), "服务 #" + order.getServiceId()));
                }
                // 宠物名称 - 从订单获取
                // 店员信息
                User staff = staffMap.get(order.getStaffId());
                if (staff != null) {
                    review.setStaffName(staff.getRealName() != null ? staff.getRealName() : staff.getUsername());
                }
                review.setStaffId(order.getStaffId());
            }
        }
    }
}
