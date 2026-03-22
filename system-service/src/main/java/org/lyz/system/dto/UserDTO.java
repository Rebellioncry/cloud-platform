package org.lyz.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    private String password;
    
    private String nickname;
    private String email;
    private String mobile;
    private String avatar;
    private Integer status;
    private List<Long> roleIds;
}
