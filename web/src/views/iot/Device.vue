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
    </div>

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
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceList, addDevice, updateDevice, deleteDevice, activateDevice, disableDevice, getProductList } from '@/api/iot'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const productList = ref([])
const searchName = ref('')
const searchProductId = ref(null)
const searchStatus = ref(null)
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
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', { type: 'warning' })
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
loadData()
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
.status-dot.online { background-color: #67c23a; }
.status-dot.offline { background-color: #e6a23c; }
</style>
