import request from '@/utils/request'

export function login(username, password) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  })
}

export function sendCode(type, target) {
  return request({
    url: '/auth/code/send',
    method: 'post',
    data: { type, ...target }
  })
}

export function codeLogin(type, target, code) {
  return request({
    url: '/auth/code/login',
    method: 'post',
    data: { type, ...target, code }
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/userinfo',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
