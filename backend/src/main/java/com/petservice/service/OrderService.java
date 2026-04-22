package com.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petservice.dto.OrderCreateDTO;
import com.petservice.dto.ReviewDTO;
import com.petservice.entity.OrderInfo;
import com.petservice.entity.Review;

public interface OrderService extends IService<OrderInfo> {
    OrderInfo createOrder(Long userId, OrderCreateDTO dto);
    OrderInfo payOrder(Long orderId, Long userId);
    OrderInfo acceptOrder(Long orderId, Long providerId);
    OrderInfo completeOrder(Long orderId, Long providerId);
    OrderInfo cancelOrder(Long orderId, Long userId, String reason);
    Review addReview(Long orderId, Long userId, ReviewDTO dto);
}
