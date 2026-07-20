<template>
  <div class="page-container">
    <div class="page-header">
      <el-page-header @back="router.push('/iot/device')">
        <template #content>
          <span class="detail-title">{{ deviceInfo.deviceName || '设备详情' }}</span>
          <el-tag v-if="deviceInfo.status === 1" type="success" style="margin-left: 10px">在线</el-tag>
          <el-tag v-else-if="deviceInfo.status === 2" type="warning" style="margin-left: 10px">离线</el-tag>
          <el-tag v-else-if="deviceInfo.status === 0" type="info" style="margin-left: 10px">未激活</el-tag>
          <el-tag v-else type="danger" style="margin-left: 10px">禁用</el-tag>
        </template>
      </el-page-header>
    </div>

    <el-card class="info-card" shadow="never">
      <el-descriptions :column="4" border>
        <el-descriptions-item label="设备名称">{{ deviceInfo.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ deviceInfo.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="ProductKey">{{ deviceInfo.productKey }}</el-descriptions-item>
        <el-descriptions-item label="DeviceKey">{{ deviceInfo.deviceKey }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ deviceInfo.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="固件版本">{{ deviceInfo.firmwareVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后上线">{{ deviceInfo.lastOnlineTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ deviceInfo.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ========== 运行状态 ========== -->
        <el-tab-pane label="运行状态" name="status">
          <div v-loading="statusLoading">
            <div v-if="deviceInfo.status === 3" style="margin-bottom: 12px">
              <el-alert title="设备已禁用，数据上报已停止" type="warning" show-icon :closable="false" />
            </div>
            <div class="status-toolbar">
              <span class="status-count" v-if="statusAll.length > 0">
                属性 {{ statusList.length }} 个 · 事件 {{ eventList.length }} 个
              </span>
              <div style="margin-left: auto; display: flex; align-items: center; gap: 12px">
                <el-switch v-model="autoRefresh" active-text="自动刷新" inactive-text="" style="--el-switch-on-color: #409eff" />
                <span v-if="autoRefresh" class="auto-refresh-hint">每3秒刷新</span>
                <el-button-group>
                  <el-button :type="statusView === 'card' ? 'primary' : ''" size="small" @click="statusView = 'card'">卡片</el-button>
                  <el-button :type="statusView === 'table' ? 'primary' : ''" size="small" @click="statusView = 'table'">列表</el-button>
                </el-button-group>
              </div>
            </div>
            <div v-if="statusAll.length === 0" class="empty-tip">暂无运行数据</div>

            <!-- 卡片视图 -->
            <template v-else-if="statusView === 'card'">
              <div v-if="statusList.length > 0" class="section-label">属性</div>
              <div class="card-grid">
                <div v-for="item in statusList" :key="item.identifier" class="status-card">
                  <div class="card-header">
                    <span class="card-name">{{ item.name }}</span>
                    <el-tag size="small" :type="item.accessMode === 'rw' ? '' : 'info'">{{ accessModeLabel(item.accessMode) }}</el-tag>
                  </div>
                  <div class="card-value" :class="{ 'unreported': !item.value }">{{ item.value ?? '-' }}</div>
                  <div class="card-meta">
                    <span>{{ item.identifier }}</span>
                    <span>{{ item.dataType }}</span>
                  </div>
                  <div class="card-footer">
                    <span class="card-time" :class="{ 'unreported': !item.timestamp }">{{ item.timestamp || '暂未上报' }}</span>
                    <el-button v-if="item.value" link type="primary" size="small" @click="handleHistory(item)">历史</el-button>
                  </div>
                </div>
              </div>

              <div v-if="eventList.length > 0" class="section-label" style="margin-top: 20px">事件</div>
              <div class="card-grid">
                <div v-for="evt in eventList" :key="evt.identifier" class="status-card event-card">
                  <div class="card-header">
                    <span class="card-name">{{ evt.name }}</span>
                    <el-tag size="small" :type="eventTypeTag(evt.eventType)">{{ eventTypeLabel(evt.eventType) }}</el-tag>
                  </div>
                  <div class="card-value" :class="{ 'unreported': !evt.outputValues }">
                    {{ evt.outputValues ? formatEventValues(evt.outputValues) : '-' }}
                  </div>
                  <div class="card-meta">
                    <span>{{ evt.identifier }}</span>
                    <span>事件</span>
                  </div>
                  <div class="card-footer">
                    <span class="card-time" :class="{ 'unreported': !evt.timestamp }">{{ evt.timestamp || '暂未上报' }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 表格视图 -->
            <template v-else>
              <div v-if="statusList.length > 0" class="section-label">属性</div>
              <el-table v-if="statusList.length > 0" :data="statusList" border stripe size="small">
                <el-table-column prop="name" label="名称" width="120" />
                <el-table-column prop="identifier" label="标识符" width="140" />
                <el-table-column label="当前值" width="160">
                  <template #default="{ row }">
                    <span :class="{ 'unreported': !row.value }">{{ row.value || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="dataType" label="类型" width="80" />
                <el-table-column label="读写" width="70">
                  <template #default="{ row }">
                    <el-tag size="small">{{ accessModeLabel(row.accessMode) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="上报时间" width="180">
                  <template #default="{ row }">
                    <span :class="{ 'unreported': !row.timestamp }">{{ row.timestamp || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70">
                  <template #default="{ row }">
                    <el-button v-if="row.value" link type="primary" size="small" @click="handleHistory(row)">历史</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div v-if="eventList.length > 0" class="section-label" style="margin-top: 20px">事件</div>
              <el-table v-if="eventList.length > 0" :data="eventList" border stripe size="small">
                <el-table-column prop="name" label="名称" width="120" />
                <el-table-column prop="identifier" label="标识符" width="140" />
                <el-table-column label="事件类型" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="eventTypeTag(row.eventType)">{{ eventTypeLabel(row.eventType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="输出数据" min-width="200">
                  <template #default="{ row }">
                    <span :class="{ 'unreported': !row.outputValues }">{{ row.outputValues ? formatEventValues(row.outputValues) : '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="上报时间" width="180">
                  <template #default="{ row }">
                    <span :class="{ 'unreported': !row.timestamp }">{{ row.timestamp || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </div>
        </el-tab-pane>

        <!-- ========== 设备影子 ========== -->
        <el-tab-pane label="设备影子" name="shadow">
          <div v-loading="shadowLoading">
            <div style="margin-bottom: 12px">
              <el-button type="primary" size="small" @click="loadShadow">刷新</el-button>
              <el-button size="small" @click="showDiffOnly = !showDiffOnly">
                {{ showDiffOnly ? '显示全部' : '仅显示差异' }}
              </el-button>
            </div>
            <div v-if="shadowList.length === 0" class="empty-tip">暂无影子数据</div>
            <el-table v-else :data="filteredShadow" border stripe>
              <el-table-column prop="identifier" label="属性标识" width="180" />
              <el-table-column label="期望值 (Desired)" width="200">
                <template #default="{ row }">
                  <span :class="{ 'diff-value': row.hasDiff }">{{ row.desiredValue || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="desiredTime" label="设定时间" width="180" />
              <el-table-column label="上报值 (Reported)" width="200">
                <template #default="{ row }">
                  <span :class="{ 'diff-value': row.hasDiff }">{{ row.reportedValue || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reportedTime" label="上报时间" width="180" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.hasDiff" type="warning" size="small">不同</el-tag>
                  <el-tag v-else type="success" size="small">一致</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleSetDesired(row)">设置</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- ========== 功能调用 ========== -->
        <el-tab-pane label="功能调用" name="function">
          <div v-loading="funcLoading">
            <div v-if="deviceInfo.status === 3" class="func-disabled-notice">
              <el-alert title="设备已禁用，功能调用已关闭" type="warning" show-icon :closable="false" />
            </div>
            <div v-if="funcList.length === 0" class="empty-tip">暂无服务定义</div>
            <div v-else class="func-list">
              <el-card v-for="svc in funcList" :key="svc.identifier" shadow="hover" class="func-card">
                <div class="func-card-header">
                  <div>
                    <span class="func-name">{{ svc.name }}</span>
                    <el-tag size="small" type="info" style="margin-left: 8px">{{ svc.identifier }}</el-tag>
                    <el-tag size="small" :type="svc.callType === 'sync' ? 'success' : 'warning'" style="margin-left: 4px">
                      {{ svc.callType === 'sync' ? '同步' : '异步' }}
                    </el-tag>
                  </div>
                  <el-button-group size="small">
                    <el-button :type="svc._editMode === 'form' ? 'primary' : ''" @click="svc._editMode = 'form'">表单</el-button>
                    <el-button :type="svc._editMode === 'json' ? 'primary' : ''" @click="svc._editMode = 'json'">JSON</el-button>
                  </el-button-group>
                </div>

                <!-- 表单模式 -->
                <div v-if="svc._editMode !== 'json'" class="func-form">
                  <el-form label-width="100px" size="small">
                    <el-form-item
                      v-for="param in parseInputParams(svc)"
                      :key="param.identifier"
                      :label="param.name || param.identifier"
                    >
                      <el-input
                        v-if="!param.dataType || param.dataType === 'text' || param.dataType === 'string'"
                        v-model="svc._params[param.identifier]"
                        :placeholder="'请输入 ' + (param.name || param.identifier)"
                      />
                      <el-input-number
                        v-else-if="param.dataType === 'int32' || param.dataType === 'int' || param.dataType === 'float' || param.dataType === 'double'"
                        v-model="svc._params[param.identifier]"
                        :controls="false"
                        style="width: 100%"
                      />
                      <el-switch
                        v-else-if="param.dataType === 'bool'"
                        v-model="svc._params[param.identifier]"
                      />
                      <el-select
                        v-else-if="param.dataType === 'enum'"
                        v-model="svc._params[param.identifier]"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="(label, val) in (param.specs || {})"
                          :key="val"
                          :label="label"
                          :value="val"
                        />
                      </el-select>
                      <el-input
                        v-else
                        v-model="svc._params[param.identifier]"
                        :placeholder="'请输入 ' + (param.name || param.identifier)"
                      />
                    </el-form-item>
                    <el-form-item v-if="parseInputParams(svc).length === 0">
                      <span style="color: #5a6d80">该服务无需入参</span>
                    </el-form-item>
                  </el-form>
                </div>

                <!-- JSON模式 -->
                <div v-else class="func-json">
                  <el-input
                    v-model="svc._jsonInput"
                    type="textarea"
                    :rows="5"
                    placeholder='JSON格式入参，如: {"brightness": 80}'
                  />
                </div>

                <div class="func-card-footer">
                  <el-tooltip content="设备已禁用，无法调用" :disabled="deviceInfo.status !== 3">
                    <el-button type="primary" :loading="svc._sending" :disabled="deviceInfo.status === 3" @click="handleServiceInvoke(svc)">
                      发送指令
                    </el-button>
                  </el-tooltip>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- ========== 设备日志 ========== -->
        <el-tab-pane label="设备日志" name="log">
          <div class="log-toolbar">
            <el-select v-model="logType" placeholder="日志类型" clearable style="width: 130px; margin-right: 10px" @change="loadLogs">
              <el-option label="上线" value="online" />
              <el-option label="离线" value="offline" />
              <el-option label="属性上报" value="property_post" />
              <el-option label="历史属性" value="property_history" />
              <el-option label="批量属性" value="property_batch" />
              <el-option label="网关上报" value="property_pack" />
              <el-option label="事件上报" value="event" />
              <el-option label="服务设置" value="service_set" />
              <el-option label="服务调用" value="service_invoke" />
            </el-select>
            <el-date-picker v-model="logTimeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="margin-right: 10px" @change="loadLogs" />
            <el-button type="primary" @click="loadLogs">搜索</el-button>
          </div>
          <el-table :data="logData" v-loading="logLoading" border stripe style="margin-top: 12px">
            <el-table-column prop="timestamp" label="时间" width="200" />
            <el-table-column prop="logType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.logType === 'online'" type="success" size="small">上线</el-tag>
                <el-tag v-else-if="row.logType === 'offline'" type="danger" size="small">离线</el-tag>
                <el-tag v-else-if="row.logType === 'property_post'" type="primary" size="small">属性上报</el-tag>
                <el-tag v-else-if="row.logType === 'property_history'" type="info" size="small">历史属性</el-tag>
                <el-tag v-else-if="row.logType === 'property_batch'" type="info" size="small">批量属性</el-tag>
                <el-tag v-else-if="row.logType === 'property_pack'" type="info" size="small">网关上报</el-tag>
                <el-tag v-else-if="row.logType === 'event'" type="warning" size="small">事件上报</el-tag>
                <el-tag v-else-if="row.logType === 'service_set'" type="warning" size="small">服务设置</el-tag>
                <el-tag v-else-if="row.logType === 'service_invoke'" type="warning" size="small">服务调用</el-tag>
                <el-tag v-else size="small">{{ row.logType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
            <el-table-column prop="messageId" label="消息ID" width="180" show-overflow-tooltip />
          </el-table>
          <el-pagination
            v-model:current-page="logPagination.page"
            v-model:page-size="logPagination.size"
            :total="logPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLogs"
            @current-change="loadLogs"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 历史数据弹窗 -->
    <el-dialog v-model="historyVisible" :title="`${historyPropertyId} 历史数据`" width="800px">
      <el-table :data="historyData" border stripe v-loading="historyLoading">
        <el-table-column prop="ts" label="时间" width="200" />
        <el-table-column prop="value" label="值" />
        <el-table-column prop="valueType" label="数据类型" width="100" />
      </el-table>
    </el-dialog>

    <!-- 设置期望值弹窗 -->
    <el-dialog v-model="desiredVisible" title="设置期望值" width="450px">
      <el-form label-width="80px">
        <el-form-item label="属性">
          <el-tag>{{ desiredItem?.identifier }}</el-tag>
        </el-form-item>
        <el-form-item label="期望值">
          <el-input v-model="desiredValue" placeholder="请输入期望值" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="desiredVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDesiredSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getDevice, getDeviceStatus, getPropertyHistory,
  listDeviceFunctions, invokeDeviceFunction,
  getDeviceLogs, getDeviceShadow, updateDeviceShadow
} from '@/api/iot'

const route = useRoute()
const router = useRouter()
const deviceId = route.params.id

function formatTime(date) {
  const d = date instanceof Date ? date : new Date(date)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const activeTab = ref('status')
const deviceInfo = reactive({})

// ========== 运行状态 ==========
const statusLoading = ref(false)
const statusData = reactive({ properties: {}, events: {} })
const statusView = ref('card')
const autoRefresh = ref(false)
let refreshTimer = null

const statusList = computed(() => {
  return Object.entries(statusData.properties || {}).map(([key, val]) => ({
    identifier: key, ...val
  }))
})
const eventList = computed(() => {
  return Object.entries(statusData.events || {}).map(([key, val]) => ({
    identifier: key, ...val
  }))
})
const statusAll = computed(() => [...statusList.value, ...eventList.value])

// ========== 影子 ==========
const shadowLoading = ref(false)
const shadowList = ref([])
const showDiffOnly = ref(false)
const filteredShadow = computed(() => {
  return showDiffOnly.value ? shadowList.value.filter(s => s.hasDiff) : shadowList.value
})

// ========== 功能 ==========
const funcLoading = ref(false)
const funcList = ref([])

// ========== 日志 ==========
const logLoading = ref(false)
const logData = ref([])
const logType = ref('')
const logTimeRange = ref(null)
const logPagination = reactive({ page: 1, size: 10, total: 0 })

// ========== 弹窗 ==========
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyData = ref([])
const historyPropertyId = ref('')

const desiredVisible = ref(false)
const desiredItem = ref(null)
const desiredValue = ref('')

// ========== 数据加载 ==========
const loadDevice = async () => {
  try {
    const res = await getDevice(deviceId)
    Object.assign(deviceInfo, res.data || {})
  } catch (e) { /* ignore */ }
}

const loadStatus = async () => {
  statusLoading.value = true
  try {
    const res = await getDeviceStatus(deviceId)
    Object.assign(statusData, res.data || {})
  } catch (e) {
    console.error(e)
  } finally {
    statusLoading.value = false
  }
}

const loadShadow = async () => {
  shadowLoading.value = true
  try {
    const res = await getDeviceShadow(deviceId)
    shadowList.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    shadowLoading.value = false
  }
}

const loadFunctions = async () => {
  funcLoading.value = true
  try {
    const res = await listDeviceFunctions(deviceId)
    const list = res.data || []
    list.forEach(svc => {
      svc._editMode = 'form'
      svc._params = {}
      svc._jsonInput = ''
      svc._sending = false
      const params = parseInputParams(svc)
      params.forEach(p => {
        svc._params[p.identifier] = p.dataType === 'bool' ? false : (p.dataType === 'int32' || p.dataType === 'int' || p.dataType === 'float' || p.dataType === 'double' ? undefined : '')
      })
    })
    funcList.value = list
  } catch (e) {
    console.error(e)
  } finally {
    funcLoading.value = false
  }
}

const loadLogs = async () => {
  logLoading.value = true
  try {
    const params = { page: logPagination.page, size: logPagination.size }
    if (logType.value) params.logType = logType.value
    if (logTimeRange.value && logTimeRange.value.length === 2) {
      params.startTime = formatTime(logTimeRange.value[0])
      params.endTime = formatTime(logTimeRange.value[1])
    }
    const res = await getDeviceLogs(deviceId, params)
    logData.value = res.data?.records || []
    logPagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    logLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'status') loadStatus()
  else if (tab === 'shadow') loadShadow()
  else if (tab === 'function') loadFunctions()
  else if (tab === 'log') loadLogs()
}

// ========== 历史 ==========
const handleHistory = async (row) => {
  historyPropertyId.value = row.identifier
  historyVisible.value = true
  historyLoading.value = true
  try {
    const endTime = formatTime(new Date())
    const start = new Date(Date.now() - 7 * 24 * 3600 * 1000)
    const startTime = formatTime(start)
    const res = await getPropertyHistory(deviceId, row.identifier, { startTime, endTime })
    historyData.value = res.data || []
  } catch (e) {
    historyData.value = []
  } finally {
    historyLoading.value = false
  }
}

// ========== 服务调用 ==========
function parseInputParams(svc) {
  try {
    const arr = typeof svc.specs === 'string' ? JSON.parse(svc.specs) : (svc.specs || [])
    return Array.isArray(arr) ? arr : []
  } catch { return [] }
}

const handleServiceInvoke = async (svc) => {
  let inputStr = ''
  if (svc._editMode === 'json') {
    inputStr = svc._jsonInput || '{}'
  } else {
    const params = parseInputParams(svc)
    const obj = {}
    params.forEach(p => {
      const val = svc._params[p.identifier]
      if (val !== undefined && val !== '') {
        if (p.dataType === 'int32' || p.dataType === 'int') obj[p.identifier] = parseInt(val) || 0
        else if (p.dataType === 'float' || p.dataType === 'double') obj[p.identifier] = parseFloat(val) || 0
        else if (p.dataType === 'bool') obj[p.identifier] = !!val
        else obj[p.identifier] = val
      }
    })
    inputStr = JSON.stringify(obj)
  }

  svc._sending = true
  try {
    await invokeDeviceFunction(deviceId, { identifier: svc.identifier, input: inputStr })
    ElMessage.success(`服务 ${svc.name} 指令已发送`)
  } catch (e) {
    console.error(e)
  } finally {
    svc._sending = false
  }
}

// ========== 期望值 ==========
const handleSetDesired = (row) => {
  desiredItem.value = row
  desiredValue.value = row.desiredValue || ''
  desiredVisible.value = true
}

const handleDesiredSubmit = async () => {
  try {
    await updateDeviceShadow(deviceId, {
      identifier: desiredItem.value.identifier,
      value: desiredValue.value
    })
    ElMessage.success('期望值设置成功')
    desiredVisible.value = false
    loadShadow()
  } catch (e) {
    console.error(e)
  }
}

// ========== 辅助 ==========
function accessModeLabel(mode) {
  if (mode === 'rw') return '读写'
  if (mode === 'w') return '只写'
  return '只读'
}

function eventTypeLabel(type) {
  if (type === 'alert') return '告警'
  if (type === 'error') return '错误'
  return '信息'
}

function eventTypeTag(type) {
  if (type === 'alert') return 'warning'
  if (type === 'error') return 'danger'
  return 'info'
}

function formatEventValues(content) {
  try {
    const obj = typeof content === 'string' ? JSON.parse(content) : content
    const data = obj.data || obj
    return JSON.stringify(data)
  } catch {
    return content
  }
}

onMounted(() => {
  loadDevice()
  loadStatus()
})

watch(autoRefresh, (val) => {
  if (val) {
    refreshTimer = setInterval(() => {
      if (activeTab.value === 'status') loadStatus()
    }, 3000)
  } else {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

onBeforeUnmount(() => {
  clearInterval(refreshTimer)
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 16px; }
.detail-title { font-size: 18px; font-weight: 600; color: #e2e8f0; }
.info-card { margin-bottom: 0; }
.empty-tip { text-align: center; color: #5a6d80; padding: 40px 0; }
.diff-value { color: #e6a23c; font-weight: 600; }
.unreported { color: #5a6d80; font-style: italic; }
.log-toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 0; }

/* 运行状态 */
.status-toolbar { display: flex; align-items: center; margin-bottom: 12px; }
.status-count { color: #8899aa; font-size: 13px; }
.auto-refresh-hint { font-size: 12px; color: #409eff; animation: blink 1.5s infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
.section-label { font-size: 14px; font-weight: 600; color: #c8d6e5; margin-bottom: 10px; }
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}
.status-card {
  border: 1px solid #1e3350;
  border-radius: 6px;
  padding: 14px;
  background: #132238;
  transition: box-shadow 0.2s;
}
.status-card:hover { box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15); border-color: #2a4060; }
.status-card.event-card { border-left: 3px solid #e6a23c; }
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.card-name { font-size: 14px; font-weight: 600; color: #e2e8f0; }
.card-value { font-size: 22px; font-weight: 700; color: #409eff; margin-bottom: 6px; min-height: 30px; }
.card-meta { display: flex; gap: 12px; color: #5a6d80; font-size: 12px; margin-bottom: 6px; }
.card-footer { display: flex; align-items: center; justify-content: space-between; }
.card-time { font-size: 12px; color: #5a6d80; }

/* 功能调用 */
.func-list { display: flex; flex-direction: column; gap: 16px; }
.func-card { }
.func-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.func-name { font-size: 15px; font-weight: 600; color: #e2e8f0; }
.func-form { margin-bottom: 8px; }
.func-json { margin-bottom: 8px; }
.func-card-footer { display: flex; justify-content: flex-end; border-top: 1px solid #1e3350; padding-top: 12px; }
.func-disabled-notice { margin-bottom: 16px; }
</style>
