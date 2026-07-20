<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="searchName" placeholder="产品名称" clearable style="width: 200px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增产品</el-button>
      <div style="margin-left: auto">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'" size="small">卡片</el-button>
          <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'" size="small">列表</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 卡片视图 -->
    <template v-if="viewMode === 'card'">
      <div v-if="tableData.length === 0 && !loading" class="empty-tip">暂无产品数据</div>
      <div class="card-grid" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="product-card" :class="{ 'card-disabled': row.status === 0 }">
          <div class="card-top" :style="{ borderLeftColor: row.status === 1 ? '#409eff' : '#909399' }">
            <div class="card-title-row">
              <span class="card-title">{{ row.name }}</span>
              <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="danger" size="small">禁用</el-tag>
            </div>
            <div class="card-key">{{ row.productKey }}</div>
          </div>
          <div class="card-body">
            <div class="card-info-row">
              <span class="info-label">节点类型</span>
              <el-tag v-if="row.nodeType === 0" size="small">直连设备</el-tag>
              <el-tag v-else-if="row.nodeType === 1" type="warning" size="small">网关</el-tag>
              <el-tag v-else type="info" size="small">子设备</el-tag>
            </div>
            <div class="card-info-row">
              <span class="info-label">协议</span>
              <span class="info-value">{{ row.protocol }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">物模型</span>
              <el-tag v-if="row.modelStatus === 0" type="success" size="small">正常</el-tag>
              <el-tag v-else type="danger" size="small">禁用</el-tag>
            </div>
            <div class="card-info-row">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ row.createTime }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleThingModel(row)">物模型</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggleProductStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-tooltip content="仅禁用状态可删除" :disabled="row.status === 0" placement="top">
              <el-button link type="danger" size="small" :disabled="row.status !== 0" @click="handleDelete(row)">删除</el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </template>

    <!-- 表格视图 -->
    <template v-else>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
        <el-table-column prop="name" label="产品名称" min-width="150" />
        <el-table-column prop="productKey" label="ProductKey" min-width="160" />
        <el-table-column prop="nodeType" label="节点类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.nodeType === 0">直连设备</el-tag>
            <el-tag v-else-if="row.nodeType === 1" type="warning">网关</el-tag>
            <el-tag v-else type="info">子设备</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="protocol" label="协议" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelStatus" label="物模型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.modelStatus === 0" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleThingModel(row)">物模型</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleProductStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link :type="row.modelStatus === 0 ? 'warning' : 'success'" @click="handleToggleModelStatus(row)">
              {{ row.modelStatus === 0 ? '物模型禁用' : '物模型启用' }}
            </el-button>
            <el-tooltip content="仅禁用状态可删除" :disabled="row.status === 0" placement="top">
              <el-button link type="danger" :disabled="row.status !== 0" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="ProductKey">
          <el-input v-model="form.productKey" placeholder="留空自动生成" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="form.nodeType" style="width: 100%">
            <el-option label="直连设备" :value="0" />
            <el-option label="网关" :value="1" />
            <el-option label="子设备" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="通信协议" prop="protocol">
          <el-select v-model="form.protocol" style="width: 100%">
            <el-option label="MQTT" value="MQTT" />
            <el-option label="HTTP" value="HTTP" />
            <el-option label="CoAP" value="CoAP" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据格式" prop="dataFormat">
          <el-select v-model="form.dataFormat" style="width: 100%">
            <el-option label="Alink JSON" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
          <div v-if="isEdit && form.status === 0 && originalStatus === 1" class="status-warn">
            禁用产品将同时禁用该产品下所有设备
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="thingModelVisible" title="物模型编辑" width="900px" top="3vh" destroy-on-close>
      <ThingModelEditor v-model="thingModelJson" />
      <template #footer>
        <el-button @click="thingModelVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSaveThingModel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, addProduct, updateProduct, deleteProduct, getThingModel, updateThingModel } from '@/api/iot'
import ThingModelEditor from './ThingModelEditor.vue'

const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const thingModelVisible = ref(false)
const formRef = ref()
const thingModelJson = ref('')
const currentProductId = ref(null)
const originalStatus = ref(null)
const viewMode = ref('card')
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑产品' : '新增产品')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = reactive({
  id: null,
  name: '',
  code: '',
  nodeType: 0,
  protocol: 'MQTT',
  dataFormat: 0,
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  nodeType: [{ required: true, message: '请选择节点类型', trigger: 'change' }],
  protocol: [{ required: true, message: '请选择通信协议', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductList({ page: pagination.page, size: pagination.size, name: searchName.value || undefined })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', productKey: '', nodeType: 0, protocol: 'MQTT', dataFormat: 0, description: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  originalStatus.value = row.status
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品「${row.name}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
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
      await updateProduct(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addProduct(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleThingModel = async (row) => {
  currentProductId.value = row.id
  try {
    const res = await getThingModel(row.id)
    thingModelJson.value = res.data || ''
  } catch (e) {
    thingModelJson.value = ''
  }
  thingModelVisible.value = true
}

const handleSaveThingModel = async () => {
  try {
    await updateThingModel(currentProductId.value, thingModelJson.value)
    ElMessage.success('物模型保存成功')
    thingModelVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleToggleProductStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  const tips = newStatus === 0
    ? `确定要禁用产品「${row.name}」吗？禁用后该产品下所有设备将被同步禁用，设备数据上报和功能调用将停止。`
    : `确定要启用产品「${row.name}」吗？`
  try {
    await ElMessageBox.confirm(tips, '操作确认', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
    await updateProduct(row.id, { ...row, status: newStatus })
    ElMessage.success(`产品已${action}`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleToggleModelStatus = async (row) => {
  const newStatus = row.modelStatus === 0 ? 1 : 0
  const action = newStatus === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该产品的物模型吗？`, '提示', { type: 'warning' })
    await updateProduct(row.id, { ...row, modelStatus: newStatus })
    ElMessage.success(`物模型已${action}`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

loadData()
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; }
.status-warn {
  margin-top: 6px;
  font-size: 12px;
  color: #e6a23c;
  line-height: 1.4;
}
.empty-tip { text-align: center; color: #5a6d80; padding: 60px 0; }

/* ===== 卡片视图 ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.product-card {
  background: #132238;
  border: 1px solid #1e3350;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.product-card:hover {
  border-color: #2a4060;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.product-card.card-disabled {
  opacity: 0.7;
}

.card-top {
  padding: 16px 18px 12px;
  border-left: 3px solid #409eff;
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

.card-footer {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 18px;
  border-top: 1px solid #1e3350;
  background: rgba(10, 22, 40, 0.3);
}
</style>
