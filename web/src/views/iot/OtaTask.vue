<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="searchName" placeholder="任务名称" clearable style="width: 180px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleCreate">创建任务</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
      <el-table-column prop="taskName" label="任务名称" min-width="150" />
      <el-table-column prop="firmwareName" label="固件" min-width="150" />
      <el-table-column prop="targetType" label="目标范围" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.targetType === 0" size="small">产品全量</el-tag>
          <el-tag v-else-if="row.targetType === 1" type="warning" size="small">指定设备</el-tag>
          <el-tag v-else type="info" size="small">按版本</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="progress" label="进度" width="160">
        <template #default="{ row }">
          <el-progress
            :percentage="row.totalCount ? Math.round((row.successCount + row.failCount) / row.totalCount * 100) : 0"
            :status="row.status === 3 ? 'exception' : row.status === 2 ? '' : undefined"
            :stroke-width="14"
          />
        </template>
      </el-table-column>
      <el-table-column label="成功/失败/总计" width="160" align="center">
        <template #default="{ row }">
          <span style="color: #67c23a">{{ row.successCount || 0 }}</span> /
          <span style="color: #f56c6c">{{ row.failCount || 0 }}</span> /
          <span>{{ row.totalCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="info" size="small">待执行</el-tag>
          <el-tag v-else-if="row.status === 1" type="primary" size="small">执行中</el-tag>
          <el-tag v-else-if="row.status === 2" type="success" size="small">已完成</el-tag>
          <el-tag v-else-if="row.status === 3" type="danger" size="small">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="success" @click="handleStart(row)">启动</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="handleCancel(row)">取消</el-button>
          <el-button link type="primary" @click="handleDevices(row)">设备明细</el-button>
          <el-tooltip content="仅待执行状态可删除" :disabled="row.status !== 0" placement="top">
            <el-button link type="danger" :disabled="row.status !== 0" @click="handleDelete(row)">删除</el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 创建任务对话框 -->
    <el-dialog v-model="dialogVisible" title="创建OTA任务" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="如: v2.0-全量升级" />
        </el-form-item>
        <el-form-item label="所属产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" style="width: 100%" @change="onProductChange">
            <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择固件" prop="firmwareId">
          <el-select v-model="form.firmwareId" placeholder="请选择固件" style="width: 100%" :disabled="!form.productId">
            <el-option v-for="f in firmwareList" :key="f.id" :label="f.firmwareName + ' v' + f.firmwareVersion" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标范围" prop="targetType">
          <el-radio-group v-model="form.targetType">
            <el-radio :value="0">产品全量</el-radio>
            <el-radio :value="1">指定设备</el-radio>
            <el-radio :value="2">按版本</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetType === 1" label="设备ID" prop="deviceIds">
          <el-input v-model="form.deviceIds" type="textarea" :rows="3" placeholder="多个设备ID用逗号分隔，如: device01,device02,device03" />
        </el-form-item>
        <el-form-item v-if="form.targetType === 2" label="目标版本" prop="targetVersion">
          <el-input v-model="form.targetVersion" placeholder="如: 1.0.0（低于此版本的设备将升级）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设备明细抽屉 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="75%">
      <el-table :data="deviceList" v-loading="deviceLoading" border stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="currentVersion" label="当前版本" width="120" />
        <el-table-column prop="targetVersion" label="目标版本" width="120" />
        <el-table-column prop="progress" label="进度" width="100">
          <template #default="{ row }">
            {{ row.progress || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info" size="small">待升级</el-tag>
            <el-tag v-else-if="row.status === 1" size="small">推送中</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning" size="small">下载中</el-tag>
            <el-tag v-else-if="row.status === 3" style="background: #e6a23c; color: #fff" size="small">升级中</el-tag>
            <el-tag v-else-if="row.status === 4" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="row.status === 5" type="danger" size="small">失败</el-tag>
            <el-tag v-else type="info" size="small">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="180" show-overflow-tooltip />
        <el-table-column prop="pushTime" label="推送时间" width="180" />
        <el-table-column prop="completeTime" label="完成时间" width="180" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOtaTaskList,
  deleteOtaTask,
  startOtaTask,
  cancelOtaTask,
  getOtaTaskDevices,
  getFirmwareList,
  getProductList
} from '@/api/iot'

const loading = ref(false)
const deviceLoading = ref(false)
const tableData = ref([])
const deviceList = ref([])
const productList = ref([])
const firmwareList = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const formRef = ref()

const pagination = reactive({ page: 1, size: 10, total: 0 })

const defaultForm = {
  id: null,
  taskName: '',
  productId: null,
  firmwareId: null,
  targetType: 0,
  deviceIds: '',
  targetVersion: ''
}
const form = reactive({ ...defaultForm })

const drawerTitle = computed(() => '设备明细')

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  firmwareId: [{ required: true, message: '请选择固件', trigger: 'change' }],
  targetType: [{ required: true, message: '请选择目标范围', trigger: 'change' }]
}

const loadProducts = async () => {
  try {
    const res = await getProductList({ page: 1, size: 1000 })
    productList.value = res.data?.records || []
  } catch (e) { /* ignore */ }
}

const loadFirmwares = async (productId) => {
  try {
    const params = { page: 1, size: 1000, status: 1 }
    if (productId) params.productId = productId
    const res = await getFirmwareList(params)
    firmwareList.value = res.data?.records || []
  } catch (e) { /* ignore */ }
}

const onProductChange = () => {
  form.firmwareId = null
  loadFirmwares(form.productId)
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchName.value) params.name = searchName.value
    const res = await getOtaTaskList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  Object.assign(form, { ...defaultForm })
  firmwareList.value = []
  dialogVisible.value = true
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要启动任务「${row.taskName}」吗？`,
      '启动确认',
      { type: 'warning', confirmButtonText: '确定启动', cancelButtonText: '取消' }
    )
    await startOtaTask(row.id)
    ElMessage.success('任务已启动')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消任务「${row.taskName}」吗？已推送的设备不会受影响。`,
      '取消确认',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '返回' }
    )
    await cancelOtaTask(row.id)
    ElMessage.success('任务已取消')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务「${row.taskName}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteOtaTask(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleDevices = async (row) => {
  drawerVisible.value = true
  deviceLoading.value = true
  deviceList.value = []
  try {
    const res = await getOtaTaskDevices(row.id)
    deviceList.value = res.data || []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载设备明细失败')
  } finally {
    deviceLoading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.targetType === 1 && !form.deviceIds) {
    ElMessage.warning('请输入设备ID')
    return
  }
  if (form.targetType === 2 && !form.targetVersion) {
    ElMessage.warning('请输入目标版本')
    return
  }
  try {
    const data = {
      taskName: form.taskName,
      productId: form.productId,
      firmwareId: form.firmwareId,
      targetType: form.targetType
    }
    if (form.targetType === 1) data.deviceIds = form.deviceIds
    if (form.targetType === 2) data.targetVersion = form.targetVersion
    await addOtaTask(data)
    ElMessage.success('任务创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadProducts()
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.empty-tip { text-align: center; color: #5a6d80; padding: 60px 0; }
</style>
