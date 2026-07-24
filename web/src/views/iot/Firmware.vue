<template>
  <div class="page-container">
    <div class="toolbar">
      <el-select v-model="searchProductId" placeholder="选择产品" clearable style="width: 180px; margin-right: 10px" @clear="loadData" @change="loadData">
        <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-input v-model="searchName" placeholder="固件名称" clearable style="width: 180px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleUpload">上传固件</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
      <el-table-column prop="firmwareName" label="固件名称" min-width="150" />
      <el-table-column prop="firmwareVersion" label="版本" width="120" />
      <el-table-column prop="productKey" label="ProductKey" width="160" show-overflow-tooltip />
      <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="文件大小" width="100">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="fileMd5" label="MD5" width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="mono">{{ row.fileMd5 ? row.fileMd5.substring(0, 12) + '...' : '--' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" title="上传固件" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="所属产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" style="width: 100%">
            <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="固件名称" prop="firmwareName">
          <el-input v-model="form.firmwareName" placeholder="如: main-controller-v2" />
        </el-form-item>
        <el-form-item label="固件版本" prop="firmwareVersion">
          <el-input v-model="form.firmwareVersion" placeholder="如: 1.0.0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="固件描述信息" />
        </el-form-item>
        <el-form-item label="存储配置" prop="storageId">
          <el-select v-model="form.storageId" placeholder="请选择存储" style="width: 100%">
            <el-option v-for="s in storageList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="固件文件" prop="file">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            :on-remove="handleFileRemove"
            accept=".bin,.zip,.tar.gz,.tgz,.hex,.img"
          >
            <div style="padding: 20px 0">
              <div style="color: #8899aa; font-size: 14px">将文件拖到此处，或<em style="color: #409eff">点击上传</em></div>
              <div style="color: #5a6d80; font-size: 12px; margin-top: 6px">支持 .bin .zip .tar.gz .hex .img 格式</div>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFirmwareList,
  uploadFirmware,
  deleteFirmware,
  getProductList,
  getFileStorageList
} from '@/api/iot'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const productList = ref([])
const storageList = ref([])
const searchName = ref('')
const searchProductId = ref(null)
const dialogVisible = ref(false)
const formRef = ref()
const uploadRef = ref()

const pagination = reactive({ page: 1, size: 10, total: 0 })

const defaultForm = {
  id: null,
  productId: null,
  firmwareName: '',
  firmwareVersion: '',
  description: '',
  storageId: null,
  file: null
}
const form = reactive({ ...defaultForm })

const rules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  firmwareName: [{ required: true, message: '请输入固件名称', trigger: 'blur' }],
  firmwareVersion: [{ required: true, message: '请输入固件版本', trigger: 'blur' }],
  storageId: [{ required: true, message: '请选择存储配置', trigger: 'change' }]
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i === 0 ? 0 : 1) + ' ' + units[i]
}

const handleFileChange = (file) => {
  form.file = file.raw
}

const handleFileRemove = () => {
  form.file = null
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

const loadProducts = async () => {
  try {
    const res = await getProductList({ page: 1, size: 1000 })
    productList.value = res.data?.records || []
  } catch (e) { /* ignore */ }
}

const loadStorages = async () => {
  try {
    const res = await getFileStorageList({ page: 1, size: 1000, status: 0 })
    storageList.value = res.data?.records || []
  } catch (e) { /* ignore */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchName.value) params.name = searchName.value
    if (searchProductId.value) params.productId = searchProductId.value
    const res = await getFirmwareList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleUpload = () => {
  Object.assign(form, { ...defaultForm })
  if (uploadRef.value) uploadRef.value.clearFiles()
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除固件「${row.firmwareName}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteFirmware(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!form.file) {
    ElMessage.warning('请选择固件文件')
    return
  }
  submitLoading.value = true
  try {
    const formData = new FormData()
    formData.append('productId', form.productId)
    formData.append('firmwareName', form.firmwareName)
    formData.append('firmwareVersion', form.firmwareVersion)
    if (form.description) formData.append('description', form.description)
    formData.append('storageId', form.storageId)
    formData.append('file', form.file)
    await uploadFirmware(formData)
    ElMessage.success('上传成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadProducts()
  loadStorages()
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.empty-tip { text-align: center; color: #5a6d80; padding: 60px 0; }
.mono { font-family: 'Consolas', 'Monaco', monospace; font-size: 12px; }
</style>
