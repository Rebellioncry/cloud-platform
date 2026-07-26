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

// ==================== 规则引擎 ====================

export function getRuleList(params) {
  return request({ url: '/iot/rule/list', method: 'get', params })
}

export function getRule(id) {
  return request({ url: `/iot/rule/${id}`, method: 'get' })
}

export function addRule(data) {
  return request({ url: '/iot/rule', method: 'post', data })
}

export function updateRule(id, data) {
  return request({ url: `/iot/rule/${id}`, method: 'put', data })
}

export function deleteRule(id) {
  return request({ url: `/iot/rule/${id}`, method: 'delete' })
}

export function startRule(id) {
  return request({ url: `/iot/rule/${id}/start`, method: 'post' })
}

export function stopRule(id) {
  return request({ url: `/iot/rule/${id}/stop`, method: 'post' })
}

// ==================== 规则执行日志 ====================

export function getRuleLogList(params) {
  return request({ url: '/iot/rule/log/list', method: 'get', params })
}

// ==================== 文件存储 ====================

export function getFileStorageList(params) {
  return request({ url: '/iot/file-storage/list', method: 'get', params })
}

export function getFileStorage(id) {
  return request({ url: `/iot/file-storage/${id}`, method: 'get' })
}

export function addFileStorage(data) {
  return request({ url: '/iot/file-storage', method: 'post', data })
}

export function updateFileStorage(id, data) {
  return request({ url: `/iot/file-storage/${id}`, method: 'put', data })
}

export function deleteFileStorage(id) {
  return request({ url: `/iot/file-storage/${id}`, method: 'delete' })
}

export function testFileStorage(id) {
  return request({ url: `/iot/file-storage/${id}/test`, method: 'post' })
}

// ==================== 固件管理 ====================

export function getFirmwareList(params) {
  return request({ url: '/iot/firmware/list', method: 'get', params })
}

export function getFirmware(id) {
  return request({ url: `/iot/firmware/${id}`, method: 'get' })
}

export function uploadFirmware(data) {
  return request({ url: '/iot/firmware/upload', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function updateFirmware(id, data) {
  return request({ url: `/iot/firmware/${id}`, method: 'put', data })
}

export function deleteFirmware(id) {
  return request({ url: `/iot/firmware/${id}`, method: 'delete' })
}

// ==================== OTA升级 ====================

export function getOtaTaskList(params) {
  return request({ url: '/iot/ota/task/list', method: 'get', params })
}

export function getOtaTask(id) {
  return request({ url: `/iot/ota/task/${id}`, method: 'get' })
}

export function addOtaTask(data) {
  return request({ url: '/iot/ota/task', method: 'post', data })
}

export function deleteOtaTask(id) {
  return request({ url: `/iot/ota/task/${id}`, method: 'delete' })
}

export function startOtaTask(id) {
  return request({ url: `/iot/ota/task/${id}/start`, method: 'post' })
}

export function cancelOtaTask(id) {
  return request({ url: `/iot/ota/task/${id}/cancel`, method: 'post' })
}

export function getOtaTaskDevices(id) {
  return request({ url: `/iot/ota/task/${id}/devices`, method: 'get' })
}

export function retryOtaDevices(taskId, deviceIds) {
  return request({ url: `/iot/ota/task/${taskId}/retry`, method: 'post', data: deviceIds })
}

