package org.lyz.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {
    @Schema(description = "状态码")
    private int code;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "数据")
    private T data;
    
    @Schema(description = "时间戳")
    private long timestamp;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> unauthorized(String message) {
        return fail(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return fail(403, message);
    }
}
