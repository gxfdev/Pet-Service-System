package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservice.common.Result;
import com.petservice.entity.ProviderService;
import com.petservice.entity.ServiceItem;
import com.petservice.service.ProviderServiceService;
import com.petservice.service.ServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ProviderServiceService providerServiceService;

    @GetMapping
    public Result<Page<ServiceItem>> listServices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String keyword) {
        Page<ServiceItem> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
        if (category != null) wrapper.eq(ServiceItem::getCategory, category);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(ServiceItem::getName, keyword);
        return Result.success(serviceItemService.page(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    public Result<ServiceItem> getServiceDetail(@PathVariable Long id) {
        ServiceItem item = serviceItemService.getById(id);
        if (item == null) return Result.error("服务不存在");
        return Result.success(item);
    }

    @GetMapping("/{id}/providers")
    public Result<List<ProviderService>> getProvidersByService(@PathVariable Long id) {
        List<ProviderService> providers = providerServiceService.list(
            new LambdaQueryWrapper<ProviderService>()
                .eq(ProviderService::getServiceId, id)
                .eq(ProviderService::getStatus, 1)
        );
        return Result.success(providers);
    }
}
