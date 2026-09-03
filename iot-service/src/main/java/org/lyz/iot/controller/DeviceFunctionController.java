package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.DeviceFunctionDTO;
import org.lyz.iot.service.DeviceFunctionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备功能")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceFunctionController {

    private final DeviceFunctionService functionService;

    @Operation(summary = "设备功能列表")
    @GetMapping("/{id}/function")
    public Result<List<DeviceFunctionDTO>> listFunctions(@PathVariable(value = "id") String deviceId) {
        return Result.success(functionService.listFunctions(deviceId));
    }

    @Operation(summary = "调用设备功能")
    @PostMapping("/{id}/function/invoke")
    public Result<Void> invokeFunction(@PathVariable(value = "id") String deviceId,
                                        @RequestBody Map<String, String> body) {
        functionService.invokeFunction(deviceId, body.get("identifier"), body.get("input"));
        return Result.success(null);
    }
}
