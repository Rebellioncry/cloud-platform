package org.lyz.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {
    @Schema(description = "总记录数")
    private long total;
    @Schema(description = "当前页码")
    private long pageNum;
    @Schema(description = "每页大小")
    private long pageSize;
    @Schema(description = "总页数")
    private long pages;
    @Schema(description = "数据列表")
    private List<T> records;

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((total + pageSize - 1) / pageSize);
        result.setRecords(records);
        return result;
    }
}
