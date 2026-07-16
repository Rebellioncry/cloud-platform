package org.lyz.system.service;

import org.lyz.system.dto.UserDTO;
import org.lyz.common.core.entity.SysUser;
import org.lyz.common.core.result.PageResult;
import java.util.List;

public interface UserService {
    PageResult<UserDTO> list(int page, int size);
    UserDTO getById(String id);
    void create(UserDTO dto);
    void update(UserDTO dto);
    void delete(String id);
    void resetPassword(String id, String password);
    void assignRoles(String userId, List<String> roleIds);
}
