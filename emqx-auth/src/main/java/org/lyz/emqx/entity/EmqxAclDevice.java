package org.lyz.emqx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("iot_device")
public class EmqxAclDevice {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String deviceName;

    private String deviceKey;

    private Integer status;
}
