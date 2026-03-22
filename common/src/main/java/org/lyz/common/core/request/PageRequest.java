package org.lyz.common.core.request;

import lombok.Data;
import org.lyz.common.core.constant.SecurityConstants;

@Data
public class PageRequest {
    private Long pageNum = (long) SecurityConstants.DEFAULT_PAGE_NUM;
    private Long pageSize = (long) SecurityConstants.DEFAULT_PAGE_SIZE;

    public long offset() {
        return (pageNum - 1) * pageSize;
    }
}
