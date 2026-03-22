package org.lyz.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String mobile;
    private String avatar;
    private Long tenantId;
    private List<String> roles;
    private List<MenuTree> menus;
}
