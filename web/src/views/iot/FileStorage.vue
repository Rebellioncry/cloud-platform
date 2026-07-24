<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="searchName" placeholder="存储名称" clearable style="width: 180px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增存储</el-button>
      <div style="margin-left: auto">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'" size="small">卡片</el-button>
          <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'" size="small">列表</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 卡片视图 -->
    <template v-if="viewMode === 'card'">
      <div v-if="tableData.length === 0 && !loading" class="empty-tip">暂无存储配置</div>
      <div class="card-grid" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="device-card">
          <div class="card-top" :style="{ borderLeftColor: row.isDefault ? '#409eff' : '#67c23a' }">
            <div class="card-title-row">
              <span class="card-title">{{ row.name }}</span>
            </div>
            <div class="card-key">{{ row.storageType === 1 ? 'MinIO' : '本地存储' }}{{ row.isDefault ? ' · 默认' : '' }}</div>
          </div>
          <div class="card-body">
            <div class="card-info-row">
              <span class="info-label">Endpoint</span>
              <span class="info-value mono">{{ row.endpoint || '--' }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">Bucket</span>
              <span class="info-value mono">{{ row.bucket || '--' }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">Region</span>
              <span class="info-value mono">{{ row.region || '--' }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ row.createTime }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleTest(row)">测试连接</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- 表格视图 -->
    <template v-else>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
        <el-table-column prop="name" label="存储名称" min-width="140" />
        <el-table-column prop="storageType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.storageType === 1 ? 'MinIO' : '本地' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endpoint" label="Endpoint" min-width="180" show-overflow-tooltip />
        <el-table-column prop="bucket" label="Bucket" width="140" />
        <el-table-column prop="region" label="Region" width="120" />
        <el-table-column prop="isDefault" label="默认" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="primary" size="small">是</el-tag>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleTest(row)">测试连接</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="存储名称" prop="name">
          <el-input v-model="form.name" placeholder="如: my-storage" />
        </el-form-item>
        <el-form-item label="存储类型" prop="storageType">
          <el-select v-model="form.storageType" placeholder="请选择类型" style="width: 100%" @change="onStorageTypeChange">
            <el-option label="本地存储" :value="0" />
            <el-option label="MinIO" :value="1" />
          </el-select>
        </el-form-item>
        <template v-if="form.storageType === 0">
          <el-form-item label="本地路径" prop="localPath">
            <el-input v-model="form.localPath" placeholder="如: /data/firmware" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="Endpoint" prop="endpoint">
            <el-input v-model="form.endpoint" placeholder="如: http://minio.example.com:9000" />
          </el-form-item>
          <el-form-item label="AccessKey" prop="accessKey">
            <el-input v-model="form.accessKey" placeholder="Access Key" />
          </el-form-item>
          <el-form-item label="SecretKey" prop="secretKey">
            <el-input v-model="form.secretKey" placeholder="Secret Key" show-password />
          </el-form-item>
          <el-form-item label="Bucket" prop="bucket">
            <el-input v-model="form.bucket" placeholder="Bucket 名称" />
          </el-form-item>
          <el-form-item label="Region">
            <el-input v-model="form.region" placeholder="可选" />
          </el-form-item>
        </template>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFileStorageList,
  addFileStorage,
  updateFileStorage,
  deleteFileStorage,
  testFileStorage
} from '@/api/iot'

const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const viewMode = ref('card')
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑存储' : '新增存储')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const defaultForm = {
  id: null,
  name: '',
  storageType: 1,
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucket: '',
  region: '',
  localPath: '',
  isDefault: false
}
const form = reactive({ ...defaultForm })

const minioRules = {
  name: [{ required: true, message: '请输入存储名称', trigger: 'blur' }],
  storageType: [{ required: true, message: '请选择存储类型', trigger: 'change' }],
  endpoint: [{ required: true, message: '请输入Endpoint', trigger: 'blur' }],
  accessKey: [{ required: true, message: '请输入AccessKey', trigger: 'blur' }],
  secretKey: [{ required: true, message: '请输入SecretKey', trigger: 'blur' }],
  bucket: [{ required: true, message: '请输入Bucket', trigger: 'blur' }]
}

const localRules = {
  name: [{ required: true, message: '请输入存储名称', trigger: 'blur' }],
  storageType: [{ required: true, message: '请选择存储类型', trigger: 'change' }],
  localPath: [{ required: true, message: '请输入本地路径', trigger: 'blur' }]
}

const rules = computed(() => form.storageType === 1 ? minioRules : localRules)

const onStorageTypeChange = () => {
  form.endpoint = ''
  form.accessKey = ''
  form.secretKey = ''
  form.bucket = ''
  form.region = ''
  form.localPath = ''
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchName.value) params.name = searchName.value
    const res = await getFileStorageList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { ...defaultForm })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除存储「${row.name}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteFileStorage(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleTest = async (row) => {
  const loadingInstance = ElMessage({ message: '正在测试连接...', type: 'info', duration: 0 })
  try {
    await testFileStorage(row.id)
    loadingInstance.close()
    ElMessage.success('连接测试成功')
  } catch (e) {
    loadingInstance.close()
    ElMessage.error('连接测试失败: ' + (e.message || '未知错误'))
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateFileStorage(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addFileStorage(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.empty-tip { text-align: center; color: #5a6d80; padding: 60px 0; }

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
