package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "IoT看板数据")
public class IotDashboardDTO {

    @Schema(description = "产品总数")
    private long productCount;

    @Schema(description = "设备总数")
    private long deviceCount;

    @Schema(description = "在线设备数")
    private long onlineCount;

    @Schema(description = "离线设备数")
    private long offlineCount;

    @Schema(description = "未激活设备数")
    private long inactiveCount;

    @Schema(description = "已禁用设备数")
    private long disabledCount;

    @Schema(description = "各产品设备数量")
    private List<ProductStat> productDeviceStats;

    @Schema(description = "近7日设备增长趋势")
    private List<DailyStat> deviceTrend;

    @Data
    @Schema(description = "产品设备统计")
    public static class ProductStat {
        @Schema(description = "产品ID")
        private String productId;
        @Schema(description = "产品名称")
        private String productName;
        @Schema(description = "设备数量")
        private long deviceCount;
    }

    @Data
    @Schema(description = "每日统计")
    public static class DailyStat {
        @Schema(description = "日期")
        private String date;
        @Schema(description = "数量")
        private long count;
    }
}
