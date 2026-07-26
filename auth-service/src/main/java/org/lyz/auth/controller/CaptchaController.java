package org.lyz.auth.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "验证码管理", description = "滑动拼图验证码生成与校验")
public class CaptchaController {

    private final ImageCaptchaApplication imageCaptchaApplication;
    private final ObjectMapper objectMapper;

    public CaptchaController(ImageCaptchaApplication imageCaptchaApplication, ObjectMapper objectMapper) {
        this.imageCaptchaApplication = imageCaptchaApplication;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "生成验证码", description = "生成滑动拼图验证码")
    @RequestMapping(value = "/captcha/gen", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> gen() {
        ApiResponse<?> response = imageCaptchaApplication.generateCaptcha("SLIDER");
        Map<String, Object> result = new HashMap<>();
        result.put("code", response.getCode());
        result.put("msg", response.getMsg());
        result.put("data", response.getData());
        return result;
    }

    @Operation(summary = "校验验证码", description = "校验滑动拼图位置是否正确")
    @PostMapping("/captcha/check")
    public Map<String, Object> check(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        Object dataObj = params.get("data");

        ImageCaptchaTrack track;
        if (dataObj instanceof Map) {
            track = objectMapper.convertValue(dataObj, ImageCaptchaTrack.class);
        } else {
            track = new ImageCaptchaTrack();
            if (params.containsKey("left")) {
                track.setLeft(((Number) params.get("left")).intValue());
            }
            if (params.containsKey("top")) {
                track.setTop(((Number) params.get("top")).intValue());
            }
        }

        ApiResponse<?> response = imageCaptchaApplication.matching(id, track);
        Map<String, Object> result = new HashMap<>();
        result.put("code", response.getCode());
        result.put("msg", response.getMsg());
        result.put("data", response.getData());
        return result;
    }
}
