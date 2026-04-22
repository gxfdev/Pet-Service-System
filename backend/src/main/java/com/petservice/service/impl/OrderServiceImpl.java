package com.petservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petservice.common.BusinessException;
import com.petservice.common.ErrorCode;
import com.petservice.dto.OrderCreateDTO;
import com.petservice.dto.ReviewDTO;
import com.petservice.entity.*;
import com.petservice.mapper.OrderInfoMapper;
import com.petservice.mapper.PetMapper;
import com.petservice.mapper.ProviderServiceMapper;
import com.petservice.mapper.ReviewMapper;
import com.petservice.mapper.ScheduleMapper;
import com.petservice.service.OrderService;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private ProviderServiceMapper providerServiceMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public OrderInfo createOrder(Long userId, OrderCreateDTO dto) {
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PET_NOT_FOUND);
        }

        ProviderService ps = providerServiceMapper.selectOne(
            new LambdaQueryWrapper<ProviderService>()
                .eq(ProviderService::getProviderId, dto.getProviderId())
                .eq(ProviderService::getServiceId, dto.getServiceId())
                .eq(ProviderService::getStatus, 1)
        );
        if (ps == null) throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);

        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProviderId(dto.getProviderId());
        order.setServiceId(dto.getServiceId());
        order.setPetId(dto.getPetId());
        order.setAppointmentTime(dto.getAppointmentTime() != null ? dto.getAppointmentTime() : LocalDateTime.now().plusDays(1));
        order.setTotalAmount(ps.getPrice());
        order.setStatus(0); // 待支付
        order.setRemark(dto.getRemark());
        // 时间段
        if (dto.getStartTime() != null) order.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) order.setEndTime(dto.getEndTime());
        if (dto.getTimeSlot() != null) order.setTimeSlot(dto.getTimeSlot());
        else if (dto.getStartTime() != null && dto.getEndTime() != null) {
            order.setTimeSlot(dto.getStartTime() + "-" + dto.getEndTime());
        }

        // 如果客户指定了店员
        if (dto.getStaffId() != null) {
            User staff = userService.getById(dto.getStaffId());
            if (staff == null || !dto.getProviderId().equals(staff.getParentProviderId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            order.setStaffId(dto.getStaffId());
        }

        save(order);
        return order;
    }

    @Override
    @Transactional
    public OrderInfo payOrder(Long orderId, Long userId) {
        OrderInfo order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId))
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        if (order.getStatus() != 0)
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

        order.setStatus(1); // 待服务
        order.setPayTime(LocalDateTime.now());

        // ===== 自动分配逻辑：支付后根据排班自动分配空闲店员 =====
        if (order.getStaffId() == null) {
            autoAssignStaff(order);
        }

        updateById(order);
        return order;
    }

    /**
     * 基于排班的自动接单分配逻辑
     * 1. 获取该店铺所有在岗店员
     * 2. 查询预约当天有排班且在预约时间段内工作的店员
     * 3. 检查店员在预约时间是否已有冲突订单
     * 4. 分配给第一个无冲突的空闲店员
     */
    private void autoAssignStaff(OrderInfo order) {
        try {
            Long providerId = order.getProviderId();
            String orderDate = order.getAppointmentTime().toLocalDate().toString();

            // 获取本店所有在岗店员
            List<User> staffs = userService.list(
                new LambdaQueryWrapper<User>()
                    .eq(User::getParentProviderId, providerId)
                    .eq(User::getStaffRole, 2)
                    .eq(User::getStatus, 1)
            );

            if (staffs.isEmpty()) return;

            // 预约的时间段
            String orderStart = order.getStartTime();
            String orderEnd = order.getEndTime();
            // 如果没有时间段，从appointmentTime推算1小时
            if (orderStart == null || orderEnd == null) {
                int hour = order.getAppointmentTime().getHour();
                orderStart = String.format("%02d:00", hour);
                orderEnd = String.format("%02d:00", Math.min(hour + 1, 23));
            }

            // 查找当天有排班且时间匹配的空闲店员
            for (User staff : staffs) {
                // 查询该店员当天的排班
                List<Schedule> schedules = scheduleMapper.findByStaffAndDateRange(
                    staff.getId(), orderDate, orderDate);

                boolean inSchedule = false;
                for (Schedule s : schedules) {
                    if (s.getStatus() != 1) continue;
                    // 检查预约时间段是否在排班时间内
                    if (timeOverlap(orderStart, orderEnd, s.getStartTime(), s.getEndTime())) {
                        inSchedule = true;
                        break;
                    }
                }

                if (!inSchedule) continue;

                // 检查该店员在预约时间是否有冲突订单
                boolean hasConflict = hasTimeConflict(staff.getId(), orderDate, orderStart, orderEnd, null);

                if (!hasConflict) {
                    // 自动分配
                    order.setStaffId(staff.getId());
                    order.setAutoAssigned(1);
                    // 支付后自动接单 → 直接进入服务中
                    order.setStatus(2);
                    return;
                }
            }
            // 没有空闲店员，保持待接单状态
        } catch (Exception e) {
            // 自动分配失败不影响支付流程
        }
    }

    /** 检查时间是否重叠 */
    private boolean timeOverlap(String s1, String e1, String s2, String e2) {
        try {
            double a1 = parseTime(s1), a2 = parseTime(e1);
            double b1 = parseTime(s2), b2 = parseTime(e2);
            return a1 < b2 && a2 > b1;
        } catch (Exception e) { return false; }
    }

    /** 检查店员在指定日期时间段是否有订单冲突 */
    private boolean hasTimeConflict(Long staffId, String date, String start, String end, Long excludeOrderId) {
        LocalDateTime dayStart = LocalDate.parse(date).atTime(0, 0);
        LocalDateTime dayEnd = LocalDate.parse(date).atTime(23, 59);

        List<OrderInfo> orders = list(
            new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStaffId, staffId)
                .in(OrderInfo::getStatus, 1, 2) // 待服务和服务中
                .ge(OrderInfo::getAppointmentTime, dayStart)
                .le(OrderInfo::getAppointmentTime, dayEnd)
        );

        for (OrderInfo o : orders) {
            if (excludeOrderId != null && o.getId().equals(excludeOrderId)) continue;
            String oStart = o.getStartTime();
            String oEnd = o.getEndTime();
            if (oStart == null || oEnd == null) {
                int h = o.getAppointmentTime().getHour();
                oStart = String.format("%02d:00", h);
                oEnd = String.format("%02d:00", Math.min(h + 1, 23));
            }
            if (timeOverlap(start, end, oStart, oEnd)) return true;
        }
        return false;
    }

    private double parseTime(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) + Integer.parseInt(p[1]) / 60.0;
    }

    @Override
    public OrderInfo acceptOrder(Long orderId, Long providerId) {
        OrderInfo order = getById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);

        // 验证权限：店长或被分配的店员
        User user = userService.getById(providerId);
        if (user == null) throw new BusinessException(ErrorCode.FORBIDDEN);

        boolean isShopOwner = user.getRole() == 2 || (user.getRole() == 1 && (user.getStaffRole() == null || user.getStaffRole() == 1));
        boolean isAssignedStaff = providerId.equals(order.getStaffId());

        if (!isShopOwner && !isAssignedStaff && !order.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (order.getStatus() != 1) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

        // 店员接单：实名验证
        if (!isShopOwner && user.getStaffRole() != null && user.getStaffRole() == 2) {
            if (user.getRealName() == null || user.getRealName().isEmpty()) {
                throw new BusinessException("请先完成实名认证后再接单");
            }
            if (!isAssignedStaff) {
                order.setStaffId(providerId);
            }
        }

        order.setStatus(2); // 服务中
        updateById(order);
        return order;
    }

    @Override
    public OrderInfo completeOrder(Long orderId, Long providerId) {
        OrderInfo order = getById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);

        User user = userService.getById(providerId);
        boolean isShopOwner = user != null && (user.getRole() == 2 || (user.getRole() == 1 && (user.getStaffRole() == null || user.getStaffRole() == 1)));
        boolean isAssignedStaff = providerId.equals(order.getStaffId());

        if (!isShopOwner && !isAssignedStaff && !order.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (order.getStatus() != 2) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

        order.setStatus(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        updateById(order);
        return order;
    }

    @Override
    public OrderInfo cancelOrder(Long orderId, Long userId, String reason) {
        OrderInfo order = getById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);

        User user = userService.getById(userId);
        boolean isShopOwner = user != null && (user.getRole() == 2 || (user.getRole() == 1 && (user.getStaffRole() == null || user.getStaffRole() == 1)));

        // 权限控制：仅店长可取消订单（客户也可以取消自己的待支付订单）
        boolean isCustomer = order.getUserId().equals(userId) && order.getStatus() == 0;

        if (!isShopOwner && !isCustomer) {
            throw new BusinessException("仅店长有权取消订单");
        }

        if (order.getStatus() == 3) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

        order.setStatus(4); // 已取消
        order.setCancelReason(reason);
        updateById(order);
        return order;
    }

    @Override
    @Transactional
    public Review addReview(Long orderId, Long userId, ReviewDTO dto) {
        OrderInfo order = getById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (order.getStatus() != 3) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

        Review existing = reviewMapper.selectOne(new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId));
        if (existing != null) throw new BusinessException("该订单已评价");

        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setProviderId(order.getProviderId());
        review.setStaffId(order.getStaffId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        reviewMapper.insert(review);
        return review;
    }

    private String generateOrderNo() {
        return "PS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
               + String.format("%04d", (int)(Math.random() * 10000));
    }
}
