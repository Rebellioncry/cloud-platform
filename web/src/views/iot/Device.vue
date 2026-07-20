<template>
  <div class="page-container">
    <div class="toolbar">
      <el-select v-model="searchProductId" placeholder="选择产品" clearable style="width: 180px; margin-right: 10px" @clear="loadData" @change="loadData">
        <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-input v-model="searchName" placeholder="设备名称" clearable style="width: 180px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="searchStatus" placeholder="设备状态" clearable style="width: 130px; margin-right: 10px" @clear="loadData" @change="loadData">
        <el-option label="未激活" :value="0" />
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="2" />
        <el-option label="禁用" :value="3" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增设备</el-button>
      <div style="margin-left: auto">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'" size="small">卡片</el-button>
          <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'" size="small">列表</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 卡片视图 -->
    <template v-if="viewMode === 'card'">
      <div v-if="tableData.length === 0 && !loading" class="empty-tip">暂无设备数据</div>
      <div class="card-grid" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="device-card" :class="cardStatusClass(row.status)">
          <div class="card-top" :style="{ borderLeftColor: statusColor(row.status) }">
            <div class="card-title-row">
              <span class="card-title">{{ row.nickname || row.deviceName }}</span>
              <el-tag v-if="row.status === 0" type="info" size="small">未激活</el-tag>
              <el-tag v-else-if="row.status === 1" type="success" size="small">在线</el-tag>
              <el-tag v-else-if="row.status === 2" type="warning" size="small">离线</el-tag>
              <el-tag v-else type="danger" size="small">禁用</el-tag>
            </div>
            <div class="card-key">{{ row.deviceName }}</div>
          </div>
          <div class="card-body">
            <div class="card-info-row">
              <span class="info-label">ProductKey</span>
              <span class="info-value mono">{{ row.productKey }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">IP地址</span>
              <span class="info-value mono">{{ row.ipAddress || '--' }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">最后上线</span>
              <span class="info-value">{{ row.lastOnlineTime || '--' }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ row.createTime }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handleActivate(row)">激活</el-button>
            <el-button v-if="row.status !== 3" link type="warning" size="small" @click="handleDisable(row)">禁用</el-button>
            <el-tooltip content="仅禁用状态可删除" :disabled="row.status === 3" placement="top">
              <el-button link type="danger" size="small" :disabled="row.status !== 3" @click="handleDelete(row)">删除</el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </template>

    <!-- 表格视图 -->
    <template v-else>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="productKey" label="ProductKey" min-width="160" />
        <el-table-column prop="deviceKey" label="DeviceKey" min-width="160" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info" size="small">未激活</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" size="small">
              <span class="status-dot online"></span>在线
            </el-tag>
            <el-tag v-else-if="row.status === 2" type="warning" size="small">
              <span class="status-dot offline"></span>离线
            </el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="lastOnlineTime" label="最后上线" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="handleActivate(row)">激活</el-button>
            <el-button v-if="row.status !== 3" link type="warning" @click="handleDisable(row)">禁用</el-button>
            <el-tooltip content="仅禁用状态可删除" :disabled="row.status === 3" placement="top">
              <el-button link type="danger" :disabled="row.status !== 3" @click="handleDelete(row)">删除</el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </template>

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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" style="width: 100%" :disabled="!!form.id">
            <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="如: sensor01（作为MQTT用户名）" :disabled="deviceActive" />
        </el-form-item>
        <el-form-item label="DeviceKey">
          <el-input v-model="form.deviceKey" placeholder="留空自动生成" :disabled="deviceActive" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="固件版本" prop="firmwareVersion">
          <el-input v-model="form.firmwareVersion" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceList, addDevice, updateDevice, deleteDevice, activateDevice, disableDevice, getProductList } from '@/api/iot'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const productList = ref([])
const searchName = ref('')
const searchProductId = ref(null)
const searchStatus = ref(null)
const viewMode = ref('card')
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑设备' : '新增设备')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = reactive({
  id: null,
  productId: null,
  deviceName: '',
  deviceKey: '',
  nickname: '',
  firmwareVersion: ''
})

const deviceActive = computed(() => isEdit.value && form.status !== 0 && form.status !== 3)

const statusColor = (status) => {
  const map = { 0: '#909399', 1: '#67c23a', 2: '#e6a23c', 3: '#f56c6c' }
  return map[status] || '#909399'
}
const cardStatusClass = (status) => {
  if (status === 3) return 'card-disabled'
  if (status === 0) return 'card-inactive'
  return ''
}

const rules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const loadProducts = async () => {
  try {
    const res = await getProductList({ page: 1, size: 1000 })
    productList.value = res.data?.records || []
  } catch (e) { /* ignore */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchName.value) params.name = searchName.value
    if (searchProductId.value) params.productId = searchProductId.value
    if (searchStatus.value !== null) params.status = searchStatus.value
    const res = await getDeviceList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  router.push(`/iot/device/${row.id}`)
}

const handleAdd = () => {
  Object.assign(form, { id: null, productId: null, deviceName: '', deviceKey: '', nickname: '', firmwareVersion: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备「${row.deviceName}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteDevice(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleActivate = async (row) => {
  try {
    await activateDevice(row.id)
    ElMessage.success('设备已激活')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm('确定要禁用该设备吗？', '提示', { type: 'warning' })
    await disableDevice(row.id)
    ElMessage.success('设备已禁用')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateDevice(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addDevice(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

loadProducts()

onMounted(() => {
  const statusParam = route.query.status
  if (statusParam !== undefined && statusParam !== '') {
    searchStatus.value = Number(statusParam)
  }
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
.status-dot.online { background-color: #67c23a; }
.status-dot.offline { background-color: #e6a23c; }
.empty-tip { text-align: center; color: #5a6d80; padding: 60px 0; }

/* ===== 卡片视图 ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.device-card {
  background: #132238;
  border: 1px solid #1e3350;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.device-card:hover {
  border-color: #2a4060;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.device-card.card-disabled {
  opacity: 0.7;
}
.device-card.card-inactive {
  opacity: 0.8;
}

.card-top {
  padding: 16px 18px 12px;
  border-left: 3px solid #67c23a;
}
.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
}
.card-key {
  font-size: 12px;
  color: #5a6d80;
  font-family: 'Consolas', 'Monaco', monospace;
}

.card-body {
  padding: 0 18px 12px;
}
.card-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid rgba(30, 51, 80, 0.5);
}
.card-info-row:last-child {
  border-bottom: none;
}
.info-label {
  font-size: 13px;
  color: #8899aa;
}
.info-value {
  font-size: 13px;
  color: #c8d6e5;
}
.info-value.mono {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 18px;
  border-top: 1px solid #1e3350;
  background: rgba(10, 22, 40, 0.3);
}
</style>
