package org.lyz.system.service;

import org.lyz.system.dto.UserDTO;
import org.lyz.common.core.entity.SysUser;
import java.util.List;

public interface UserService {
    List<SysUser> list();
    UserDTO getById(Long id);
    void create(UserDTO dto);
    void update(UserDTO dto);
    void delete(Long id);
    void resetPassword(Long id, String password);
    void assignRoles(Long userId, List<Long> roleIds);
}
