package org.lyz.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_social")
public class SysSocial {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    private Long tenantId;
    private String platform;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
}
