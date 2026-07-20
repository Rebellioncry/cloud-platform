package org.lyz.iot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.IotDashboardDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.dao.IotProductDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "IoT看板")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class IotDashboardController {

    private final IotProductDao productDao;
    private final IotDeviceDao deviceDao;

    @Operation(summary = "看板概览数据")
    @GetMapping("/overview")
    public Result<IotDashboardDTO> overview() {
        IotDashboardDTO dto = new IotDashboardDTO();

        dto.setProductCount(productDao.count(
                new LambdaQueryWrapper<IotProduct>()
                        .eq(IotProduct::getDeleted, 0)));

        List<IotDevice> devices = deviceDao.list(
                new LambdaQueryWrapper<IotDevice>()
                        .eq(IotDevice::getDeleted, 0));

        dto.setDeviceCount((long) devices.size());
        dto.setOnlineCount(devices.stream().filter(d -> d.getStatus() != null && d.getStatus() == 1).count());
        dto.setOfflineCount(devices.stream().filter(d -> d.getStatus() != null && d.getStatus() == 2).count());
        dto.setInactiveCount(devices.stream().filter(d -> d.getStatus() != null && d.getStatus() == 0).count());
        dto.setDisabledCount(devices.stream().filter(d -> d.getStatus() != null && d.getStatus() == 3).count());

        Map<String, Long> groupByProduct = devices.stream()
                .collect(Collectors.groupingBy(IotDevice::getProductId, Collectors.counting()));

        List<IotDashboardDTO.ProductStat> stats = new ArrayList<>();
        for (Map.Entry<String, Long> entry : groupByProduct.entrySet()) {
            IotDashboardDTO.ProductStat stat = new IotDashboardDTO.ProductStat();
            stat.setProductId(entry.getKey());
            stat.setDeviceCount(entry.getValue());

            IotProduct product = productDao.getById(entry.getKey());
            stat.setProductName(product != null ? product.getName() : entry.getKey());
            stats.add(stat);
        }
        dto.setProductDeviceStats(stats);

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<IotDashboardDTO.DailyStat> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);
            long count = deviceDao.count(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getDeleted, 0)
                            .between(IotDevice::getCreateTime, start, end));
            IotDashboardDTO.DailyStat ds = new IotDashboardDTO.DailyStat();
            ds.setDate(day.format(fmt));
            ds.setCount(count);
            trend.add(ds);
        }
        dto.setDeviceTrend(trend);

        return Result.success(dto);
    }
}
