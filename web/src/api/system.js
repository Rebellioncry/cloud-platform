import request from '@/utils/request'

// 用户管理
export function getUserList(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

export function getUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

export function assignRoles(userId, roleIds) {
  return request({
    url: '/system/user/assign-roles',
    method: 'put',
    params: { userId },
    data: roleIds
  })
}

// 角色管理
export function getRole(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'get'
  })
}

export function getRoleList(params) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  })
}

export function addRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  })
}

export function assignMenus(roleId, menuIds) {
  return request({
    url: '/system/role/assign-menus',
    method: 'put',
    params: { roleId },
    data: menuIds
  })
}

// 菜单管理
export function getMenuList(params) {
  return request({
    url: '/system/menu/list',
    method: 'get',
    params
  })
}

export function getMenuTree() {
  return request({
    url: '/system/menu/tree',
    method: 'get'
  })
}

export function addMenu(data) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  })
}

export function updateMenu(data) {
  return request({
    url: '/system/menu',
    method: 'put',
    data
  })
}

export function deleteMenu(id) {
  return request({
    url: `/system/menu/${id}`,
    method: 'delete'
  })
}

// 租户管理
export function getTenantList(params) {
  return request({
    url: '/system/tenant/list',
    method: 'get',
    params
  })
}

export function getTenant(id) {
  return request({
    url: `/system/tenant/${id}`,
    method: 'get'
  })
}

export function addTenant(data) {
  return request({
    url: '/system/tenant',
    method: 'post',
    data
  })
}

export function updateTenant(data) {
  return request({
    url: '/system/tenant',
    method: 'put',
    data
  })
}

export function deleteTenant(id) {
  return request({
    url: `/system/tenant/${id}`,
    method: 'delete'
  })
}

// 审计日志
export function getAuditLogList(params) {
  return request({
    url: '/system/audit/list',
    method: 'get',
    params
  })
}
