import request from '@/utils/request'

export function login(username, password) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/user/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function getSocialUrl(platform) {
  return request({
    url: `/auth/social/authorize/${platform}`,
    method: 'get'
  })
}

export function socialCallback(platform, callback) {
  return request({
    url: `/auth/social/callback/${platform}`,
    method: 'get',
    params: callback
  })
}
