package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.DeviceShadowDTO;
import org.lyz.iot.service.DeviceShadowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备影子")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceShadowController {

    private final DeviceShadowService shadowService;

    @Operation(summary = "获取设备影子")
    @GetMapping("/{id}/shadow")
    public Result<List<DeviceShadowDTO>> getShadow(@PathVariable(value = "id") String deviceId) {
        return Result.success(shadowService.getShadow(deviceId));
    }

    @Operation(summary = "设置期望值")
    @PutMapping("/{id}/shadow")
    public Result<Void> updateDesired(@PathVariable(value = "id") String deviceId,
                                       @RequestBody Map<String, String> body) {
        shadowService.updateDesired(deviceId, body.get("identifier"), body.get("value"));
        return Result.success(null);
    }

    @Operation(summary = "获取差异(期望值 != 上报值)")
    @GetMapping("/{id}/shadow/diff")
    public Result<List<DeviceShadowDTO>> getDiff(@PathVariable(value = "id") String deviceId) {
        return Result.success(shadowService.getDiff(deviceId));
    }
}
