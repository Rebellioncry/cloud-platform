import request from '@/utils/request'

// ==================== 看板 ====================

export function getDashboardOverview() {
  return request({ url: '/iot/dashboard/overview', method: 'get' })
}

// ==================== 产品管理 ====================

export function getProductList(params) {
  return request({ url: '/iot/product/list', method: 'get', params })
}

export function getProduct(id) {
  return request({ url: `/iot/product/${id}`, method: 'get' })
}

export function addProduct(data) {
  return request({ url: '/iot/product', method: 'post', data })
}

export function updateProduct(id, data) {
  return request({ url: `/iot/product/${id}`, method: 'put', data })
}

export function deleteProduct(id) {
  return request({ url: `/iot/product/${id}`, method: 'delete' })
}

export function getProductTree() {
  return request({ url: '/iot/product/tree', method: 'get' })
}

export function getThingModel(id) {
  return request({ url: `/iot/product/${id}/thing-model`, method: 'get' })
}

export function updateThingModel(id, thingModel) {
  return request({ url: `/iot/product/${id}/thing-model`, method: 'put', data: thingModel, headers: { 'Content-Type': 'text/plain' } })
}

export function publishThingModel(id) {
  return request({ url: `/iot/product/${id}/thing-model/publish`, method: 'post' })
}

// ==================== 设备管理 ====================

export function getDeviceList(params) {
  return request({ url: '/iot/device/list', method: 'get', params })
}

export function getDevice(id) {
  return request({ url: `/iot/device/${id}`, method: 'get' })
}

export function addDevice(data) {
  return request({ url: '/iot/device', method: 'post', data })
}

export function updateDevice(id, data) {
  return request({ url: `/iot/device/${id}`, method: 'put', data })
}

export function deleteDevice(id) {
  return request({ url: `/iot/device/${id}`, method: 'delete' })
}

export function activateDevice(id) {
  return request({ url: `/iot/device/${id}/activate`, method: 'post' })
}

export function disableDevice(id) {
  return request({ url: `/iot/device/${id}/disable`, method: 'post' })
}

// ==================== 设备状态 ====================

export function getDeviceStatus(id) {
  return request({ url: `/iot/device/${id}/status`, method: 'get' })
}

export function getPropertyHistory(id, propertyId, params) {
  return request({ url: `/iot/device/${id}/status/property/${propertyId}/history`, method: 'get', params })
}

// ==================== 设备功能 ====================

export function listDeviceFunctions(id) {
  return request({ url: `/iot/device/${id}/function/list`, method: 'get' })
}

export function invokeDeviceFunction(id, data) {
  return request({ url: `/iot/device/${id}/function/invoke`, method: 'post', data })
}

// ==================== 设备日志 ====================

export function getDeviceLogs(id, params) {
  return request({ url: `/iot/device/${id}/log`, method: 'get', params })
}

// ==================== 设备影子 ====================

export function getDeviceShadow(id) {
  return request({ url: `/iot/device/${id}/shadow`, method: 'get' })
}

export function updateDeviceShadow(id, data) {
  return request({ url: `/iot/device/${id}/shadow`, method: 'put', data })
}

export function getDeviceShadowDiff(id) {
  return request({ url: `/iot/device/${id}/shadow/diff`, method: 'get' })
}

// ==================== MQTT配置 ====================

export function getMqttConfigList(params) {
  return request({ url: '/iot/mqtt/config/list', method: 'get', params })
}

export function getMqttConfig(id) {
  return request({ url: `/iot/mqtt/config/${id}`, method: 'get' })
}

export function addMqttConfig(data) {
  return request({ url: '/iot/mqtt/config', method: 'post', data })
}

export function updateMqttConfig(id, data) {
  return request({ url: `/iot/mqtt/config/${id}`, method: 'put', data })
}

export function deleteMqttConfig(id) {
  return request({ url: `/iot/mqtt/config/${id}`, method: 'delete' })
}

export function startMqttConfig(id) {
  return request({ url: `/iot/mqtt/config/${id}/start`, method: 'post' })
}

export function stopMqttConfig(id) {
  return request({ url: `/iot/mqtt/config/${id}/stop`, method: 'post' })
}
