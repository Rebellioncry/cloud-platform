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
        <el-tab-pane label="运行状态" name="status">
          <div v-loading="statusLoading">
            <div v-if="Object.keys(statusData.properties || {}).length === 0" class="empty-tip">暂无运行数据</div>
            <el-table v-else :data="statusList" border stripe>
              <el-table-column prop="identifier" label="属性标识" width="180" />
              <el-table-column prop="value" label="当前值" width="200" />
              <el-table-column prop="valueType" label="数据类型" width="100" />
              <el-table-column prop="timestamp" label="上报时间" width="200" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleHistory(row)">历史</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

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

        <el-tab-pane label="功能调用" name="function">
          <div v-loading="funcLoading">
            <div v-if="funcList.length === 0" class="empty-tip">暂无功能定义</div>
            <el-table v-else :data="funcList" border stripe>
              <el-table-column prop="identifier" label="标识符" width="180" />
              <el-table-column prop="name" label="名称" width="180" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.type === 'property'" size="small">属性</el-tag>
                  <el-tag v-else-if="row.type === 'service'" type="warning" size="small">服务</el-tag>
                  <el-tag v-else type="info" size="small">事件</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="dataType" label="数据类型" width="100" />
              <el-table-column prop="accessMode" label="读写" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.accessMode === 'rw'" size="small">读写</el-tag>
                  <el-tag v-else type="info" size="small">只读</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button v-if="row.type === 'service'" link type="primary" @click="handleInvoke(row)">调用</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="设备日志" name="log">
          <div class="log-toolbar">
            <el-select v-model="logType" placeholder="日志类型" clearable style="width: 130px; margin-right: 10px" @change="loadLogs">
              <el-option label="上线" value="online" />
              <el-option label="离线" value="offline" />
              <el-option label="属性上报" value="property_post" />
              <el-option label="事件上报" value="event_post" />
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
                <el-tag v-else-if="row.logType === 'event_post'" type="warning" size="small">事件上报</el-tag>
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

    <el-dialog v-model="historyVisible" :title="`${historyPropertyId} 历史数据`" width="800px">
      <el-table :data="historyData" border stripe v-loading="historyLoading">
        <el-table-column prop="ts" label="时间" width="200" />
        <el-table-column prop="value" label="值" />
        <el-table-column prop="valueType" label="数据类型" width="100" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="invokeVisible" title="调用功能" width="500px">
      <el-form label-width="80px">
        <el-form-item label="功能">
          <el-tag>{{ invokeFunc?.name }} ({{ invokeFunc?.identifier }})</el-tag>
        </el-form-item>
        <el-form-item label="入参">
          <el-input v-model="invokeInput" type="textarea" :rows="4" placeholder='JSON格式，如: {"brightness": 80}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="invokeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInvokeSubmit">发送</el-button>
      </template>
    </el-dialog>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDevice, getDeviceStatus, getPropertyHistory, listDeviceFunctions, invokeDeviceFunction, getDeviceLogs, getDeviceShadow, updateDeviceShadow } from '@/api/iot'

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

const statusLoading = ref(false)
const statusData = reactive({ properties: {} })
const statusList = computed(() => {
  return Object.entries(statusData.properties || {}).map(([key, val]) => ({
    identifier: key,
    ...val
  }))
})

const shadowLoading = ref(false)
const shadowList = ref([])
const showDiffOnly = ref(false)
const filteredShadow = computed(() => {
  return showDiffOnly.value ? shadowList.value.filter(s => s.hasDiff) : shadowList.value
})

const funcLoading = ref(false)
const funcList = ref([])

const logLoading = ref(false)
const logData = ref([])
const logType = ref('')
const logTimeRange = ref(null)
const logPagination = reactive({ page: 1, size: 10, total: 0 })

const historyVisible = ref(false)
const historyLoading = ref(false)
const historyData = ref([])
const historyPropertyId = ref('')

const invokeVisible = ref(false)
const invokeFunc = ref(null)
const invokeInput = ref('')

const desiredVisible = ref(false)
const desiredItem = ref(null)
const desiredValue = ref('')

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
    funcList.value = res.data || []
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

const handleInvoke = (row) => {
  invokeFunc.value = row
  invokeInput.value = ''
  invokeVisible.value = true
}

const handleInvokeSubmit = async () => {
  try {
    await invokeDeviceFunction(deviceId, {
      identifier: invokeFunc.value.identifier,
      input: invokeInput.value
    })
    ElMessage.success('功能调用已发送')
    invokeVisible.value = false
  } catch (e) {
    console.error(e)
  }
}

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

onMounted(() => {
  loadDevice()
  loadStatus()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 16px; }
.detail-title { font-size: 18px; font-weight: 600; }
.info-card { margin-bottom: 0; }
.empty-tip { text-align: center; color: #999; padding: 40px 0; }
.diff-value { color: #e6a23c; font-weight: 600; }
.log-toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
</style>
